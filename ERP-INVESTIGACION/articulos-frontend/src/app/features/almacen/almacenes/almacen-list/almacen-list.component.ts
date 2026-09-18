import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlmacenService } from '../../service/almacen.service';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { Almacen } from '../../models/almacen.model';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';

import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { AlmacenFormComponent } from '../almacen-form/almacen-form.component';

@Component({
    selector: 'app-almacen-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        ModalComponent,
        SearchGenericComponent,
        PaginationComponent,
        PageHeaderComponent,
        AlmacenFormComponent
    ],
    templateUrl: './almacen-list.component.html'
})
export class AlmacenListComponent implements OnInit {
    almacenes = signal<Almacen[]>([]);
    loading = signal(false);

    // Pagination & Search
    currentPage = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');
    searchType = signal('ALL');

    showModal = signal(false);
    selectedAlmacen = signal<Almacen | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Gestión de Almacenes' }
    ];

    columns: Columna[] = [
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'codigo', header: 'Código', tipo: 'badge', subField: [] },
        { field: 'ubicacion', header: 'Ubicación', tipo: 'text', subField: [] },
        { field: 'esPrincipal', header: 'Principal', tipo: 'status', subField: [] },
        { field: 'responsable', header: 'Responsable', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' },
        { label: 'Código', value: 'CODIGO' },
        { label: 'Responsable', value: 'RESPONSABLE' }
    ];

    almacenesFiltrados = computed(() => {
        const query = this.searchTerm().toLowerCase().trim();
        const type = this.searchType();
        const data = this.almacenes();

        if (!query) return data;

        return data.filter(a => {
            switch (type) {
                case 'NOMBRE': return a.nombre.toLowerCase().includes(query);
                case 'CODIGO': return a.codigo?.toLowerCase().includes(query);
                case 'RESPONSABLE': return a.responsable?.toLowerCase().includes(query);
                default:
                    return a.nombre.toLowerCase().includes(query) ||
                        a.codigo?.toLowerCase().includes(query) ||
                        a.responsable?.toLowerCase().includes(query);
            }
        });
    });

    totalElements = computed(() => this.almacenesFiltrados().length);
    totalPages = computed(() => Math.ceil(this.totalElements() / this.pageSize()));

    paginatedAlmacenes = computed(() => {
        const start = this.currentPage() * this.pageSize();
        const end = start + this.pageSize();
        return this.almacenesFiltrados().slice(start, end);
    });

    constructor(
        private almacenService: AlmacenService,
        private alertService: AlertService,
        private authService: AuthService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.loadAlmacenes();
    }

    loadAlmacenes(): void {
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (!sucursalId) return;

        this.loading.set(true);
        this.almacenService.getBySucursal(sucursalId).subscribe({
            next: (res: any) => {
                const resData = res.data || res;
                const data = Array.isArray(resData) ? resData : (resData?.content ? resData.content : []);
                this.almacenes.set(data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar almacenes', 'error');
            }
        });
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
    }

    abrirModalNuevo(): void {
        this.selectedAlmacen.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(almacen: Almacen): void {
        this.selectedAlmacen.set(almacen);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedAlmacen.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Almacén guardado correctamente', 'success');
        this.loadAlmacenes();
    }

    eliminar(almacen: Almacen): void {
        this.alertService.confirm(
            '¿Eliminar almacén?',
            `Se eliminará "${almacen.nombre}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.almacenService.delete(almacen.id).subscribe({
                    next: () => {
                        this.alertService.toast('Almacén eliminado', 'success');
                        this.loadAlmacenes();
                    },
                    error: () => this.alertService.toast('Error al eliminar almacén', 'error')
                });
            }
        });
    }
}
