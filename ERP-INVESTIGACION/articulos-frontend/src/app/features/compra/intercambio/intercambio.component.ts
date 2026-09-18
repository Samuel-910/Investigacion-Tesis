import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IntercambioService } from '../services/intercambio.service';
import { ProductoService } from '../../almacen/service/producto.service';
import { CatalogoService } from '../../configuraciones/services/catalogo.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';
import { AlertService } from '../../../core/services/alert.service';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { AuthService } from '../../auth/services/auth.service';

@Component({
    selector: 'app-intercambio',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        FormInputComponent,
        SearchableSelectComponent
    ],
    templateUrl: './intercambio.component.html'
})
export class IntercambioComponent implements OnInit {
    loading = signal(false);
    productosStock = signal<any[]>([]);

    selectedProductoFrom = signal<any>(null);
    proveedorOriginal = signal<any>(null);

    cantidadIntercambio = signal(1);

    searchNombre = signal('');
    searchLote = signal('');

    // Para el producto de destino
    catalogos = signal<any[]>([]);
    idCatalogoTo = signal<number | null>(null);
    selectedCatalogoTo = signal<any>(null);
    nuevoLote = signal('');
    nuevaFechaVencimiento = signal('');
    motivo = signal('');
    cantidadEntregar = signal<number>(1);
    cantidadRecibir = signal<number>(1);
    precioCompraDestino = signal<number | null>(null);
    showErrors = signal(false);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Compras', route: '/compras' },
        { label: 'Intercambio de Productos' }
    ];

    constructor(
        private intercambioService: IntercambioService,
        private productoService: ProductoService,
        private catalogoService: CatalogoService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.cargarProductosStock();
        this.cargarCatalogos();
    }

    cargarCatalogos(): void {
        this.catalogoService.listarTodos(0, 2000, 'PRODUCTO').subscribe(res => {
            if (res.success) {
                this.catalogos.set(res.data.content);
            }
        });
    }

    cargarProductosStock(): void {
        this.loading.set(true);
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (!sucursalId) return;

        this.productoService.buscar(
            this.searchNombre(),
            0,
            100,
            sucursalId
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    let items = res.data.content || [];
                    if (this.searchLote()) {
                        items = items.filter((p: any) =>
                            p.nroLote?.toLowerCase().includes(this.searchLote().toLowerCase())
                        );
                    }
                    this.productosStock.set(items);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onSearchNombreChange(val: string): void {
        this.searchNombre.set(val);
        this.cargarProductosStock();
    }

    onSearchLoteChange(val: string): void {
        this.searchLote.set(val);
        this.cargarProductosStock();
    }

    seleccionarProductoFrom(producto: any): void {
        this.selectedProductoFrom.set(producto);
        this.precioCompraDestino.set(producto.precioCompra || 0);
        this.proveedorOriginal.set(null);

        // Si ya hay un producto destino seleccionado y es el mismo, lo quitamos
        if (this.selectedCatalogoTo() && this.selectedCatalogoTo()?.id === producto?.catalogo?.id) {
            this.alertService.toast('El producto destino ha sido limpiado por ser igual al origen', 'warning');
            this.selectedCatalogoTo.set(null);
            this.idCatalogoTo.set(null);
        }

        this.intercambioService.obtenerProveedor(producto.idProducto, producto.nroLote).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.proveedorOriginal.set(res.data);
                } else {
                    this.proveedorOriginal.set({ razonSocial: 'No registrado / Sin proveedor', numeroDocumento: 'N/A' });
                }
            },
            error: () => {
                this.proveedorOriginal.set({ razonSocial: 'Error al cargar proveedor', numeroDocumento: 'N/A' });
            }
        });
    }

    seleccionarCatalogoTo(cat: any): void {
        const from = this.selectedProductoFrom();
        if (from && cat && from.catalogo?.id === cat.id) {
            this.alertService.warning('Atención', 'No puede intercambiar un producto por el mismo producto (mismo catálogo).');
            // Timeout para dejar que angular actualice el valor, luego lo limpiamos
            setTimeout(() => {
                this.selectedCatalogoTo.set(null);
                this.idCatalogoTo.set(null);
            });
            return;
        }
        this.selectedCatalogoTo.set(cat);
    }

    onCantidadEntregarChange(val: number): void {
        const from = this.selectedProductoFrom();
        if (from && val > from.stock) {
            this.cantidadEntregar.set(from.stock);
            this.alertService.toast(`La cantidad máxima disponible es ${from.stock}`, 'warning');
        } else {
            this.cantidadEntregar.set(val || 1);
        }
    }

    procesarIntercambio(): void {
        this.showErrors.set(true);
        const from = this.selectedProductoFrom();
        const to = this.selectedCatalogoTo();

        if (!from || !to || !this.cantidadEntregar() || !this.cantidadRecibir() || !this.motivo()) {
            this.alertService.toast('Complete todos los campos requeridos', 'error');
            return;
        }

        if (to.manejaLotes && !this.nuevoLote()) {
            this.alertService.toast('El producto destino maneja lotes, debe ingresar un nuevo lote', 'error');
            return;
        }

        if (this.cantidadEntregar() > from.stock) {
            this.alertService.toast('La cantidad a entregar excede el stock disponible', 'error');
            return;
        }

        const request = {
            idProductoOrigen: from.idProducto,
            cantidad: this.cantidadEntregar(),
            cantidadDestino: this.cantidadRecibir(),
            precioCompraDestino: this.precioCompraDestino(),
            idCatalogoDestino: to.id,
            idProveedor: this.proveedorOriginal()?.id,
            loteDestino: this.nuevoLote(),
            fechaVencDestino: this.nuevaFechaVencimiento() || null,
            motivo: this.motivo()
        };

        this.alertService.confirm('¿Confirmar Intercambio?', 'Se realizará el movimiento en el Kardex.').then(result => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.intercambioService.procesarIntercambio(request).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.toast('Intercambio procesado correctamente', 'success');
                            this.resetForm();
                            this.cargarProductosStock();
                        } else {
                            this.alertService.toast(res.message || 'Error al procesar intercambio', 'error');
                        }
                        this.loading.set(false);
                    },
                    error: () => {
                        this.loading.set(false);
                        this.alertService.toast('Error de conexión', 'error');
                    }
                });
            }
        });
    }

    resetForm(): void {
        this.selectedProductoFrom.set(null);
        this.proveedorOriginal.set(null);
        this.selectedCatalogoTo.set(null);
        this.idCatalogoTo.set(null);
        this.nuevoLote.set('');
        this.nuevaFechaVencimiento.set('');
        this.motivo.set('');
        this.cantidadEntregar.set(1);
        this.cantidadRecibir.set(1);
        this.precioCompraDestino.set(null);
        this.showErrors.set(false);
    }
}
