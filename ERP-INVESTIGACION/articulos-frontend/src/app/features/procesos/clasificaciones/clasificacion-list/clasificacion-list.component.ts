import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Clasificacion } from '../../../procesos/models/clasificacion.model';
import { ClasificacionService } from '../../../procesos/service/clasificacion.service';
import { ClasificacionFormComponent } from '../clasificacion-form/clasificacion-form.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertService } from '../../../../core/services/alert.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-clasificacion-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ClasificacionFormComponent,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PrimaryButtonComponent,
        ModalComponent,
        SearchGenericComponent,
        BreadcrumbComponent,
        PaginationComponent
    ],
    templateUrl: './clasificacion-list.component.html'
})
export class ClasificacionListComponent implements OnInit {
    clasificaciones = signal<Clasificacion[]>([]);
    loading = signal(false);

    searchTerm = signal('');
    searchType = signal('ALL');

    // Paginación
    page = signal(0);
    size = signal(10);
    totalPages = signal(0);
    totalElements = signal(0);

    showModal = signal(false);
    selectedClasificacion = signal<Clasificacion | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Clasificaciones de Movimiento' }
    ];

    columns: Columna[] = [
        { field: 'N°', header: 'N°', tipo: 'index', subField: [] },
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        {
            field: 'tipo',
            header: 'Tipo Movimiento',
            tipo: 'badge',
            subField: [],
            badgeConfig: {
                from: 'from-blue-50',
                to: 'to-indigo-50',
                text: 'text-indigo-700',
                border: 'border-indigo-200'
            }
        },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' }
    ];

    constructor(
        private clasificacionService: ClasificacionService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarClasificaciones();
    }

    cargarClasificaciones(): void {
        this.loading.set(true);
        const tipoFinal = (this.searchType() !== 'ALL' && this.searchType() !== 'NOMBRE') ? this.searchType() : undefined;
        this.clasificacionService.listar(
            tipoFinal,
            this.searchTerm(),
            this.page(),
            this.size()
        ).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.clasificaciones.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al cargar las clasificaciones');
            }
        });
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.cargarClasificaciones();
    }

    onPageSizeChange(size: number): void {
        this.size.set(size);
        this.page.set(0); // Reset a primera página
        this.cargarClasificaciones();
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.page.set(0); // Reset a primera página al buscar
        this.cargarClasificaciones();
    }

    abrirModalNuevo(): void {
        this.selectedClasificacion.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(clasificacion: Clasificacion): void {
        this.selectedClasificacion.set(clasificacion);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedClasificacion.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Clasificación guardada correctamente', 'success');
        this.cargarClasificaciones();
    }

    eliminar(clasificacion: Clasificacion): void {
        if (!clasificacion.id) return;
        this.alertService.confirm(
            '¿Eliminar clasificación?',
            `Se eliminará "${clasificacion.nombre}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.clasificacionService.eliminar(clasificacion.id!).subscribe(() => {
                    this.alertService.toast('Clasificación eliminada', 'success');
                    this.cargarClasificaciones();
                });
            }
        });
    }
}
