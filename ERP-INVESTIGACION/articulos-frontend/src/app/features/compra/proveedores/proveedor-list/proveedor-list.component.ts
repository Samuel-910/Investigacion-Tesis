import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Proveedor } from '../../models/proveedor.model';
import { ProveedorService } from '../../services/proveedor.service';
import { ProveedorFormComponent } from '../proveedor-form/proveedor-form.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';

import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';

@Component({
    selector: 'app-proveedor-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ProveedorFormComponent,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PrimaryButtonComponent,
        ModalComponent,
        SearchGenericComponent,
        BreadcrumbComponent
    ],
    templateUrl: './proveedor-list.component.html'
})
export class ProveedorListComponent implements OnInit {
    proveedores = signal<Proveedor[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');
    searchType = signal('');

    showModal = signal(false);
    selectedProveedor = signal<Proveedor | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Compras', route: '/compra' },
        { label: 'Gestión de Proveedores' }
    ];

    // Design Playground State
    designFilterDropdownOpen = signal(false);
    designAdvancedFiltersOpen = signal(false);

    columns: Columna[] = [
        { field: 'tipoDocIdent', header: 'Tipo Doc', tipo: 'text', subField: [] },
        { field: 'numDocIdent', header: 'N° Documento', tipo: 'text', subField: [] },
        { field: 'razonSocial', header: 'Razón Social', tipo: 'text', subField: [] },
        { field: 'telefono', header: 'Teléfono', tipo: 'text', subField: [] },
        { field: 'email', header: 'Email', tipo: 'text', subField: [] },
        { field: 'distrito', header: 'Distrito', tipo: 'text', subField: [] },
        { field: 'saldo', header: 'Saldo Cuenta', tipo: 'currency-status', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];
    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Razón Social', value: 'RAZON_SOCIAL' },
        { label: 'Documento', value: 'DOCUMENTO' },
        { label: 'Teléfono', value: 'TELEFONO' },
        { label: 'Email', value: 'EMAIL' },
        { label: 'Distrito', value: 'DISTRITO' }
    ];
    constructor(
        private proveedorService: ProveedorService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.cargarProveedores();
    }

    cargarProveedores(): void {
        this.loading.set(true);
        const observable = this.searchTerm()
            ? this.proveedorService.buscar(this.searchTerm(), this.searchType(), this.currentPage(), this.pageSize())
            : this.proveedorService.listarTodos(this.currentPage(), this.pageSize());

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.proveedores.set(res.data.content);
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

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarProveedores();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarProveedores();
    }

    abrirModalNuevo(): void {
        this.selectedProveedor.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(proveedor: Proveedor): void {
        this.selectedProveedor.set(proveedor);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedProveedor.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Proveedor guardado correctamente', 'success');
        this.cargarProveedores();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarProveedores();
    }

    irACuenta(proveedor: Proveedor): void {
        if (proveedor.id) {
            this.router.navigate(['/compra/proveedores', proveedor.id, 'cuenta']);
        }
    }

    eliminar(proveedor: Proveedor): void {
        if (!proveedor.id) return;
        this.alertService.confirm(
            '¿Eliminar proveedor?',
            `Se eliminará "${proveedor.razonSocial}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.proveedorService.eliminar(proveedor.id!).subscribe(() => {
                    this.alertService.toast('Proveedor eliminado', 'success');
                    this.cargarProveedores();
                });
            }
        });
    }
}
