import { Component, EventEmitter, Input, Output, signal, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventarioService } from '../../service/movimiento.service';
import { AlertService } from '../../../../core/services/alert.service';
import { PrimaryButtonComponent } from "../../../../shared/components/primary-button/primary-button";
import { AuthService } from '../../../auth/services/auth.service';
import { PuntoDocumentoService } from '../../../documentos/services/punto-documento.service';
import { DocumentoImpresionService } from '../../../documentos/services/documento-impresion.service';

@Component({
    selector: 'app-movimiento-detalle-modal',
    standalone: true,
    imports: [CommonModule, PrimaryButtonComponent],
    templateUrl: './movimiento-detalle-modal.component.html'
})
export class MovimientoDetalleModalComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() movimiento: any = null;
    @Output() onClose = new EventEmitter<void>();

    private inventarioService = inject(InventarioService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    private impresionService = inject(DocumentoImpresionService);

    loadingDetalles = signal(false);
    detallesMovimiento = signal<any[]>([]);

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen'] && this.isOpen && this.movimiento) {
            this.cargarDetalles();
        } else if (changes['isOpen'] && !this.isOpen) {
            this.detallesMovimiento.set([]);
        }
    }

    cargarDetalles(): void {
        this.loadingDetalles.set(true);
        this.detallesMovimiento.set([]);

        this.inventarioService.obtenerDetallesDiverso(this.movimiento.id || this.movimiento.referenciaId).subscribe({
            next: (res) => {
                if (res.success) {
                    this.detallesMovimiento.set(res.data);
                }
                this.loadingDetalles.set(false);
            },
            error: () => {
                this.loadingDetalles.set(false);
                this.alertService.error('Error', 'No se pudieron cargar los detalles del movimiento');
            }
        });
    }

    close(): void {
        this.onClose.emit();
    }

    imprimirComprobante(): void {
        const authPuntoId = this.authService.getPuntoIdFromToken();
        if (!authPuntoId) {
            this.alertService.error('Error', 'No se ha detectado el punto de venta asociado al usuario');
            return;
        }

        // Para movimientos diversos usamos modulo='MOVIMIENTO' (o el que tengan configurado, pero usualmente MOVIMIENTO o ALMACEN)
        // Buscamos cualquier plantilla activa para este módulo
        this.puntoDocumentoService.obtenerPorPunto(authPuntoId).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const asignacion = res.data.find((a: any) => 
                        (a.modulo === 'MOVIMIENTO' || a.modulo === 'ALMACEN') && 
                        a.estado === 'ACTIVO'
                    );
                    
                    if (!asignacion || !asignacion.idPlantilla) {
                        this.alertService.error('Error', 'No hay plantilla de impresión configurada para Movimientos');
                        return;
                    }

                    const idPlantilla = asignacion.idPlantilla;
                    
                    this.alertService.toast('Generando comprobante de movimiento...', 'info');
                    
                    const movId = this.movimiento.id || this.movimiento.referenciaId;
                    this.inventarioService.obtenerMovimientoDiverso(movId).subscribe({
                        next: (resMov: any) => {
                            if (resMov.success && resMov.data) {
                                // Add details to the movement object since the template might need them
                                resMov.data.detalles = this.detallesMovimiento();
                                this.impresionService.imprimirMovimiento(idPlantilla, resMov.data)
                                    .then(() => this.alertService.toast('Documento enviado a impresión', 'success'))
                                    .catch(() => this.alertService.error('Error', 'No se pudo generar la impresión'));
                            }
                        },
                        error: () => this.alertService.error('Error', 'No se pudieron cargar los datos del movimiento')
                    });
                }
            },
            error: () => this.alertService.error('Error', 'No se pudo obtener la configuración de documentos')
        });
    }
}
