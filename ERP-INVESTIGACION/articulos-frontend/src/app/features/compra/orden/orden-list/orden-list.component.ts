import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompraService } from '../../services/compra.service';
import { CompraResponse } from '../../models/compra.model';
import { AuthService } from '../../../auth/services/auth.service';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { SearchGenericComponent } from "../../../../shared/components/reusable-search-selector/reusable-search-selector";
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../../core/services/alert.service';
import { OrdenFormComponent } from '../orden-form/orden-form.component';

export interface OrdenGroup {
    nombreGrupo: string;
    ordenes: CompraResponse[];
    expanded: boolean;
}

@Component({
    selector: 'app-orden-list',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        ModalComponent,
        OrdenFormComponent,
        PaginationComponent,
        PageHeaderComponent,
        PrimaryButtonComponent,
        SearchGenericComponent,
        BreadcrumbComponent
    ],
    templateUrl: './orden-list.component.html'
})
export class OrdenListComponent implements OnInit {
    ordenes = signal<CompraResponse[]>([]);
    loading = signal(false);
    showModal = signal(false);
    soloResumenMode = signal(false);

    // Agrupación
    ordenesAgrupadas = computed(() => {
        const groupsMap = new Map<string, CompraResponse[]>();
        this.ordenes().forEach(o => {
            const key = o.nombreGrupo || 'SIN GRUPO';
            if (!groupsMap.has(key)) {
                groupsMap.set(key, []);
            }
            groupsMap.get(key)!.push(o);
        });

        // Retornamos todos los grupos ya que el backend ya filtra por estado
        return Array.from(groupsMap.entries())
            .map(([nombreGrupo, ordenes]) => ({
                nombreGrupo,
                ordenes,
                expanded: true // Por defecto expandidas para ver los datos nuevos
            } as OrdenGroup));
    });

    // Control de expansión manual (opcional si queremos persistencia)
    expandedGroups = signal<Record<string, boolean>>({});

    toggleGroup(nombreGrupo: string) {
        this.expandedGroups.update(prev => ({
            ...prev,
            [nombreGrupo]: !prev[nombreGrupo]
        }));
    }

    isGroupExpanded(nombreGrupo: string): boolean {
        return !!this.expandedGroups()[nombreGrupo];
    }

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Compra', route: '/compra' },
        { label: 'Órdenes de Compra' }
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
        { label: 'N° Orden', value: 'COMPROBANTE' },
        { label: 'Fecha', value: 'FECHA' },
        { label: 'Proveedor', value: 'PROVEEDOR' }
    ];
    searchTerm = signal('');
    searchType = signal('');
    estadoFiltro = signal<string | null>('PENDIENTE');

    constructor(
        private compraService: CompraService,
        private authService: AuthService,
        private router: Router,
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
            ? this.compraService.buscar(idSucursal, undefined, undefined, undefined, 'REGISTRADO', this.currentPage(), this.pageSize())
            : this.compraService.listarOrdenes(idSucursal, this.currentPage(), this.pageSize());
        observable.subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const mapped = res.data.content.map((c: any) => ({
                        ...c,
                        proveedorNombre: c.proveedor?.razonSocial || '',
                        serie_correlativo: (c.serie && c.correlativo) ? `${c.serie}-${c.correlativo}` : `id: ${c.id}`,
                        estado: this.getEstadoNombre(c.estado)
                    }));
                    this.ordenes.set(mapped);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    toggleVerAnulados() {
        this.estadoFiltro.set(this.estadoFiltro() === 'ANULADO' ? null : 'ANULADO');
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
        this.selectedCompraIdEditar.set(null);
        this.showModal.set(true);
    }

    cerrarModal() {
        this.showModal.set(false);
        this.selectedCompraIdEditar.set(null);
        this.soloResumenMode.set(false);
    }

    onGuardado() {
        this.cerrarModal();
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
        this.showModal.set(true);
    }

    editarResumen(compra: any) {
        this.selectedCompraIdEditar.set(compra.id);
        this.soloResumenMode.set(true);
        this.showModal.set(true);
    }

    eliminarCompra(compra: any) {
        const estado = this.getEstadoNombre(compra.estado);
        if (estado === 'ANULADO') {
            this.alertService.toast('La orden ya está anulada', 'warning');
            return;
        }

        if (estado === 'PENDIENTE_ANULACION') {
            this.alertService.toast('Ya hay una solicitud pendiente para esta orden', 'info');
            return;
        }

        this.alertService.confirm(
            'Anular Orden',
            `¿Está seguro de anular la orden del proveedor ${compra.proveedorNombre}?`,
            'Sí, anular',
            'Cancelar'
        ).then((result) => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.compraService.anular(compra.id).subscribe({
                    next: (res) => {
                        this.loading.set(false);
                        if (res.success) {
                            this.alertService.success('Anulado', 'La orden ha sido anulada exitosamente.');
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

    // Comparación
    showModalComparar = signal(false);
    selectedGroupForCompare = signal<OrdenGroup | null>(null);

    compararGrupo(group: OrdenGroup) {
        this.selectedGroupForCompare.set(group);
        // Asegurarnos de tener los detalles de todas las órdenes del grupo
        // Si no vienen en el listado, tendríamos que cargarlos aquí uno por uno.
        // Pero asumiremos que ya los tenemos o los pediremos.
        this.showModalComparar.set(true);
    }

    // Datos procesados para la tabla comparativa
    comparativoData = computed(() => {
        const group = this.selectedGroupForCompare();
        if (!group) return { productos: [], ordenes: [] };

        const productosMap = new Map<number, any>();
        const ordenesHeaders: any[] = [];

        group.ordenes.forEach(orden => {
            ordenesHeaders.push({
                id: orden.id,
                proveedor: orden.proveedor?.razonSocial,
                total: orden.total,
                moneda: orden.moneda
            });

            (orden.detalles || []).forEach(d => {
                const prodId = d.producto?.id;
                if (!prodId) return;

                if (!productosMap.has(prodId)) {
                    productosMap.set(prodId, {
                        id: prodId,
                        nombre: d.producto.nombre,
                        precios: {} // ordenId -> precio data
                    });
                }
                
                const precioBruto = d.precioUnitario || 0;
                const desc1 = d.porcentajeDescuento || 0;
                const desc2 = d.porcentajeDescuento2 || 0;
                const p1 = precioBruto * (1 - desc1 / 100);
                const precioFinal = p1 * (1 - desc2 / 100);

                productosMap.get(prodId).precios[orden.id] = {
                    precioUnitario: precioBruto,
                    descuento1: desc1,
                    descuento2: desc2,
                    precioFinal: precioFinal,
                    esBonificacion: d.esBonificacion
                };
            });
        });

        return {
            productos: Array.from(productosMap.values()),
            ordenes: ordenesHeaders
        };
    });

    getPrecioEnOrden(producto: any, ordenId: number): any | null {
        return producto.precios[ordenId] || null;
    }

    esMejorPrecio(producto: any, ordenId: number): boolean {
        const datosActual = this.getPrecioEnOrden(producto, ordenId);
        if (!datosActual) return false;

        const todosLosPrecios = Object.values(producto.precios).map((p: any) => p.precioFinal) as number[];
        const minPrecio = Math.min(...todosLosPrecios);
        return datosActual.precioFinal === minPrecio;
    }

    seleccionarOrden(id: number) {
        const group = this.selectedGroupForCompare();
        if (!group) return;

        this.alertService.confirm(
            'Seleccionar Ganadora',
            '¿Está seguro de seleccionar esta orden como la ganadora? Las demás órdenes del grupo se cancelarán.',
            'Aceptar',
            'Cancelar'
        ).then((result) => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.compraService.seleccionarGanadora(id, group.nombreGrupo).subscribe({
                    next: (res) => {
                        this.loading.set(false);
                        if (res.success) {
                            this.alertService.success('Completado', 'Orden seleccionada y grupo actualizado.');
                            this.cerrarModalComparar();
                            this.cargarCompras();
                        }
                    },
                    error: (err) => {
                        this.loading.set(false);
                        this.alertService.error('Error', err.error?.message || 'No se pudo seleccionar la ganadora.');
                    }
                });
            }
        });
    }

    cerrarModalComparar() {
        this.showModalComparar.set(false);
        this.selectedGroupForCompare.set(null);
    }

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

    imprimirMatriz(grupo: OrdenGroup) {
        const idSucursal = this.authService.getSucursalIdFromToken();
        if (!idSucursal) return;
        this.loading.set(true);
        this.compraService.imprimirGrupo(grupo.nombreGrupo, idSucursal).subscribe({
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
