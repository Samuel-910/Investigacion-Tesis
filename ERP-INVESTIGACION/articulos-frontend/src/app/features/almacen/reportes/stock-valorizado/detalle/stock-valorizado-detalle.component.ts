import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../../shared/components/page-header/page-header';
import { Columna, TablaGeneralComponent } from '../../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../../shared/components/breadcrumb/breadcrumb';
import { KardexService } from '../../../service/kardex.service';
import { AlertService } from '../../../../../core/services/alert.service';
import { AuthService } from '../../../../auth/services/auth.service';
import { SidebarService } from '../../../../../shared/sidebar/sidebar.service';
import { SearchGenericComponent } from '../../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-stock-valorizado-detalle',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        SearchGenericComponent,
        PaginationComponent
    ],
    templateUrl: './stock-valorizado-detalle.component.html'
})
export class StockValorizadoDetalleComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private kardexService = inject(KardexService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    public sidebarService = inject(SidebarService);

    idCatalogo = signal<number | null>(null);
    idSucursal = signal<number>(1);
    productoInfo = signal<any>(null);
    loading = signal(false);

    currentPage = signal(0);
    pageSize = signal(5);
    totalElements = signal(0);
    totalPages = signal(0);
    movimientosPaginados = signal<any[]>([]);

    searchTerm = signal('');
    searchType = signal('ALL');

    fechaDesde = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0];
    fechaHasta = new Date().toISOString().split('T')[0];

    movimientosRaw: any[] = [];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Operación', value: 'OPERACION' },
        { label: 'Documento', value: 'DOCUMENTO' },
        { label: 'Cliente/Proveedor', value: 'CLIENTE' }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Reportes', route: '/reportes' },
        { label: 'Stock Valorizado', route: '/reportes/stock-valorizado' },
        { label: 'Detalle de Movimientos' }
    ];

    columns: Columna[] = [
        { field: 'fecha', header: 'FECHA', tipo: 'date', subField: [] },
        { field: 'proceso', header: 'PROCESO', tipo: 'badge', subField: [] },
        { field: 'operacion', header: 'OPERACIÓN', tipo: 'text', subField: [] },
        { field: 'numDoc', header: 'DOCUMENTO', tipo: 'text', subField: [] },
        { field: 'origenId', header: 'R.U.C.', tipo: 'text', subField: [] },
        { field: 'detalle', header: 'CLIENTE/PROVEEDOR', tipo: 'text', subField: [] },
        { field: 'cantidad', header: 'CANTIDAD', tipo: 'text', subField: [] },
        { field: 'saldoCantidad', header: 'SALDO', tipo: 'text', subField: [] },
        { field: 'costoUnitario', header: 'COSTO', tipo: 'currency', subField: [] },
        { field: 'saldoCostoTotal', header: 'VALORIZADO', tipo: 'currency', subField: [] },
        { field: 'nroLote', header: 'LOTE', tipo: 'text', subField: [] },
        { field: 'fechaVenc', header: 'VENCIMIENTO', tipo: 'date', subField: [] },
        { field: 'almacenNombre', header: 'ALMACÉN', tipo: 'text', subField: [] }
    ];

    ngOnInit(): void {
        this.idSucursal.set(this.authService.getSucursalIdFromToken() || 1);
        this.route.params.subscribe(params => {
            if (params['id']) {
                this.idCatalogo.set(+params['id']);
                this.cargarMovimientos();
            }
        });
    }

    cargarMovimientos(): void {
        if (!this.idCatalogo()) return;

        this.loading.set(true);
        this.kardexService.getMovimientos(
            this.idCatalogo()!,
            this.idSucursal(),
            0,
            1000,
            this.fechaDesde,
            this.fechaHasta
        ).subscribe({
            next: (res) => {
                if (res.success && res.data.content) {
                    const mapped = res.data.content.map(m => ({
                        ...m,
                        proceso: m.signo === '+' ? 'ENTRADA' : 'SALIDA',
                        almacenNombre: 'ALMACÉN PRINCIPAL'
                    }));
                    this.movimientosRaw = mapped;
                    this.aplicarFiltroYPagina();

                    if (mapped.length > 0 && mapped[0].producto) {
                        this.productoInfo.set({
                            codigo: this.idCatalogo(),
                            nombre: mapped[0].producto.nombre
                        });
                    }
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudieron cargar los movimientos');
            }
        });
    }

    private aplicarFiltroYPagina(): void {
        const query = this.searchTerm().toLowerCase().trim();
        const type = this.searchType();
        let filtrados = this.movimientosRaw;

        if (query) {
            filtrados = filtrados.filter(m => {
                switch (type) {
                    case 'OPERACION': return m.operacion?.toLowerCase().includes(query);
                    case 'DOCUMENTO': return m.numDoc?.toLowerCase().includes(query);
                    case 'CLIENTE': return m.detalle?.toLowerCase().includes(query);
                    default:
                        return m.operacion?.toLowerCase().includes(query) ||
                               m.numDoc?.toLowerCase().includes(query) ||
                               m.detalle?.toLowerCase().includes(query) ||
                               m.origenId?.toString().includes(query);
                }
            });
        }

        this.totalElements.set(Number(filtrados.length));
        this.totalPages.set(Math.ceil(filtrados.length / this.pageSize()));

        const inicio = Number(this.currentPage()) * Number(this.pageSize());
        const fin = inicio + Number(this.pageSize());
        this.movimientosPaginados.set(filtrados.slice(inicio, fin));
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.aplicarFiltroYPagina();
    }

    cambiarPagina(page: any): void {
        this.currentPage.set(Number(page));
        this.aplicarFiltroYPagina();
    }

    cambiarTamanoPagina(size: any): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.aplicarFiltroYPagina();
    }

    onFiltrar(): void {
        this.cargarMovimientos();
    }
}
