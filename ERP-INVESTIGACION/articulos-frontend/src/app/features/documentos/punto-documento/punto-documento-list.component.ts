import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PuntoDocumentoService, PuntoDocumento } from '../services/punto-documento.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../core/services/alert.service';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { PuntoDocumentoFormComponent } from './punto-documento-form.component';
import { AuthService } from '../../auth/services/auth.service';
import { PuntoService } from '../../configuraciones/services/punto.service';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-punto-documento-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        ModalComponent,
        PuntoDocumentoFormComponent,
        PrimaryButtonComponent,
        SearchGenericComponent,
        PageHeaderComponent,
        PaginationComponent
    ],
    templateUrl: './punto-documento-list.component.html'
})
export class PuntoDocumentoListComponent implements OnInit {
    currentPage = signal(0);
    pageSize = signal(10);
    totalElements = signal(0);
    totalPages = signal(0);
    documentosPaginados = signal<PuntoDocumento[]>([]);

    loading = signal(false);
    showModal = signal(false);
    documentoSeleccionado: PuntoDocumento | null = null;
    puntoId = signal<number | null>(null);
    puntoNombre = signal<string>('');

    searchTerm = signal('');
    searchType = signal('ALL');

    private documentosRaw: PuntoDocumento[] = [];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Documento', value: 'DOCUMENTO' },
        { label: 'Plantilla', value: 'PLANTILLA' },
        { label: 'Formato', value: 'FORMATO' },
        { label: 'Orientación', value: 'ORIENTACION' },
        { label: 'Tipo', value: 'TIPO' }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Documentos', route: '/documentos' },
        { label: 'Punto de Emisión' }
    ];

    columns: Columna[] = [
        { field: 'tipoDocumentoNombre', header: 'Documento', tipo: 'text', subField: [] },
        { field: 'serie', header: 'Serie', tipo: 'text', subField: [], badgeConfig: { from: 'text-pink-500', to: 'text-pink-500', text: 'font-bold' } },
        { field: 'numeroFormat', header: 'Número', tipo: 'text', subField: [] },
        { field: 'plantillaNombre', header: 'Plantilla', tipo: 'text', subField: [] },
        { field: 'plantillaFormato', header: 'Formato', tipo: 'text', subField: [] },
        { field: 'plantillaOrientacion', header: 'Orientación', tipo: 'text', subField: [] },
        { field: 'modulo', header: 'Tipo', tipo: 'text', subField: [] },
        { field: 'ip', header: 'Ip', tipo: 'text', subField: [] }
    ];

    constructor(
        private puntoDocumentoService: PuntoDocumentoService,
        private alertService: AlertService,
        private authService: AuthService,
        private puntoService: PuntoService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    cargarDatos(): void {
        const sucursalId = this.authService.getSucursalIdFromToken();
        const puntoId = this.authService.getPuntoIdFromToken();
        const puntoNombre = this.authService.getPuntoFromToken();

        if (!sucursalId || !puntoId) {
            this.alertService.toast('No se pudo identificar el punto de emisión actual', 'error');
            return;
        }

        this.loading.set(true);
        this.puntoId.set(puntoId);
        this.puntoNombre.set(puntoNombre || `Punto ${puntoId}`);
        this.cargarItems();
    }

    cargarItems(): void {
        const puntoId = this.puntoId();
        if (!puntoId) return;

        this.loading.set(true);
        this.puntoDocumentoService.obtenerTodos(this.currentPage(), this.pageSize(), puntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const formattedData = (res.data.content || []).map((doc: any) => ({
                        ...doc,
                        numeroFormat: doc.numero != null ? doc.numero.toString().padStart(7, '0') : '0000000'
                    }));
                    this.documentosRaw = formattedData;
                    
                    const query = this.searchTerm().toLowerCase().trim();
                    if (query) {
                        const filtrados = this.documentosRaw.filter(d => {
                            const type = this.searchType();
                            switch (type) {
                                case 'DOCUMENTO': return d.tipoDocumentoNombre?.toLowerCase().includes(query);
                                case 'PLANTILLA': return d.plantillaNombre?.toLowerCase().includes(query);
                                case 'FORMATO': return d.plantillaFormato?.toLowerCase().includes(query);
                                case 'ORIENTACION': return d.plantillaOrientacion?.toLowerCase().includes(query);
                                case 'TIPO': return d.modulo?.toLowerCase().includes(query);
                                default:
                                    return d.tipoDocumentoNombre?.toLowerCase().includes(query) ||
                                        d.serie?.toLowerCase().includes(query) ||
                                        d.numero?.toString().includes(query) ||
                                        d.plantillaNombre?.toLowerCase().includes(query) ||
                                        d.modulo?.toLowerCase().includes(query);
                            }
                        });
                        this.documentosPaginados.set(filtrados);
                        this.totalElements.set(filtrados.length);
                        this.totalPages.set(Math.ceil(filtrados.length / this.pageSize()));
                    } else {
                        this.documentosPaginados.set(formattedData);
                        this.totalElements.set(res.data.totalElements || 0);
                        this.totalPages.set(res.data.totalPages || 0);
                    }
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar configuración del punto', 'error');
            }
        });
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarItems();
    }

    cambiarPagina(page: any): void {
        this.currentPage.set(Number(page));
        this.cargarItems();
    }

    cambiarTamanoPagina(size: any): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.cargarItems();
    }

    abrirModalNuevo(): void {
        this.documentoSeleccionado = null;
        this.showModal.set(true);
    }

    abrirModalEditar(doc: PuntoDocumento): void {
        this.documentoSeleccionado = doc;
        this.showModal.set(true);
    }

    guardarDocumento(documento: Partial<PuntoDocumento>): void {
        this.alertService.toast('Configuración guardada correctamente', 'success');
        this.showModal.set(false);
        this.cargarDatos();
    }

    eliminar(doc: PuntoDocumento): void {
        this.alertService.confirm('¿Está seguro de eliminar esta asignación?', 'confirm').then(res => {
            if (res.isConfirmed) {
                this.puntoDocumentoService.eliminar(doc.id).subscribe({
                    next: () => {
                        this.alertService.toast('Asignación eliminada', 'success');
                        this.cargarDatos();
                    },
                    error: () => this.alertService.toast('Error al eliminar', 'error')
                });
            }
        });
    }
}
