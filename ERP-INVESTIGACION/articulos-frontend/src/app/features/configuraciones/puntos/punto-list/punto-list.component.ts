import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../auth/services/auth.service';
import { PuntoService } from '../../services/punto.service';
import { AlertService } from '../../../../core/services/alert.service';
import { Router } from '@angular/router';
import { Punto } from '../../models/punto.model';
import { Columna, TablaGeneralComponent } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PuntoFormComponent } from '../punto-form/punto-form.component'; import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { ModalComponent } from "../../../../shared/components/modal/modal";
import { PuntoDocumentoService, PuntoDocumento } from '../../../documentos/services/punto-documento.service';
import { PuntoDocumentoFormComponent } from '../../../documentos/punto-documento/punto-documento-form.component';

@Component({
    selector: 'app-punto-list',
    standalone: true,
    imports: [
        CommonModule,
        PrimaryButtonComponent,
        HeaderComponent,
        SidebarComponent,
        PuntoFormComponent,
        TablaGeneralComponent,
        PaginationComponent,
        SearchGenericComponent,
        BreadcrumbComponent,
        PageHeaderComponent,
        ModalComponent,
        PuntoDocumentoFormComponent
    ],
    templateUrl: './punto-list.component.html'
})
export class PuntoListComponent implements OnInit {
    puntos = signal<Punto[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    totalElements = signal(0);
    totalPages = signal(0);
    currentPage = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');
    searchType = signal('ALL');

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/inicio' },
        { label: 'Configuraciones', route: '/configuracion' },
        { label: 'Puntos de Venta' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' },
        { label: 'Tipo', value: 'TIPO' },
        { label: 'Proceso', value: 'PROCESO' }
    ];

    showModal = signal(false);
    selectedPunto = signal<Punto | null>(null);

    // Modal Documentos
    showDocumentosModal = signal(false);
    loadingDocumentos = signal(false);
    documentosPunto = signal<PuntoDocumento[]>([]);
    documentosColumns: Columna[] = [
        { field: 'tipoDocumentoNombre', header: 'Documento', tipo: 'text', subField: [] },
        { field: 'serie', header: 'Serie', tipo: 'badge', badgeConfig: { from: 'from-pink-500', to: 'to-pink-600', text: 'text-white', border: 'border-pink-200' }, subField: [] },
        { field: 'numeroFormat', header: 'Número', tipo: 'text', subField: [] },
        { field: 'modulo', header: 'Módulo', tipo: 'badge', subField: [] },
        { field: 'plantillaNombre', header: 'Plantilla', tipo: 'text', subField: [] }
    ];
    
    showDocFormModal = signal(false);
    selectedDoc = signal<PuntoDocumento | null>(null);

    columns: Columna[] = [
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'nombreAlmacen', header: 'Almacén', tipo: 'text', subField: [] }, { field: 'nombreTipo', header: 'Tipo', tipo: 'badge', subField: [] },
        { field: 'nombreProceso', header: 'Proceso', tipo: 'badge', subField: [] },
        { field: 'ipAccesoModulo', header: 'IP Acceso', tipo: 'text', subField: [] },
        { field: 'valido', header: 'Estado', tipo: 'status', subField: [] }
    ];

    idSucursal: number = 0;

    private puntoService = inject(PuntoService);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    public sidebarService = inject(SidebarService);
    private authService = inject(AuthService);
    private alertService = inject(AlertService);
    private router = inject(Router);

    constructor() {
        this.idSucursal = this.authService.getSucursalIdFromToken() || 0;
    }

    get userPunto(): string | null {
        return localStorage.getItem('puntoNombre');
    }

    get userSucursal(): string | null {
        return this.authService.getSucursalFromToken();
    }

    ngOnInit() {
        this.cargarPuntos();
    }

    cargarPuntos(page: number = 0) {
        this.loading.set(true);
        this.currentPage.set(page);

        const term = this.searchTerm().trim();
        const type = this.searchType();

        const request = (term !== '' || type !== 'ALL')
            ? this.puntoService.buscar(term, this.idSucursal, page, this.pageSize(), type)
            : this.puntoService.listarTodos(this.idSucursal, page, this.pageSize());
        request.subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.puntos.set(response.data.content);
                    this.totalElements.set(response.data.totalElements);
                    this.totalPages.set(response.data.totalPages);
                } else {
                    this.error.set(response.message || 'Error al cargar puntos');
                    this.puntos.set([]);
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set('Error al cargar puntos de venta');
                this.loading.set(false);
            }
        });
    }

    handleSearch(event: { q: string, type: string }) {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.cargarPuntos(0);
    }

    onPageChange(page: number) {
        this.cargarPuntos(page);
    }

    onPageSizeChange(size: number) {
        this.pageSize.set(size);
        this.cargarPuntos(0);
    }

    abrirModalNuevo() {
        this.selectedPunto.set(null);
        this.showModal.set(true);
    }

    editar(punto: Punto) {
        this.selectedPunto.set(punto);
        this.showModal.set(true);
    }

    verDocumentos(punto: Punto) {
        this.selectedPunto.set(punto);
        this.showDocumentosModal.set(true);
        this.loadingDocumentos.set(true);
        this.puntoDocumentoService.obtenerPorPunto(punto.punto).subscribe({
            next: (res) => {
                if (res.success) {
                    const formattedData = (res.data || []).map((doc: any) => ({
                        ...doc,
                        numeroFormat: doc.numero != null ? doc.numero.toString().padStart(7, '0') : '0000000'
                    }));
                    this.documentosPunto.set(formattedData);
                } else {
                    this.documentosPunto.set([]);
                }
                this.loadingDocumentos.set(false);
            },
            error: () => {
                this.loadingDocumentos.set(false);
                this.alertService.toast('Error al cargar documentos del punto', 'error');
            }
        });
    }

    nuevoDocumento() {
        this.selectedDoc.set(null);
        this.showDocFormModal.set(true);
    }

    editarDocumento(doc: any) {
        this.selectedDoc.set(doc);
        this.showDocFormModal.set(true);
    }

    eliminarDocumento(doc: any) {
        this.alertService.confirm('¿Eliminar documento?', 'Esta acción no se puede deshacer').then((result) => {
            if (result.isConfirmed) {
                this.puntoDocumentoService.eliminar(doc.id).subscribe({
                    next: () => {
                        this.alertService.toast('Documento eliminado', 'success');
                        this.verDocumentos(this.selectedPunto()!);
                    }
                });
            }
        });
    }

    onGuardarDocumento() {
        this.showDocFormModal.set(false);
        this.verDocumentos(this.selectedPunto()!);
    }

    eliminar(punto: Punto) {
        this.alertService.confirm('¿Estás seguro?', 'No podrás revertir esta acción').then((result) => {
            if (result.isConfirmed) {
                this.puntoService.eliminar(punto.punto).subscribe({
                    next: () => {
                        this.alertService.toast('El punto ha sido eliminado.', 'success');
                        this.cargarPuntos(this.currentPage());
                    },
                    error: (err) => {
                        this.alertService.toast('No se pudo eliminar el punto.', 'error');
                    }
                });
            }
        });
    }

    cerrarModal() {
        this.showModal.set(false);
        this.selectedPunto.set(null);
    }

    onGuardar() {
        this.cerrarModal();
        this.cargarPuntos(this.currentPage());
        this.alertService.toast('Operación realizada con éxito', 'success');
    }
}
