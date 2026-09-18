import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompraService } from '../../services/compra.service';
import { CompraResponse } from '../../models/compra.model';
import { AuthService } from '../../../auth/services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { CompensaFormComponent } from '../compensa-form/compensa-form.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { SearchGenericComponent } from "../../../../shared/components/reusable-search-selector/reusable-search-selector";
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';

import { RegistroDetalleComponent } from '../registro-detalle/registro-detalle.component';
import { RegistroFormComponent } from '../registro-form/registro-form.component';
import { AlertService } from '../../../../core/services/alert.service';

@Component({
    selector: 'app-registro-list',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        ModalComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PageHeaderComponent,
        PrimaryButtonComponent,
        SearchGenericComponent,
        BreadcrumbComponent,
        RegistroDetalleComponent,
        RegistroFormComponent,
        CompensaFormComponent
    ],
    templateUrl: './registro-list.component.html'
})
export class RegistroListComponent implements OnInit {
    compras = signal<CompraResponse[]>([]);
    loading = signal(false);
    showModal = signal(false);
    soloResumenMode = signal(false);
    showModalSeleccion = signal(false);
    showModalRegistro = signal(false);
    solicitados = signal<CompraResponse[]>([]);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Compras', route: '/compra' },
        { label: 'Gestión de Compras' }
    ];

    // Pagination
    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    columns: Columna[] = [
        { field: 'fechaEmision', header: 'Fecha', tipo: 'text', subField: [] },
        { field: 'tipoComprobante', header: 'Comprobante', tipo: 'text', subField: [] },
        { field: 'serie_correlativo', header: 'Serie-Correlativo', tipo: 'text', subField: [] },
        { field: 'proveedorNombre', header: 'Proveedor', tipo: 'text', subField: [] },
        { field: 'totalConMoneda', header: 'Total', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];
    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'N° Comprobante', value: 'COMPROBANTE' },
        { label: 'Fecha', value: 'FECHA' },
        { label: 'Proveedor', value: 'PROVEEDOR' }
    ];
    searchTerm = signal('');
    searchType = signal('');
    estadoFiltro = signal<string | null>('PAGADO');

    constructor(
        private compraService: CompraService,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute,
        public sidebarService: SidebarService,
        private alertService: AlertService
    ) { }

    ngOnInit(): void {
        this.cargarCompras();
    }

    cargarCompras() {
        const idSucursal = this.authService.getSucursalIdFromToken();
        if (!idSucursal) {
            console.error('No se encontró ID de sucursal');
            return;
        }

        this.loading.set(true);
        const observable = this.searchTerm()
            ? this.compraService.buscar(idSucursal, undefined, undefined, undefined, this.estadoFiltro() || undefined, this.currentPage(), this.pageSize())
            : this.compraService.listar(idSucursal, this.currentPage(), this.pageSize(), this.estadoFiltro());
        observable.subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const mapped = res.data.content.map((c: any) => ({
                        ...c,
                        proveedorNombre: c.proveedor?.razonSocial || 'N/A',
                        serie_correlativo: `${c.serie}-${c.correlativo}`,
                        totalConMoneda: `${c.moneda} ${c.total.toFixed(2)}`,
                        estado: this.getEstadoNombre(c.estado)
                    }));
                    this.compras.set(mapped);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onEstadoChange(event: any) {
        const val = event.target.value;
        this.cambiarEstado(val === 'ALL' ? null : val);
    }

    cambiarEstado(estado: string | null) {
        this.estadoFiltro.set(estado);
        this.currentPage.set(0);
        this.cargarCompras();
    }

    cambiarPagina(page: number) {
        this.currentPage.set(page);
        this.cargarCompras();
    }

    cambiarTamanoPagina(size: number) {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.cargarCompras();
    }

    selectedCompraIdEditar = signal<number | null>(null);

    irARegistro() {
        this.cargarSolicitados();
        this.showModalSeleccion.set(true);
    }

    irACompraDirecta() {
        this.selectedCompraIdEditar.set(null);
        this.showModalSeleccion.set(false);
        this.showModalRegistro.set(true);
    }

    cargarSolicitados() {
        const idSucursal = this.authService.getSucursalIdFromToken();
        if (!idSucursal) return;

        this.loading.set(true);
        this.compraService.listar(idSucursal, 0, 50, 'VALIDADO').subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.solicitados.set(res.data.content);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    seleccionarOrden(orden: any) {
        this.selectedCompraIdEditar.set(orden.id);
        this.showModalSeleccion.set(false);
        this.showModalRegistro.set(true);
    }

    cerrarModalRegistro() {
        this.showModalRegistro.set(false);
        this.selectedCompraIdEditar.set(null);
    }

    cerrarModal() {
        this.showModal.set(false);
        this.selectedCompraIdEditar.set(null);
        this.soloResumenMode.set(false);
    }

    onGuardado() {
        this.cerrarModal();
        this.cerrarModalRegistro();
        this.cargarCompras();
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarCompras();
    }

    editarCompra(compra: any) {
        this.selectedCompraIdEditar.set(compra.id);
        this.showModalRegistro.set(true);
    }

    editarResumen(compra: any) {
        this.selectedCompraIdEditar.set(compra.id);
        this.soloResumenMode.set(true);
        this.showModal.set(true);
    }

    eliminarCompra(compra: any) {
        const estado = this.getEstadoNombre(compra.estado);
        if (estado === 'ANULADO') {
            this.alertService.toast('La compra ya está anulada', 'warning');
            return;
        }

        if (estado === 'PENDIENTE_ANULACION') {
            this.alertService.toast('Ya hay una solicitud pendiente para esta compra', 'info');
            return;
        }

        this.alertService.prompt(
            'Solicitar Anulación',
            `Ingrese el motivo para anular la compra ${compra.serie_correlativo}:`,
            'text'
        ).then((result) => {
            if (result.isConfirmed && result.value) {
                this.loading.set(true);
                this.compraService.anular(compra.id, result.value).subscribe({
                    next: (res) => {
                        this.loading.set(false);
                        if (res.success) {
                            this.alertService.success('Solicitado', 'La solicitud de anulación ha sido enviada.');
                            this.cargarCompras();
                        }
                    },
                    error: (err) => {
                        this.loading.set(false);
                        this.alertService.error('Error', err.error?.message || 'No se pudo enviar la solicitud.');
                    }
                });
            }
        });
    }

    showModalDetalle = signal(false);
    selectedCompraId = signal<number | null>(null);

    // ...

    verDetalles(compra: any) {
        this.selectedCompraId.set(compra.id);
        this.showModalDetalle.set(true);
    }

    imprimirCompraFisico(compra: any) {
        this.loading.set(true);
        this.compraService.imprimir(compra.id).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    this.lanzarImpresion(res.data);
                }
            },
            error: () => this.loading.set(false)
        });
    }

    private lanzarImpresion(htmlContent: string) {
        const iframe = document.createElement('iframe');
        iframe.style.position = 'fixed';
        iframe.style.right = '0';
        iframe.style.bottom = '0';
        iframe.style.width = '0';
        iframe.style.height = '0';
        iframe.style.border = '0';
        document.body.appendChild(iframe);

        const doc = iframe.contentWindow?.document;
        if (doc) {
            doc.open();
            doc.write(htmlContent);
            doc.close();

            setTimeout(() => {
                iframe.contentWindow?.focus();
                iframe.contentWindow?.print();
                setTimeout(() => document.body.removeChild(iframe), 1000);
            }, 500);
        }
    }

    cerrarModalDetalle() {
        this.showModalDetalle.set(false);
        this.selectedCompraId.set(null);
    }

    getEstadoNombre(estado: any): string {
        if (!estado) return '';
        if (typeof estado === 'string') return estado;
        if (typeof estado === 'object') return estado.name || estado.valor?.toString() || '';
        return String(estado);
    }
}
