import { Component, EventEmitter, Input, Output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VentaRegistroService } from '../../services/venta-registro.service';
import { AlertService } from '../../../../core/services/alert.service';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { AuthService } from '../../../auth/services/auth.service';
import { PuntoDocumentoService } from '../../../documentos/services/punto-documento.service';
import { DocumentoImpresionService } from '../../../documentos/services/documento-impresion.service';

@Component({
    selector: 'app-venta-detalle-modal',
    standalone: true,
    imports: [CommonModule, TablaGeneralComponent],
    templateUrl: './venta-detalle-modal.component.html'
})
export class VentaDetalleModalComponent {
    @Input() isOpen = false;
    @Input() venta: any = null;
    @Output() onClose = new EventEmitter<void>();

    private ventaService = inject(VentaRegistroService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    private impresionService = inject(DocumentoImpresionService);

    get totalVenta(): number {
        return this.venta?.total || 0;
    }

    get detalles(): any[] {
        return this.venta?.detalles || [];
    }

    get detallesFormateados(): any[] {
        return this.detalles.map(det => ({
            ...det,
            descripcionMostrar: det.descripcion || det.glosa
        }));
    }

    columnasDetalle: Columna[] = [
        { field: 'item', header: 'Item', tipo: 'text', subField: [] },
        { field: 'descripcionMostrar', header: 'Descripción', tipo: 'text', subField: [] },
        { field: 'cantidad', header: 'Cant.', tipo: 'text', subField: [] },
        { field: 'unidadMedida', header: 'U.M.', tipo: 'text', subField: [] },
        { field: 'precioUnitario', header: 'P. Unit', tipo: 'currency', subField: [] },
        { field: 'total', header: 'Total', tipo: 'currency', subField: [] }
    ];

    close(): void {
        this.onClose.emit();
    }

    enviarWhatsApp(): void {
        const telefono = prompt('Ingrese el número de WhatsApp (ej: 51944074058). Déjelo en blanco para usar el de la clínica:', '');
        if (telefono !== null) {
            this.alertService.toast('Enviando comprobante por WhatsApp...', 'info');
            this.ventaService.enviarPorWhatsApp(this.venta.idVenta, telefono, 0).subscribe({
                next: () => {
                    this.alertService.toast('¡Comprobante enviado por WhatsApp exitosamente!', 'success');
                },
                error: (err) => {
                    this.alertService.error('Error al enviar WhatsApp', err.error?.message || 'Error desconocido');
                }
            });
        }
    }

    imprimirComprobante(): void {
        const authPuntoId = this.authService.getPuntoIdFromToken();
        if (!authPuntoId) {
            this.alertService.error('Error', 'No se ha detectado el punto de venta asociado al usuario');
            return;
        }

        const tipoDoc = this.venta?.tipoDoc || this.venta?.tipoDocumento || '03'; 
        let moduleType = 'VENTA';
        if (tipoDoc === 'NV' || tipoDoc === 'NOTA_VENTA' || tipoDoc === 'Nota de Venta') {
            moduleType = 'NOTA_VENTA';
        } else if (tipoDoc === '07' || tipoDoc === 'NOTA_CREDITO' || tipoDoc === 'Nota de Crédito') {
            moduleType = 'NOTA_CREDITO';
        } else if (tipoDoc === '08' || tipoDoc === 'NOTA_DEBITO' || tipoDoc === 'Nota de Débito') {
            moduleType = 'NOTA_DEBITO';
        }

        // Convert long names to short names for db matching if needed, though '01', '03' etc are expected
        let searchTipoDoc = tipoDoc;
        if (searchTipoDoc.toLowerCase() === 'factura') searchTipoDoc = '01';
        else if (searchTipoDoc.toLowerCase() === 'boleta') searchTipoDoc = '03';
        else if (searchTipoDoc.toLowerCase().includes('nota de venta')) searchTipoDoc = 'NV';
        else if (searchTipoDoc.toLowerCase().includes('nota de crédito')) searchTipoDoc = '07';
        else if (searchTipoDoc.toLowerCase().includes('nota de débito')) searchTipoDoc = '08';

        this.puntoDocumentoService.obtenerPorPunto(authPuntoId).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const asignacion = res.data.find((a: any) => 
                        a.modulo === moduleType && 
                        a.estado === 'ACTIVO' &&
                        a.tipoDoc === searchTipoDoc
                    );
                    
                    if (!asignacion || !asignacion.idPlantilla) {
                        this.alertService.error('Error', `No hay plantilla configurada para el punto y tipo de documento (${searchTipoDoc})`);
                        return;
                    }

                    const idPlantilla = asignacion.idPlantilla;
                    const ventaId = this.venta.idVenta || this.venta.id;
                    
                    this.alertService.toast('Generando comprobante...', 'info');
                    this.ventaService.obtenerPorId(ventaId).subscribe({
                        next: (resVenta) => {
                            if (resVenta.success && resVenta.data) {
                                this.impresionService.imprimirVenta(idPlantilla, resVenta.data)
                                    .then(() => this.alertService.toast('Documento enviado a impresión', 'success'))
                                    .catch(() => this.alertService.error('Error', 'No se pudo generar la impresión'));
                            }
                        },
                        error: () => {
                            this.alertService.error('Error', 'No se pudieron cargar los detalles para imprimir');
                        }
                    });
                } else {
                    this.alertService.error('Error', 'No se pudo obtener la configuración de documentos');
                }
            },
            error: () => this.alertService.error('Error', 'No se pudo obtener la configuración de documentos')
        });
    }
}
