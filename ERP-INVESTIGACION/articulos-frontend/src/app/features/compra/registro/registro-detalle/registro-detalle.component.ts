import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompraResponse, CronogramaPago } from '../../models/compra.model';
import { CompraService } from '../../services/compra.service';
import { CronogramaPagoService } from '../../services/cronograma-pago.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { AlertService } from '../../../../core/services/alert.service';


import { Subscription } from 'rxjs';

@Component({
    selector: 'app-registro-detalle',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './registro-detalle.component.html'
})
export class RegistroDetalleComponent implements OnInit, OnChanges, OnDestroy {
    @Input() idCompra: number | null = null;
    @Output() cerrar = new EventEmitter<void>();

    compra = signal<CompraResponse | null>(null);
    loading = signal(false);
    private wsSubscription?: Subscription;

    private cronogramaService = inject(CronogramaPagoService);
    private websocketService = inject(WebSocketService);
    private alertService = inject(AlertService);

    constructor(private compraService: CompraService) { }

    ngOnInit() {
        if (this.idCompra) {
            this.cargarDetalle();
            this.escucharNotificaciones();
        }
    }

    ngOnDestroy() {
        if (this.wsSubscription) {
            this.wsSubscription.unsubscribe();
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['idCompra'] && this.idCompra) {
            this.cargarDetalle();
        }
    }

    escucharNotificaciones() {
        this.websocketService.connect();
        this.wsSubscription = this.websocketService.notifications$.subscribe(notif => {
            if (notif.modulo === 'COMPRAS') {
                // Si el mensaje menciona nuestra compra, recargamos
                const info = String(notif.contenido || '');
                if (info.includes('#' + this.idCompra)) {
                    console.log('Recargando por notificación de WebSocket...');
                    this.cargarDetalle();
                }
            }
        });
    }

    cargarDetalle() {
        if (!this.idCompra) return;

        this.loading.set(true);
        this.compraService.obtener(this.idCompra).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.compra.set(res.data);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    solicitarPago(cuota: CronogramaPago) {
        this.alertService.confirm('Solicitar Pago', '¿Desea solicitar el pago para esta cuota?')
            .then(result => {
                if (result.isConfirmed) {
                    this.cronogramaService.solicitarPago(cuota.idCronograma).subscribe({
                        next: (res) => {
                            if (res.success) {
                                // Se omite la alerta local para dejar que el WebSocket (header) muestre la notificación
                                this.cargarDetalle(); // Recargar para ver estado solicitado
                            }
                        }
                    });
                }
            });
    }

    abrirModalPago(cuota: CronogramaPago) {
        // Usar SweetAlert2 para pedir el número de operación de forma rápida
        this.alertService.custom({
            title: 'Registrar Pago',
            text: 'Ingrese el número de operación del pago',
            input: 'text',
            inputPlaceholder: 'Nº Operación',
            showCancelButton: true,
            confirmButtonText: 'Registrar Pago',
            inputValidator: (value: any) => {
                if (!value) return '¡El número de operación es obligatorio!';
                return null;
            }
        }).then(result => {
            if (result.isConfirmed && result.value) {
                this.registrarPago(cuota.idCronograma, result.value);
            }
        });
    }

    registrarPago(idCronograma: number, nroOp: string) {
        this.cronogramaService.pagarCuota(idCronograma, nroOp).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success('Pago Registrado', 'El pago se ha procesado correctamente.');
                    this.cargarDetalle();
                }
            }
        });
    }

    imprimirFisico() {
        if (!this.idCompra) return;
        this.loading.set(true);
        this.compraService.imprimir(this.idCompra).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    const iframe = document.createElement('iframe');
                    iframe.style.position = 'fixed';
                    iframe.style.right = '0';
                    iframe.style.bottom = '0';
                    iframe.style.width = '0';
                    iframe.style.height = '0';
                    iframe.style.border = '0';
                    document.body.appendChild(iframe);

                    const doc = iframe.contentWindow?.document;
                    if (doc) {
                        doc.open();
                        doc.write(res.data);
                        doc.close();

                        setTimeout(() => {
                            iframe.contentWindow?.focus();
                            iframe.contentWindow?.print();
                            setTimeout(() => document.body.removeChild(iframe), 1000);
                        }, 500);
                    }
                }
            },
            error: () => this.loading.set(false)
        });
    }

    getEstadoNombre(estado: any): string {
        if (!estado) return '';
        if (typeof estado === 'string') return estado;
        if (typeof estado === 'object') return estado.name || estado.valor?.toString() || '';
        return String(estado);
    }

    getTipoAfectacionNombre(codigo: string | number): string {
        if (!codigo) return '';
        const cod = String(codigo);
        if (cod === '10') return 'GRAVADO';
        if (cod === '20') return 'EXONERADO';
        if (cod === '30') return 'INAFECTO';
        return cod;
    }
}
