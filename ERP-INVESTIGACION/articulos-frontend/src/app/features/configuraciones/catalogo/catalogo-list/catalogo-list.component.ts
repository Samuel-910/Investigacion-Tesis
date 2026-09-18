import { Component, OnInit, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CatalogoFormComponent } from '../catalogo-form/catalogo-form.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { CompraService } from '../../../../features/compra/services/compra.service';
import { CatalogoResponse } from '../../models/catalogo.model';
import { CategoriaService } from '../../../almacen/service/atributo.service';
import { CatalogoService } from '../../services/catalogo.service';

@Component({
    selector: 'app-catalogo-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        CatalogoFormComponent,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        ModalComponent,
        BreadcrumbComponent,
        AdvancedFilterDrawerComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './catalogo-list.component.html'
})
export class CatalogoListComponent implements OnInit {
    items = signal<CatalogoResponse[]>([]);
    loading = signal(false);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Catálogo de Artículos' }
    ];

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');
    searchType = signal('');
    selectedFilter = signal('PRODUCTO');

    showModal = signal(false);
    selectedItem = signal<CatalogoResponse | null>(null);

    // Filtros
    idCategoria = signal<number | null>(null);
    esGenerico = signal<boolean | null>(null);
    manejaLotes = signal<boolean | null>(null);
    tipoAfectacion = signal<string | null>(null);
    estado = signal<string | null>(null);

    categorias = signal<any[]>([]);

    // Conteo de filtros activos
    activeFiltersCount = signal<number>(0);
    @ViewChild(AdvancedFilterDrawerComponent) filterDrawer!: AdvancedFilterDrawerComponent;

    categoriaOptions = computed(() => {
        return this.categorias().map(c => ({
            label: c.nombre || c.descripcion,
            value: c.id === null ? 'TODOS' : c.id
        }));
    });

    opcionesMarca = [
        { label: 'Todos', value: null },
        { label: 'Marca', value: false },
        { label: 'Genérico', value: true }
    ];

    // Historial
    historialProducto = signal<any[]>([]);
    showHistorialModal = signal(false);

    columns: Columna[] = [
        { field: 'codigo', header: 'Código', tipo: 'text', subField: [] },
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'categoria', header: 'Categoría', tipo: 'badge', subField: [] },
        { field: 'presentacion', header: 'Presentación', tipo: 'text', subField: [] },
        { field: 'precioKairos', header: 'P. Kairos', tipo: 'currency', subField: [] },
        { field: 'esGenerico', header: 'Genérico', tipo: 'sino-na', subField: [] },
        { field: 'manejaLotes', header: 'Lotes', tipo: 'sino-na', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' },
        { label: 'Código', value: 'CODIGO' },
        { label: 'Categoría', value: 'CATEGORIA' }
    ];

    constructor(
        private catalogoService: CatalogoService,
        private compraService: CompraService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private categoriaService: CategoriaService
    ) { }

    ngOnInit(): void {
        this.cargarCategorias();
        this.cargarItems();
    }

    cargarCategorias(): void {
        this.categoriaService.listar(0, 1000, '').subscribe((res: any) => {
            if (res.success && res.data) {
                this.categorias.set([{ id: null, nombre: 'Todas las Categorías' }, ...res.data.content]);
            }
        });
    }

    cargarItems(): void {
        this.loading.set(true);
        const catId = this.idCategoria() ?? undefined;
        const gen = this.esGenerico() !== null ? this.esGenerico() : undefined;
        const lotes = this.manejaLotes() !== null ? this.manejaLotes() : undefined;
        const est = this.estado() ?? undefined;
        const afec = this.tipoAfectacion() ?? undefined;

        const observable = this.searchTerm()
            ? this.catalogoService.buscar(this.searchTerm(), this.searchType(), this.selectedFilter(), this.currentPage(), this.pageSize(), catId, gen, lotes, est, afec)
            : this.catalogoService.listarTodos(this.currentPage(), this.pageSize(), this.selectedFilter(), catId, gen, lotes, est, afec);

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.items.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onGlobalSearch(term: string): void {
        this.searchTerm.set(term);
        this.searchType.set('ALL');
        this.currentPage.set(0);
        this.cargarItems();
    }

    onApplyFilters(filters: AdvancedFilters): void {
        this.idCategoria.set(filters.categoriaId !== 'TODOS' ? (filters.categoriaId as number) : null);
        this.esGenerico.set(filters.esGenerico !== 'TODOS' ? (filters.esGenerico === true || String(filters.esGenerico) === 'true') : null);
        this.manejaLotes.set(filters.manejaLotes !== 'TODOS' ? (filters.manejaLotes === true || String(filters.manejaLotes) === 'true') : null);
        this.tipoAfectacion.set(filters.tipoAfectacion !== 'TODOS' ? (filters.tipoAfectacion as string) : null);
        this.estado.set(filters.estado !== 'TODOS' ? (filters.estado as string) : null);

        this.currentPage.set(0);
        this.cargarItems();
    }

    onActiveFiltersCountChange(count: number): void {
        this.activeFiltersCount.set(count);
    }

    onFilterChange(): void {
        this.currentPage.set(0);
        this.cargarItems();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarItems();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0); // Resetear a la primera página
        this.cargarItems();
    }

    abrirModalNuevo(): void {
        this.selectedItem.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(item: CatalogoResponse): void {
        this.selectedItem.set(item);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedItem.set(null);
    }

    onGuardado(): void {
        this.selectedItem.set(null); // Limpiar para que el form se resetee
        this.showModal.set(false);
        this.alertService.toast('Artículo guardado correctamente', 'success');
        this.cargarItems();
    }

    eliminar(item: CatalogoResponse): void {
        const id = item.id;
        if (!id) {
            this.alertService.error('Error', 'No se pudo identificar el ID del artículo.');
            return;
        }

        this.alertService.confirm(
            '¿Eliminar artículo?',
            `Se eliminará "${item.nombre}" permanentemente del catálogo. Esta acción no se puede deshacer.`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.loading.set(true);
                this.catalogoService.eliminar(id).subscribe({
                    next: (response) => {
                        this.loading.set(false);
                        if (response.success) {
                            this.alertService.toast('Artículo eliminado correctamente', 'success');
                            this.cargarItems();
                        } else {
                            this.alertService.error('No se pudo eliminar', response.message || 'El servidor rechazó la solicitud.');
                        }
                    },
                    error: (err) => {
                        this.loading.set(false);
                        const techMsg = err.error?.message || '';
                        let friendlyMsg = 'No se pudo completar la operación. Por favor, intente más tarde.';

                        if (techMsg.includes('viola la llave foránea') || techMsg.includes('foreign key constraint')) {
                            friendlyMsg = 'No puede eliminar este artículo porque ya tiene **productos registrados o stock** vinculados en el almacén. Primero debe eliminar los productos asociados.';
                        } else if (techMsg) {
                            friendlyMsg = techMsg;
                        }

                        this.alertService.custom({
                            title: 'Operación no permitida',
                            html: `<div class="text-sm border-t border-slate-100 pt-3 mt-2">${friendlyMsg}</div>`,
                            icon: 'warning',
                            confirmButtonText: 'Entendido'
                        });
                    }
                });
            }
        });
    }
}
