import { Component, Input, Output, EventEmitter, inject, signal, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaChicaService } from '../../services/caja-chica.service';
import { AlertService } from '../../../../core/services/alert.service';
import { CajaResumen } from '../../models/caja-chica.model';
import { ModalComponent } from '../../../../shared/components/modal/modal';

export interface DesgloseArqueo {
    metodoPago: string;
    saldoTeorico: number;
    saldoReal: number;
    diferencia: number;
}

@Component({
    selector: 'app-caja-arqueo-modal',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './caja-arqueo-modal.component.html'
})
export class CajaArqueoModalComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() cajaId: number | null = null;
    @Output() modalClose = new EventEmitter<boolean>();

    private cajaChicaService = inject(CajaChicaService);
    private alertService = inject(AlertService);

    resumen = signal<CajaResumen | null>(null);
    cargando = signal(false);
    saldoReal = signal<number>(0);
    diferencia = signal<number>(0);
    transferirACajaGeneral = signal<boolean>(true);
    desglose = signal<DesgloseArqueo[]>([]);

    ngOnChanges(changes: SimpleChanges): void {
        const opened = changes['isOpen']?.currentValue === true;
        const idChanged = changes['cajaId'] && this.cajaId;

        if ((opened || (this.isOpen && idChanged)) && this.cajaId) {
            this.cargarResumen();
        }
    }

    constructor() { }

    recalcularTotales(): void {
        const lista = this.desglose();
        let totalReal = 0;
        let totalTeorico = 0;

        lista.forEach(item => {
            item.diferencia = item.saldoReal - item.saldoTeorico;
            totalReal += item.saldoReal;
            totalTeorico += item.saldoTeorico;
        });

        this.saldoReal.set(totalReal);
        this.diferencia.set(totalReal - totalTeorico);
    }

    cargarResumen() {
        if (!this.cajaId) return;
        this.cargando.set(true);
        this.cajaChicaService.obtenerResumen(this.cajaId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const data = res.data;
                    this.resumen.set(data);

                    // 1. Obtener todos los métodos de pago únicos
                    const metodos = new Set<string>();
                    metodos.add('EFECTIVO'); // Siempre incluimos efectivo
                    if (data.ingresos) {
                        data.ingresos.forEach(i => {
                            if (i.metodoPago) metodos.add(i.metodoPago.toUpperCase());
                        });
                    }
                    if (data.egresos) {
                        data.egresos.forEach(e => {
                            if (e.metodoPago) metodos.add(e.metodoPago.toUpperCase());
                        });
                    }

                    // 2. Calcular teórico por método de pago
                    const listaDesglose: DesgloseArqueo[] = Array.from(metodos).map(m => {
                        const inicial = m === 'EFECTIVO' ? data.saldoInicial : 0;
                        const ingresos = (data.ingresos || [])
                            .filter(i => i.metodoPago && i.metodoPago.toUpperCase() === m)
                            .reduce((sum, item) => sum + item.monto, 0);
                        const egresos = (data.egresos || [])
                            .filter(e => e.metodoPago && e.metodoPago.toUpperCase() === m)
                            .reduce((sum, item) => sum + item.monto, 0);
                        const teorico = inicial + ingresos - egresos;

                        return {
                            metodoPago: m,
                            saldoTeorico: teorico,
                            saldoReal: teorico, // Inicialmente el real es igual al teórico
                            diferencia: 0
                        };
                    });

                    this.desglose.set(listaDesglose);
                    this.recalcularTotales();
                }
                this.cargando.set(false);
            },
            error: () => {
                this.alertService.error('Error al cargar resumen de arqueo');
                this.cargando.set(false);
                this.close();
            }
        });
    }

    close() {
        this.modalClose.emit(false);
    }

    confirmarCierre() {
        if (!this.cajaId) return;

        this.alertService.confirm(
            '¿Confirmar Cierre?',
            'Una vez cerrada, no podrá registrar más movimientos en esta caja.',
            'Sí, Cerrar Caja',
            'Cancelar'
        ).then(result => {
            if (result.isConfirmed) {
                this.cargando.set(true);

                const request = {
                    saldoCierreReal: this.saldoReal(),
                    transferirACajaGeneral: true,
                    diferencias: this.desglose().map(d => ({
                        metodoPago: d.metodoPago,
                        montoTeorico: d.saldoTeorico,
                        montoReal: d.saldoReal,
                        diferencia: d.diferencia
                    }))
                };

                this.cajaChicaService.cerrarCajaDetallado(this.cajaId!, request).subscribe({
                    next: () => {
                        this.alertService.success('Caja cerrada correctamente');
                        this.modalClose.emit(true);
                        this.cargando.set(false);
                    },
                    error: (err) => {
                        this.alertService.error('Error al cerrar caja: ' + (err.error?.message || 'Error desconocido'));
                        this.cargando.set(false);
                    }
                });
            }
        });
    }
}
