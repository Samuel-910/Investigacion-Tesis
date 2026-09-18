import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { AlertService } from '../../../../core/services/alert.service';
import { Sucursal } from '../../models/sucursal.model';
import { Columna, TablaGeneralComponent } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { SucursalFormComponent } from '../sucursal-form/sucursal-form.component';
import { ModalComponent } from '../../../../shared/components/modal/modal';

@Component({
    selector: 'app-sucursal-list',
    standalone: true,
    imports: [
        CommonModule,
        TablaGeneralComponent,
        PrimaryButtonComponent,
        HeaderComponent,
        SidebarComponent,
        SucursalFormComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        PaginationComponent,
        SearchGenericComponent,
        ModalComponent
    ],
    templateUrl: './sucursal-list.component.html'
})
export class SucursalListComponent implements OnInit {
    sucursales = signal<Sucursal[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    pageSize = signal(10);
    totalPages = signal(0);
    totalElements = signal(0);
    searchTerm = signal('');
    searchType = signal('ALL');
    showModal = signal(false);
    selectedSucursal = signal<Sucursal | null>(null);

    breadcrumbItems = [
        { label: 'Inicio', route: '/inicio' },
        { label: 'Configuraciones', route: '/configuracion' },
        { label: 'Sucursales' }
    ];

    columns: Columna[] = [
        { field: 'idSucursal', header: 'ID', tipo: 'index', subField: [] },
        { field: 'nombreSucursal', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'telefono', header: 'Teléfono', tipo: 'text', subField: [] },
        { field: 'direccion', header: 'Dirección', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] },
        { field: 'fechaCreacion', header: 'F. Creación', tipo: 'date', subField: [] },
        { field: 'fechaActualizacion', header: 'F. Actualización', tipo: 'date', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' },
        { label: 'Teléfono', value: 'TELEFONO' },
        { label: 'Dirección', value: 'DIRECCION' }
    ];

    constructor(
        private sucursalService: SucursalService,
        public sidebarService: SidebarService,
        private alertService: AlertService
    ) { }

    ngOnInit() {
        this.cargarSucursales();
    }

    cargarSucursales() {
        this.loading.set(true);
        const request = this.searchTerm()
            ? this.sucursalService.buscar(this.currentPage(), this.pageSize(), this.searchTerm(), this.searchType())
            : this.sucursalService.getAll(this.currentPage(), this.pageSize());

        request.subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.sucursales.set(response.data.content);
                    this.totalElements.set(response.data.totalElements);
                    this.totalPages.set(response.data.totalPages);
                } else {
                    this.alertService.error('Error al cargar sucursales');
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.alertService.error('Error de conexión');
                this.loading.set(false);
            }
        });
    }

    handleSearch(event: any) {
        this.searchTerm.set(event.q || '');
        this.searchType.set(event.type || 'ALL');
        this.currentPage.set(0);
        this.cargarSucursales();
    }

    onPageChange(page: number) {
        this.currentPage.set(page);
        this.cargarSucursales();
    }

    onPageSizeChange(size: number) {
        this.pageSize.set(size);
        this.currentPage.set(0); // Reset to first page
        this.cargarSucursales();
    }

    abrirModalNuevo() {
        this.selectedSucursal.set(null);
        this.showModal.set(true);
    }

    editar(sucursal: Sucursal) {
        this.selectedSucursal.set(sucursal);
        this.showModal.set(true);
    }

    eliminar(sucursal: Sucursal) {
        this.alertService.confirm(
            '¿Estás seguro?',
            `Se eliminará la sucursal: ${sucursal.nombreSucursal}`,
            'Sí, eliminar',
            'Cancelar'
        ).then((result) => {
            if (result.isConfirmed) {
                this.sucursalService.eliminar(sucursal.idSucursal).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.toast('La sucursal ha sido eliminada.', 'success');
                            this.cargarSucursales();
                        } else {
                            this.alertService.error(res.message || 'No se pudo eliminar');
                        }
                    },
                    error: (err) => {
                        this.alertService.error('Error al eliminar');
                    }
                });
            }
        });
    }

    cerrarModal() {
        this.showModal.set(false);
        this.selectedSucursal.set(null);
    }

    guardarSucursal(event: any) {
        this.cerrarModal();
        this.cargarSucursales();
        this.alertService.toast('Operación realizada con éxito', 'success');
    }
}
