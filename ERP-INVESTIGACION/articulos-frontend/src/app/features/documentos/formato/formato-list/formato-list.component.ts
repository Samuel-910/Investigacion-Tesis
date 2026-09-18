import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormatoService, DocumentoFormato } from '../../services/formato.service';
import { FormatoFormComponent } from '../formato-form/formato-form.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';


@Component({
    selector: 'app-formato-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        FormatoFormComponent,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PrimaryButtonComponent,
        ModalComponent,
        BreadcrumbComponent,
        SearchGenericComponent,
        PaginationComponent
    ],
    templateUrl: './formato-list.component.html'
})
export class FormatoListComponent implements OnInit {
    formatos = signal<DocumentoFormato[]>([]);
    loading = signal(false);

    // Paginación y Búsqueda
    paginaActual = signal(0);
    tamanoPagina = signal(10);
    totalRegistros = signal(0);
    query = signal('');
    filtroTipo = signal('NOMBRE');

    totalPages = computed(() => Math.ceil(this.totalRegistros() / this.tamanoPagina()));

    searchOptions = [
        { label: 'Nombre', value: 'NOMBRE' },
        { label: 'Descripción', value: 'DESCRIPCION' }
    ];

    showModal = signal(false);
    selectedFormato = signal<DocumentoFormato | null>(null);

    private formatoService = inject(FormatoService);
    private alertService = inject(AlertService);
    public sidebarService = inject(SidebarService);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Documentos', route: '/documentos' },
        { label: 'Formatos de Documento' }
    ];

    columns: Columna[] = [
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'anchoPx', header: 'Ancho (px)', tipo: 'text', subField: [] },
        { field: 'altoPx', header: 'Alto (px)', tipo: 'text', subField: [] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text', subField: [] }
    ];

    ngOnInit(): void {
        this.cargarFormatos();
    }

    cargarFormatos(): void {
        this.loading.set(true);
        // Nota: El servicio actual no soporta paginación en el backend, 
        // pero preparamos la estructura por si se implementa.
        // Por ahora listamos todo y podríamos filtrar en cliente si fuera necesario.
        this.formatoService.listar().subscribe({
            next: (res) => {
                if (res.success) {
                    const dataRaw: any = res.data;
                    let data: any[] = dataRaw?.content || dataRaw || [];

                    // Filtrado manual simple en cliente (mientras no haya endpoint de búsqueda)
                    if (this.query()) {
                        const q = this.query().toLowerCase();
                        data = data.filter(f => {
                            if (this.filtroTipo() === 'NOMBRE') return f.nombre.toLowerCase().includes(q);
                            if (this.filtroTipo() === 'DESCRIPCION') return f.descripcion?.toLowerCase().includes(q);
                            return true;
                        });
                    }

                    this.totalRegistros.set(data.length);

                    // Paginación manual en cliente
                    const inicio = this.paginaActual() * this.tamanoPagina();
                    const fin = inicio + this.tamanoPagina();
                    this.formatos.set(data.slice(inicio, fin));
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onSearch(event: { q: string, type: string }): void {
        this.query.set(event.q);
        this.filtroTipo.set(event.type);
        this.paginaActual.set(0);
        this.cargarFormatos();
    }

    cambiarPagina(pagina: number): void {
        this.paginaActual.set(pagina);
        this.cargarFormatos();
    }

    cambiarTamanoPagina(tamano: number): void {
        this.tamanoPagina.set(Number(tamano));
        this.paginaActual.set(0);
        this.cargarFormatos();
    }

    abrirModalNuevo(): void {
        this.selectedFormato.set(null);
        this.showModal.set(true);
    }

    abrirModalEditar(formato: DocumentoFormato): void {
        this.selectedFormato.set(formato);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedFormato.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Formato guardado correctamente', 'success');
        this.cargarFormatos();
    }

    eliminar(formato: DocumentoFormato): void {
        if (!formato.id) return;
        this.alertService.confirm(
            '¿Eliminar formato?',
            `Se eliminará "${formato.nombre}" permanentemente`,
            'Sí, eliminar',
            'Cancelar'
        ).then((res) => {
            if (res.isConfirmed) {
                this.formatoService.eliminar(formato.id).subscribe(() => {
                    this.alertService.toast('Formato eliminado', 'success');
                    this.cargarFormatos();
                });
            }
        });
    }
}
