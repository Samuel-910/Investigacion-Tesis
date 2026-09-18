import { Component, Input, Output, EventEmitter, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimaryButtonComponent } from '../../../components/primary-button/primary-button';
import { AtributoFormComponent } from '../atributo-form/atributo-form.component';
import { Columna, TablaGeneralComponent } from '../../../components/tabla-general/tabla-general.component';
import { SearchGenericComponent } from '../../../components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../components/pagination/pagination';
import { ModalComponent } from '../../../components/modal/modal';
import { BaseAtributoService } from '../../../../features/almacen/service/atributo.service';
import { AlertService } from '../../../../core/services/alert.service';
import { AtributoRequest, BaseAtributo } from '../../../../features/configuraciones/models/atributo.model';


@Component({
    selector: 'app-atributo-list',
    standalone: true,
    imports: [
        CommonModule,
        PrimaryButtonComponent,
        AtributoFormComponent,
        TablaGeneralComponent,
        SearchGenericComponent,
        PaginationComponent,
        ModalComponent
    ],
    templateUrl: './atributo-list.component.html'
})
export class AtributoListComponent implements OnInit {
    @Input() service!: BaseAtributoService<BaseAtributo>;
    @Output() totalElementsChange = new EventEmitter<number>();

    titulo = signal<string>('Item');
    @Input('titulo') set _titulo(val: string) {
        this.titulo.set(val);
    }

    @ViewChild(AtributoFormComponent) formComponent!: AtributoFormComponent;

    items = signal<BaseAtributo[]>([]);
    loading = signal(false);

    totalElements = signal(0);
    totalPages = signal(0);
    currentPage = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');

    showModal = signal(false);
    selectedItem = signal<BaseAtributo | null>(null);

    columns: Columna[] = [
        { field: 'N°', header: 'N°', tipo: 'index', subField: [] },
        { field: 'descripcion', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Nombre', value: 'descripcion' }
    ];

    constructor(private alertService: AlertService) { }

    ngOnInit() {
        if (this.service) {
            this.cargarDatos();
        }
    }

    cargarDatos(page: number = 0) {
        this.loading.set(true);
        this.currentPage.set(page);

        this.service.listar(page, this.pageSize(), this.searchTerm()).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.items.set(response.data.content);
                    this.totalElements.set(response.data.totalElements);
                    this.totalElementsChange.emit(response.data.totalElements);
                    this.totalPages.set(response.data.totalPages);
                } else {
                    this.items.set([]);
                    this.totalElements.set(0);
                    this.totalElementsChange.emit(0);
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error al cargar datos');
            }
        });
    }

    handleSearch(event: { q: string }) {
        this.searchTerm.set(event.q);
        this.cargarDatos(0);
    }

    onPageChange(page: number) {
        this.cargarDatos(page);
    }

    onPageSizeChange(size: number) {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarDatos(0);
    }

    abrirModalNuevo() {
        this.selectedItem.set(null);
        this.showModal.set(true);
    }

    editar(item: BaseAtributo) {
        this.selectedItem.set(item);
        this.showModal.set(true);
    }

    eliminar(item: BaseAtributo) {
        this.alertService.confirm('¿Estás seguro?', 'No podrás revertir esta acción').then((result) => {
            if (result.isConfirmed) {
                this.service.eliminar(item.id).subscribe({
                    next: () => {
                        this.alertService.toast('El registro ha sido eliminado.', 'success');
                        this.cargarDatos(this.currentPage());
                    },
                    error: (err) => {
                        this.alertService.toast('No se pudo eliminar el registro.', 'error');
                    }
                });
            }
        });
    }

    cerrarModal() {
        this.showModal.set(false);
        this.selectedItem.set(null);
    }

    onGuardar(request: AtributoRequest) {
        const item = this.selectedItem();
        let obs = item ? this.service.actualizar(item.id, request) : this.service.crear(request);

        obs.subscribe({
            next: (response) => {
                if (response.success) {
                    this.alertService.toast('Operación realizada con éxito', 'success');
                    this.cerrarModal();
                    this.cargarDatos(this.currentPage());
                } else {
                    this.alertService.error(response.message || 'Error al guardar');
                }
                this.formComponent.setLoading(false);
            },
            error: (err) => {
                this.alertService.error('Error al procesar la solicitud');
                this.formComponent.setLoading(false);
            }
        });
    }
}
