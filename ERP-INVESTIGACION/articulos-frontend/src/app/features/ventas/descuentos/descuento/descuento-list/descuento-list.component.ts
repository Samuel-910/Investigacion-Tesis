import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DescuentoService } from '../../services/descuento.service';
import { Descuento, TipoDescuento } from '../../models/descuento.model';
import { RouterModule } from '@angular/router';
import { AlertService } from '../../../../../core/services/alert.service';
import { DescuentoFormComponent } from '../descuento-form/descuento-form.component';
import { HeaderComponent } from '../../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../../shared/sidebar/sidebar.component';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { SidebarService } from '../../../../../shared/sidebar/sidebar.service';
import { Columna, TablaGeneralComponent } from '../../../../../shared/components/tabla-general/tabla-general.component';
import { PageHeaderComponent } from '../../../../../shared/components/page-header/page-header';
import { SearchGenericComponent } from '../../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';

@Component({
    selector: 'app-descuento-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        DescuentoFormComponent,
        HeaderComponent,
        SidebarComponent,
        ModalComponent,
        TablaGeneralComponent,
        PageHeaderComponent,
        SearchGenericComponent,
        PaginationComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './descuento-list.component.html'
})
export class DescuentoListComponent implements OnInit {
    descuentos = signal<Descuento[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    pageSize = signal(10);
    totalElements = signal(0);
    totalPages = signal(0);

    searchTerm = signal('');
    searchType = signal('NOMBRE');

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/inicio' },
        { label: 'Ventas', route: '/ventas' },
        { label: 'Gestión de Descuentos' }
    ];

    showModal = signal(false);
    selectedDescuento = signal<Descuento | null>(null);

    columnasDescuentos: Columna[] = [
        { field: 'id', header: 'N°', tipo: 'index', subField: [] },
        {
            field: 'nombre',
            header: 'Nombre',
            tipo: 'layered-info',
            subField: [],
            layeredConfig: [
                { field: 'nombre', class: 'font-bold text-gray-900 dark:text-white' },
                { field: 'descripcion', class: 'text-xs text-gray-500 italic', format: 'text' }
            ]
        },
        {
            field: 'valorFormateado',
            header: 'Valor',
            tipo: 'badge',
            subField: [],
            badgeConfig: {
                from: 'from-blue-100',
                to: 'to-blue-100',
                text: 'text-blue-800',
                border: 'border-blue-200'
            }
        },
        { field: 'tipoAlcance', header: 'Alcance', tipo: 'text', subField: [] },
        { field: 'vigencia', header: 'Vigencia (Inicio | Fin)', tipo: 'text', subField: [] },
        { field: 'activo', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Nombre', value: 'NOMBRE' }
    ];

    constructor(
        private descuentoService: DescuentoService,
        public sidebarService: SidebarService,
        private alertService: AlertService
    ) { }

    ngOnInit(): void {
        this.cargarDescuentos();
    }

    cargarDescuentos(): void {
        this.loading.set(true);
        this.descuentoService.listar(this.currentPage(), this.pageSize(), this.searchTerm())
            .subscribe({
                next: (res) => {
                    if (res.success && res.data) {
                        const formattedContent = res.data.content.map((d: Descuento) => {
                            const primerDetalle = d.detalles && d.detalles.length > 0 ? d.detalles[0] : null;
                            const tieneMas = d.detalles && d.detalles.length > 1;

                            let valorTxt = '-';
                            if (primerDetalle) {
                                valorTxt = primerDetalle.tipoDescuento === TipoDescuento.PORCENTAJE
                                    ? `${primerDetalle.valorDescuento}%`
                                    : `S/ ${primerDetalle.valorDescuento}`;
                                if (tieneMas) valorTxt += ' (+)';
                            }

                            return {
                                ...d,
                                valorFormateado: valorTxt,
                                vigencia: `${this.formatDate(d.fechaInicio)} | ${this.formatDate(d.fechaFin)}`
                            };
                        });
                        this.descuentos.set(formattedContent);
                        this.totalElements.set(res.data.totalElements);
                        this.totalPages.set(res.data.totalPages);
                    }
                    this.loading.set(false);
                },
                error: (err) => {
                    this.loading.set(false);
                }
            });
    }

    private formatDate(dateStr: string | undefined): string {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        if (isNaN(date.getTime())) return dateStr;
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarDescuentos();
    }

    onPageChange(page: number): void {
        this.currentPage.set(page);
        this.cargarDescuentos();
    }

    buscar(): void {
        this.currentPage.set(0);
        this.cargarDescuentos();
    }

    abrirModalNuevo(): void {
        this.selectedDescuento.set(null);
        this.showModal.set(true);
    }

    editar(descuento: Descuento): void {
        this.selectedDescuento.set(descuento);
        this.showModal.set(true);
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.selectedDescuento.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.cargarDescuentos();
        this.alertService.toast('Descuento guardado correctamente', 'success');
    }

    eliminar(descuento: Descuento): void {
        const id = descuento.id!;
        this.alertService.confirm(
            '¿Estás seguro?',
            'No podrás revertir esto',
            'Sí, eliminar',
            'Cancelar'
        ).then((result) => {
            if (result.isConfirmed) {
                this.descuentoService.eliminar(id).subscribe({
                    next: (res) => {
                        this.alertService.toast('El descuento ha sido eliminado.', 'success');
                        this.cargarDescuentos();
                    },
                    error: (err) => {
                        this.alertService.error('Error', 'No se pudo eliminar el descuento.');
                    }
                });
            }
        });
    }

}
