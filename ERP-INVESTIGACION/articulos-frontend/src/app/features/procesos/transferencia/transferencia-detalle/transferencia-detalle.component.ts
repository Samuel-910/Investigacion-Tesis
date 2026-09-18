import { Component, inject, signal, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransferenciaSucursal, TransferenciaDetalle } from '../../models/transferencia.model';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ApiResponse } from '../../../../shared/modals/response.model';
import { TransferenciaService } from '../../service/transferencia.service';
import { ProductoService } from '../../../almacen/service/producto.service';
import { Producto } from '../../../almacen/models/producto.model';


@Component({
    selector: 'app-transferencia-detalle',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PrimaryButtonComponent
    ],
    templateUrl: './transferencia-detalle.component.html',
})
export class TransferenciaDetalleComponent implements OnChanges {
    @Input() idTransferencia: number | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() accionCompletada = new EventEmitter<void>();

    private transferenciaService = inject(TransferenciaService);
    private authService = inject(AuthService);
    private productoService = inject(ProductoService);
    private alertService = inject(AlertService);

    transferencia = signal<TransferenciaSucursal | null>(null);
    loading = signal<boolean>(false);
    esOrigen = signal<boolean>(false);
    esDestino = signal<boolean>(false);

    // Para el envío
    lotesDisponibles = new Map<number, Producto[]>();

    constructor() { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['idTransferencia'] && this.idTransferencia) {
            this.cargarTransferencia(this.idTransferencia);
        }
    }

    cargarTransferencia(id: number) {
        this.loading.set(true);
        this.transferenciaService.obtenerPorId(id).subscribe({
            next: (res: ApiResponse<TransferenciaSucursal>) => {
                if (res.success && res.data) {
                    const t = res.data;
                    this.transferencia.set(t);
                    const miSucursal = this.authService.getSucursalIdFromToken();
                    this.esOrigen.set(t.idSucursalOrigen === miSucursal);
                    this.esDestino.set(t.idSucursalDestino === miSucursal);
                    const estadoNombre = this.getEstadoNombre(t.estado);

                    if (estadoNombre === 'SOLICITADO' && this.esOrigen()) {
                        this.cargarLotesParaEnvio(t.detalles);
                    }
                }
                this.loading.set(false);
            },
            error: () => {
                this.alertService.error('Error', 'No se pudo cargar la transferencia');
                this.loading.set(false);
            }
        });
    }

    cargarLotesParaEnvio(detalles: TransferenciaDetalle[]) {
        detalles.forEach(d => {
            this.productoService.listarPorCatalogoYSucursal(d.idCatalogo, this.authService.getSucursalIdFromToken()!)
                .subscribe((res: ApiResponse<Producto[]>) => {
                    if (res.success) {
                        this.lotesDisponibles.set(d.idCatalogo, res.data);
                    }
                });
        });
    }

    onClose() {
        this.cerrar.emit();
    }

    async confirmarEnvio() {
        const t = this.transferencia();
        if (!t) return;

        const detallesEnvio = t.detalles.map(d => ({
            idCatalogo: d.idCatalogo,
            cantidad: d.cantidadEnviada || d.cantidadSolicitada,
            nroLote: d.nroLote,
            fechaVenc: d.fechaVenc
        }));

        if (detallesEnvio.some(d => !d.nroLote)) {
            this.alertService.error('Incompleto', 'Debe seleccionar un lote para cada producto');
            return;
        }

        const { isConfirmed } = await this.alertService.confirm(
            'Confirmar Envío',
            'Se rebajará el stock de su sucursal y la transferencia pasará a estado ENVIADO.'
        );

        if (isConfirmed) {
            this.loading.set(true);
            this.transferenciaService.enviar(t.id!, this.authService.getUserIdFromToken()!, detallesEnvio).subscribe({
                next: () => {
                    this.alertService.toast('Transferencia enviada correctamente', 'success');
                    this.accionCompletada.emit();
                    this.loading.set(false);
                },
                error: (err) => {
                    this.alertService.error('Error', err.error?.message || 'Hubo un problema al procesar el envío');
                    this.loading.set(false);
                }
            });
        }
    }

    async confirmarRecepcion() {
        const t = this.transferencia();
        if (!t) return;

        const { isConfirmed } = await this.alertService.confirm(
            'Confirmar Recepción',
            'Se ingresará el stock a su sucursal y la transferencia pasará a estado RECIBIDO.'
        );

        if (isConfirmed) {
            this.loading.set(true);
            // hardcoded almacenDestino a 1 por ahora como en el anterior
            this.transferenciaService.recibir(t.id!, this.authService.getUserIdFromToken()!, 1).subscribe({
                next: () => {
                    this.alertService.toast('Transferencia recibida correctamente', 'success');
                    this.accionCompletada.emit();
                    this.loading.set(false);
                },
                error: () => {
                    this.alertService.error('Error', 'Hubo un problema al procesar la recepción');
                    this.loading.set(false);
                }
            });
        }
    }

    async rechazarTransferencia() {
        const t = this.transferencia();
        if (!t) return;

        const { isConfirmed, value: motivo } = await this.alertService.prompt(
            'Rechazar Transferencia',
            'Ingrese el motivo del rechazo:',
            'text'
        );

        if (isConfirmed) {
            this.loading.set(true);
            this.transferenciaService.rechazar(t.id!, this.authService.getUserIdFromToken()!, motivo || 'Rechazado por sucursal origen').subscribe({
                next: () => {
                    this.alertService.toast('Transferencia rechazada', 'success');
                    this.accionCompletada.emit();
                    this.loading.set(false);
                },
                error: (err) => {
                    this.alertService.error('Error', err.error?.message || 'Hubo un problema al rechazar la transferencia');
                    this.loading.set(false);
                }
            });
        }
    }

    seleccionarLote(detalle: TransferenciaDetalle, nroLote: string) {
        const lotes = this.lotesDisponibles.get(detalle.idCatalogo) || [];
        const lote = lotes.find(l => l.nroLote === nroLote);
        if (lote) {
            detalle.nroLote = lote.nroLote;
            detalle.fechaVenc = lote.fechaVencimiento?.toString();
            detalle.cantidadEnviada = detalle.cantidadSolicitada;
        }
    }

    getEstadoNombre(estado: any): string {
        if (!estado) return '';
        if (typeof estado === 'string') return estado;
        if (typeof estado === 'object') return estado.name || estado.valor?.toString() || '';
        return String(estado);
    }
}
