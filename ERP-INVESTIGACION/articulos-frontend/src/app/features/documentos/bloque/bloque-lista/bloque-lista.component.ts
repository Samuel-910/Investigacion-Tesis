import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Bloque } from '../../models/documento.model';
import { DocumentoService } from '../../services/documento.service';
import { BloqueFormComponent } from '../bloque-form/bloque-form.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-bloque-lista',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BloqueFormComponent,
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
    templateUrl: './bloque-lista.component.html'
})
export class BloqueListaComponent implements OnInit {
    bloques = signal<Bloque[]>([]);
    loading = signal(false);

    searchTerm = signal('');
    searchType = signal('ALL');

    filteredBloques = computed(() => {
        const text = this.searchTerm().toLowerCase();
        const type = this.searchType();
        const list = this.bloques().filter(b => 
            !b.categoria?.toLowerCase().startsWith('variables')
        );

        if (!text) return list;

        return list.filter(b => {
            if (type === 'NAME') return b.nombre.toLowerCase().includes(text);
            if (type === 'CATEGORY') return b.categoria.toLowerCase().includes(text);
            return b.nombre.toLowerCase().includes(text) || b.categoria.toLowerCase().includes(text);
        });
    });

    paginaActual = signal(0);
    tamanoPagina = signal(10);
    totalRegistros = computed(() => this.filteredBloques().length);
    totalPages = computed(() => Math.ceil(this.totalRegistros() / this.tamanoPagina()));

    paginatedBloques = computed(() => {
        const start = this.paginaActual() * this.tamanoPagina();
        const end = start + this.tamanoPagina();
        return this.filteredBloques().slice(start, end);
    });

    showModal = signal(false);
    showModalPreview = signal(false);
    selectedBloque = signal<Bloque | null>(null);
    previewHtml = signal<SafeHtml>('');

    private sanitizer = inject(DomSanitizer);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Documentos', route: '/documentos' },
        { label: 'Gestión de Bloques' }
    ];

    columns: Columna[] = [
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'categoria', header: 'Categoría', tipo: 'text', subField: [] },
        { field: 'modulo', header: 'Módulo', tipo: 'text', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NAME' },
        { label: 'Categoría', value: 'CATEGORY' }
    ];

    constructor(
        private documentoService: DocumentoService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarBloques();
    }

    cargarBloques(): void {
        this.loading.set(true);
        this.documentoService.listarBloques().subscribe({
            next: (res) => {
                const dataRaw: any = res.data;
                const dataArray = dataRaw?.content || dataRaw || [];
                const mapped = dataArray.map((b: any) => ({
                    ...b,
                    modulo: b.modulos && b.modulos.length > 0 ? b.modulos.join(', ') : ''
                }));
                this.bloques.set(mapped);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

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

    abrirModalNuevo(): void {
        this.selectedBloque.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(bloque: Bloque): void {
        this.selectedBloque.set(bloque);
        this.showModal.set(true);
    }

    abrirPreview(bloque: Bloque): void {
        const combined = `
            <style>${bloque.cssEstilo || ''}</style>
            <div class="block-preview-container">
                ${bloque.htmlContenido || ''}
            </div>
        `;
        this.previewHtml.set(this.sanitizer.bypassSecurityTrustHtml(combined));
        this.selectedBloque.set(bloque);
        this.showModalPreview.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.showModalPreview.set(false);
        this.selectedBloque.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Bloque guardado correctamente', 'success');
        this.cargarBloques();
    }

    eliminar(bloque: Bloque): void {
        if (!bloque.id) return;
        this.alertService.confirm(
            '¿Eliminar bloque?',
            `Se eliminará "${bloque.nombre}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.documentoService.eliminarBloque(bloque.id!).subscribe(() => {
                    this.alertService.toast('Bloque eliminado', 'success');
                    this.cargarBloques();
                });
            }
        });
    }
}
