import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { KardexService } from '../../../almacen/service/kardex.service';
import { RegArticuloKardex } from '../../../almacen/models/kardex.model';
import { AlertService } from '../../../../core/services/alert.service';
import { ProductoService } from '../../../almacen/service/producto.service';
import { CatalogoService } from '../../../configuraciones/services/catalogo.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { AuthService } from '../../../auth/services/auth.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { Producto } from '../../../almacen/models/producto.model';
import { computed, effect } from '@angular/core';

@Component({
    selector: 'app-kardex-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        SearchableSelectComponent,
        PrimaryButtonComponent,
        PaginationComponent,
        FormInputComponent
    ],
    templateUrl: './kardex-list.component.html',
})
export class KardexListComponent implements OnInit {
    sucursalId = signal<string | null>(null);
    sucursalOptions = signal<any[]>([]);

    private kardexService = inject(KardexService);
    private productoService = inject(ProductoService);
    private catalogoService = inject(CatalogoService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    private sucursalService = inject(SucursalService);
    public sidebarService = inject(SidebarService);

    // Filtros
    // Filtros con fecha local (no UTC) para evitar problemas de desfase horario
    fechaDesde = signal<string>(new Date().toLocaleDateString('en-CA'));
    fechaHasta = signal<string>(new Date().toLocaleDateString('en-CA'));
    idCatalogo = signal<number | null>(null);

    catalogos = signal<any[]>([]);

    // Datos
    movimientos = signal<RegArticuloKardex[]>([]);
    productosLote = signal<Producto[]>([]);
    loading = signal<boolean>(false);

    totalStock = computed(() => {
        return this.productosLote().reduce((acc, p) => acc + (p.stock || 0), 0);
    });

    isDetailCollapsed = signal<boolean>(false);

    esArticuloSinLote = computed(() => {
        const lotes = this.productosLote();
        return lotes.length > 0 && lotes.every(p => !p.nroLote || p.nroLote === 'SIN LOTE');
    });

    top3Lotes = computed(() => {
        return [...this.productosLote()]
            .sort((a, b) => (b.stock || 0) - (a.stock || 0))
            .slice(0, 3);
    });

    averageCostoUnitario = computed(() => {
        const lotes = this.productosLote();
        if (lotes.length === 0) return 0;
        return Math.max(...lotes.map(p => p.precioCompra || 0));
    });

    // Paginación
    currentPage = signal<number>(0);
    totalPages = signal<number>(0);
    totalElements = signal<number>(0);
    pageSize = signal(10);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Consultas', route: '/consultas' },
        { label: 'Kardex de Artículos' }
    ];

    constructor() {
        // Auto-colapsar detalle si es artículo sin lote (opcional, pero ayuda a la limpieza visual)
        effect(() => {
            if (this.esArticuloSinLote()) {
                // this.isDetailCollapsed.set(true); 
                // Mejor dejar que el usuario decida o que se muestre horizontalmente por defecto
            }
        });
    }

      ngOnInit() {
    const tokenSuc = this.authService.getSucursalIdFromToken();
    if (tokenSuc) {
      this.sucursalId.set(tokenSuc.toString());
      this.cargarDatosIniciales();
    }
    this.cargarSucursales();
  }

    cargarDatosIniciales() {
        // Cargamos el catálogo completo para el buscador (nombre de artículos)
        this.catalogoService.listarTodos(0, 2000, 'PRODUCTO').subscribe(res => {
            if (res.success) {
                this.catalogos.set(res.data.content);
            }
        });
    }

    setTab(tab: string) {
        // Obsoleto
    }

    buscar() {
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (!sucursalId) {
            this.alertService.error('Error', 'No se pudo obtener la sucursal');
            return;
        }

        if (!this.idCatalogo()) {
            this.alertService.warning('Atención', 'Debe seleccionar un artículo del catálogo');
            return;
        }

        this.loading.set(true);
        const catId = this.idCatalogo()!;

        // 1. Cargar Movimientos
        this.kardexService.getMovimientos(
            catId,
            sucursalId,
            this.currentPage(),
            this.pageSize(),
            this.fechaDesde(),
            this.fechaHasta()
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    this.movimientos.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.alertService.error('Error', 'Error al cargar el kardex');
                this.loading.set(false);
            }
        });

        // 2. Cargar Stock Real por Lotes (Resumen)
        this.productoService.listarPorCatalogo(catId).subscribe(res => {
            if (res.success) {
                // Filtrar solo los productos de la sucursal actual
                const filtered = res.data.filter(p => p.idSucursal === sucursalId);
                this.productosLote.set(filtered);
            }
        });
    }

    cambiarPagina(page: number) {
        this.currentPage.set(page);
        this.buscar();
    }

    exportarExcel() {
        const sucursalId = this.authService.getSucursalIdFromToken();
        const catId = this.idCatalogo();

        if (!sucursalId || !catId) return;

        this.loading.set(true);
        this.kardexService.exportarExcel(
            catId,
            sucursalId,
            this.fechaDesde(),
            this.fechaHasta()
        ).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Kardex_${catId}_${new Date().getTime()}.xlsx`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.loading.set(false);
                this.alertService.toast('Reporte Excel generado correctamente', 'success');
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudo generar el reporte Excel');
            }
        });
    }

    onSucursalChange(val: string): void {
        this.sucursalId.set(val);
        // Maybe reload
    }

    cargarSucursales(): void {
        this.sucursalService.getActivas().subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const mapped = res.data.map((s: any) => ({
                        label: s.nombreSucursal,
                        value: s.idSucursal.toString()
                    }));
                    this.sucursalOptions.set(mapped);
                }
            }
        });
    }
}
