import { Component, inject, signal, computed, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransferenciaService } from '../../service/transferencia.service';
import { TransferenciaRequest } from '../../models/transferencia.model';
import { AlertService } from '../../../../core/services/alert.service';
import { CatalogoService } from '../../../configuraciones/services/catalogo.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { AuthService } from '../../../auth/services/auth.service';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';

@Component({
    selector: 'app-transferencia-solicitar',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PrimaryButtonComponent,
        SearchableSelectComponent,
        FormInputComponent
    ],
    templateUrl: './transferencia-solicitar.component.html',
})
export class TransferenciaSolicitarComponent {
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    private transferenciaService = inject(TransferenciaService);
    private catalogoService = inject(CatalogoService);
    private sucursalService = inject(SucursalService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);

    idSucursalOrigen = signal<number | null>(null); // A quien se le pide
    motivo = signal<string>('');
    detalles = signal<any[]>([]);
    loading = signal<boolean>(false);
    sucursales = signal<any[]>([]);
    nombreSucursalDestino = signal<string>(''); // Sucursal actual (quien pide)

    // Nombre de la sucursal de origen seleccionada
    nombreSucursalOrigen = computed(() => {
        const id = this.idSucursalOrigen();
        if (!id) return null;
        return this.sucursales().find(s => s.idSucursal === id)?.nombreSucursal;
    });

    // Búsqueda de productos
    productosSearch = signal<any[]>([]);
    productoSeleccionado = signal<any>(null); // Señal para resetear el buscador

    constructor() {
        this.cargarSucursales();
        this.cargarProductos();
    }

    cargarSucursales() {
        const miSucursalId = this.authService.getSucursalIdFromToken();
        this.sucursalService.getActivas().subscribe(res => {
            if (res.success) {
                // Filtrar para no pedirse a sí mismo
                this.sucursales.set(res.data.filter(s => s.idSucursal !== miSucursalId));

                // Obtener nombre de mi sucursal
                const miSucursal = res.data.find(s => s.idSucursal === miSucursalId);
                if (miSucursal) {
                    this.nombreSucursalDestino.set(miSucursal.nombreSucursal);
                }
            }
        });
    }

    cargarProductos() {
        this.catalogoService.listarTodos(0, 2000).subscribe(res => {
            if (res.success && res.data) {
                this.productosSearch.set(res.data.content.map(p => ({
                    id: p.id,
                    label: `${p.nombre} (${p.codigo || 'SIN COD'})`,
                    sublabel: p.presentacion || 'UND',
                    data: p
                })));
            }
        });
    }

    seleccionarProducto(p: any) {
        if (!p) return;
        const productoData = p.data;

        const existe = this.detalles().find(d => d.idCatalogo === productoData.id);
        if (existe) {
            this.alertService.error('Duplicado', 'El producto ya está en la lista');
            setTimeout(() => this.productoSeleccionado.set(null), 0);
            return;
        }

        const nuevoDetalle = {
            idCatalogo: productoData.id,
            nombreProducto: productoData.nombre,
            cantidad: 1,
            unidadMedida: productoData.presentacion || 'UND'
        };

        this.detalles.update(prev => [...prev, nuevoDetalle]);

        // Forzar el vaciado del componente
        setTimeout(() => {
            this.productoSeleccionado.set(null);
        }, 100);
    }

    eliminarDetalle(index: number) {
        this.detalles.update(prev => prev.filter((_, i) => i !== index));
    }

    onClose() {
        this.resetForm();
        this.cerrar.emit();
    }

    resetForm() {
        this.idSucursalOrigen.set(null);
        this.motivo.set('');
        this.detalles.set([]);
    }

    async procesar() {
        if (!this.idSucursalOrigen()) {
            this.alertService.warning('Formulario Incompleto', 'Debe seleccionar la sucursal de origen');
            return;
        }

        if (this.detalles().length === 0) {
            this.alertService.warning('Formulario Incompleto', 'Debe agregar al menos un producto');
            return;
        }

        const { isConfirmed } = await this.alertService.confirm(
            '¿Enviar Solicitud?',
            `Se solicitarán ${this.detalles().length} productos a la sucursal seleccionada.`
        );

        if (isConfirmed) {
            this.loading.set(true);
            const request: TransferenciaRequest = {
                idSucursalDestino: this.authService.getSucursalIdFromToken()!,
                idSucursalOrigen: this.idSucursalOrigen()!,
                idUsuario: this.authService.getUserIdFromToken() || 1,
                motivo: this.motivo(),
                detalles: this.detalles().map(d => ({
                    idCatalogo: d.idCatalogo,
                    cantidad: d.cantidad
                }))
            };

            this.transferenciaService.solicitar(request).subscribe({
                next: () => {
                    this.alertService.toast('Solicitud enviada correctamente', 'success');
                    this.resetForm();
                    this.guardado.emit();
                    this.loading.set(false);
                },
                error: (err) => {
                    this.alertService.error('Error', 'Hubo un problema al enviar la solicitud');
                    this.loading.set(false);
                }
            });
        }
    }
}
