import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { NuevoDocumentoModalComponent } from './modals/nuevo-documento-modal.component';
import { CopiarDocumentoModalComponent } from './modals/copiar-documento-modal.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { Plantilla } from '../../models/documento.model';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { DocumentoService } from '../../services/documento.service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';

@Component({
    selector: 'app-documento-lista',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        ModalComponent,
        NuevoDocumentoModalComponent,
        CopiarDocumentoModalComponent,
        PaginationComponent,
        SearchGenericComponent
    ],
    templateUrl: './documento-lista.component.html'
})
export class DocumentoListaComponent implements OnInit {
    plantillas = signal<Plantilla[]>([]);
    loading = signal(false);
    searchTerm = signal('');
    searchType = signal('ALL');

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NAME' },
        { label: 'Formato', value: 'FORMAT' },
        { label: 'Módulo', value: 'MODULE' }
    ];

    plantillasFiltradas = computed(() => {
        const term = this.searchTerm().toLowerCase().trim();
        const type = this.searchType();
        const items = this.plantillas();
        if (!term) return items;

        return items.filter(p => {
            const nombreTipo = this.getNombreTipo(p);
            const nameMatch = p.nombre?.toLowerCase().includes(term);
            const formatMatch = p.formato?.nombre?.toLowerCase().includes(term);
            const moduleMatch = p.modulo?.toLowerCase().includes(term);
            const typeMatch = nombreTipo?.toLowerCase().includes(term);

            if (type === 'NAME') return nameMatch;
            if (type === 'FORMAT') return formatMatch;
            if (type === 'MODULE') return moduleMatch;

            return nameMatch || formatMatch || moduleMatch || typeMatch;
        });
    });

    paginaActual = signal(0);
    tamanoPagina = signal(10);
    totalRegistros = computed(() => this.plantillasFiltradas().length);
    totalPages = computed(() => Math.ceil(this.totalRegistros() / this.tamanoPagina()));

    pagedPlantillas = computed(() => {
        const start = this.paginaActual() * this.tamanoPagina();
        const end = start + this.tamanoPagina();
        return this.plantillasFiltradas().slice(start, end);
    });

    onSearch(event: any): void {
        this.searchTerm.set(event.q || '');
        this.searchType.set(event.type || 'ALL');
        this.paginaActual.set(0);
    }

    cambiarPagina(pagina: number): void {
        this.paginaActual.set(pagina);
    }

    cambiarTamanoPagina(tamano: number): void {
        this.tamanoPagina.set(Number(tamano));
        this.paginaActual.set(0);
    }

    getNombreTipo(p: Plantilla): string {
        if (!p.tipoDocumento) return '--';
        return typeof p.tipoDocumento === 'object' ? (p.tipoDocumento as any).nombre : p.tipoDocumento;
    }

    showModalPreview = signal(false);
    previewContent: SafeHtml | null = null;
    previewTitle = signal('');

    showModalNuevo = signal(false);
    showModalCopiar = signal(false);
    plantillaACopiar = signal<Plantilla | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Documentos', route: '/documentos' },
        { label: 'Mis Plantillas' }
    ];

    private sanitizer = inject(DomSanitizer);

    constructor(
        private documentoService: DocumentoService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.cargarPlantillas();
    }

    cargarPlantillas(): void {
        this.loading.set(true);
        this.documentoService.listarPlantillas().subscribe({
            next: (res: any) => {
                const data = res.data;
                this.plantillas.set(data?.content ? data.content : (data || []));
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    irAEditor(plantilla?: Plantilla): void {
        if (plantilla) {
            this.router.navigate(['/documentos/editor', plantilla.id]);
        } else {
            this.plantillaACopiar.set(null);
            this.showModalNuevo.set(true);
        }
    }

    copiarDiseno(plantilla: Plantilla): void {
        this.plantillaACopiar.set(plantilla);
        this.showModalCopiar.set(true);
    }

    onConfirmarNuevaPlantilla(ev: any): void {
        this.showModalNuevo.set(false);
        this.showModalCopiar.set(false);
        this.router.navigate(['/documentos/editor'], {
            queryParams: {
                nuevo: true,
                nombre: ev.nombre,
                modulo: ev.modulo,
                tipo: ev.tipoDocumento,
                formatoId: ev.formatoId,
                orientacion: ev.orientacion,
                copyId: ev.copyId
            }
        });
    }

    eliminar(plantilla: Plantilla): void {
        if (!plantilla.id) return;
        this.alertService.confirm(
            '¿Eliminar plantilla?',
            `Se eliminará "${plantilla.nombre}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.documentoService.eliminarPlantilla(plantilla.id!).subscribe(() => {
                    this.alertService.toast('Plantilla eliminada', 'success');
                    this.cargarPlantillas();
                });
            }
        });
    }

    generarEjemplo(plantilla: Plantilla): void {
        const datosPrueba = {
            proveedor: {
                razonSocial: 'PROVEEDOR DE PRUEBA S.A.',
                numDocIdent: '20123456789'
            },
            documento: {
                fecha: new Date().toLocaleDateString(),
                montoTotal: '1,500.00'
            }
        };

        this.documentoService.generarPdf(plantilla.id!, datosPrueba).subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            window.open(url);
        });
    }

    abrirPrevisualizacion(plantilla: Plantilla): void {
        this.previewTitle.set(plantilla.nombre);

        const htmlAServir = (plantilla.htmlTraducido && plantilla.htmlTraducido.length > 0)
            ? plantilla.htmlTraducido
            : plantilla.htmlContenido;

        const fullHtml = `
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    ${plantilla.cssEstilo}
                    body { background-color: white; padding: 20px; }
                </style>
            </head>
            <body>
                ${htmlAServir}
            </body>
            </html>
        `;

        this.previewContent = this.sanitizer.bypassSecurityTrustHtml(
            `<iframe srcdoc="${fullHtml.replace(/"/g, '&quot;')}" style="width: 100%; height: 600px; border: none;"></iframe>`
        );
        this.showModalPreview.set(true);
    }

    cerrarModalPreview(): void {
        this.showModalPreview.set(false);
        this.previewContent = null;
    }
}
