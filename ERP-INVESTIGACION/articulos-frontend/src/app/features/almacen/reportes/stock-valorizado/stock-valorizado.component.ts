import { FormsModule } from '@angular/forms';
import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { ProductoService } from '../../service/producto.service';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent,
        } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-stock-valorizado',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        SearchGenericComponent,
        PaginationComponent,
        SearchableSelectComponent
    ],
    templateUrl: './stock-valorizado.component.html'
})
export class StockValorizadoComponent implements OnInit {
    currentPage = signal(0);
    pageSize = signal(5);
    totalElements = signal(0);
    totalPages = signal(0);
    datosPaginados = signal<any[]>([]);
    
    loading = signal(false);

    sucursalId = signal<string | null>(null);
    sucursalOptions = signal<any[]>([]);
    totalInventario = signal(0);

    searchTerm = signal('');
    searchType = signal('ALL');

    private datosRaw: any[] = [];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Producto', value: 'PRODUCTO' },
        { label: 'Laboratorio', value: 'LABORATORIO' },
        { label: 'Almacén', value: 'ALMACEN' }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Reportes', route: '/almacen/reportes' },
        { label: 'Stock Valorizado' }
    ];

    columns: Columna[] = [
        { field: 'idProducto', header: 'ID', tipo: 'text', subField: [] },
        { field: 'nombre', header: 'Producto', tipo: 'text', subField: [] },
        { field: 'laboratorio', header: 'Laboratorio', tipo: 'text', subField: [] },
        { field: 'almacen', header: 'Almacén', tipo: 'text', subField: [] },
        { field: 'stock', header: 'Stock', tipo: 'text', subField: [] },
        { field: 'precioCompra', header: 'Costo Unit.', tipo: 'currency', subField: [] },
        { field: 'valorTotal', header: 'Valor Total', tipo: 'currency', subField: [] }
    ];

    private productoService = inject(ProductoService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    private sucursalService = inject(SucursalService);
    private router = inject(Router);
    public sidebarService = inject(SidebarService);

    constructor() { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    cargarDatos(): void {
        this.loading.set(true);
        const sucVal = this.sucursalId();
        const idSucursal = sucVal ? parseInt(sucVal) : (this.authService.getSucursalIdFromToken() || 1);

        this.productoService.obtenerStockValorizado(idSucursal).subscribe({
            next: (res) => {
                if (res.success) {
                    this.calcularGranTotal(res.data || []);
                }
            }
        });

        this.productoService.obtenerStockValorizado(idSucursal, this.currentPage(), this.pageSize()).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.datosRaw = res.data.content || [];
                    this.totalElements.set(Number(res.data.totalElements || 0));
                    this.totalPages.set(res.data.totalPages || 0);
                    this.aplicarFiltroYPagina();
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudo cargar el reporte de stock valorizado');
            }
        });
    }

    private aplicarFiltroYPagina(): void {
        const query = this.searchTerm().toLowerCase().trim();
        const type = this.searchType();
        let filtrados = this.datosRaw;

        if (query) {
            filtrados = filtrados.filter(d => {
                switch (type) {
                    case 'PRODUCTO': return d.nombre?.toLowerCase().includes(query);
                    case 'LABORATORIO': return d.laboratorio?.toLowerCase().includes(query);
                    case 'ALMACEN': return d.almacen?.toLowerCase().includes(query);
                    default:
                        return d.nombre?.toLowerCase().includes(query) ||
                               d.laboratorio?.toLowerCase().includes(query) ||
                               d.almacen?.toLowerCase().includes(query) ||
                               d.idProducto?.toString().includes(query);
                }
            });
        }

        this.datosPaginados.set(filtrados);
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarDatos();
    }

    cambiarPagina(page: any): void {
        this.currentPage.set(Number(page));
        this.cargarDatos();
    }

    cambiarTamanoPagina(size: any): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.cargarDatos();
    }

    calcularGranTotal(items: any[]): void {
        const total = items.reduce((acc, item) => acc + (item.valorTotal || 0), 0);
        this.totalInventario.set(total);
    }

    verDetalle(item: any): void {
        if (item && item.idProducto) {
            this.router.navigate(['/reportes/stock-valorizado', item.idProducto]);
        }
    }

    onSucursalChange(val: string): void {
        this.sucursalId.set(val);
        this.cargarDatos();
    }
}
