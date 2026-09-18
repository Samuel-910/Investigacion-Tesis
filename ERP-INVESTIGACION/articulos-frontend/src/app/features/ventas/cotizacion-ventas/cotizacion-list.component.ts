import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';
import { VentaRegistroService } from '../services/venta-registro.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../core/services/alert.service';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { Router } from '@angular/router';
import { VentaDetalleModalComponent } from '../listado-ventas/venta-detalle-modal/venta-detalle-modal.component';
import { DocumentoImpresionService } from '../../documentos/services/documento-impresion.service';
import { PuntoDocumentoService } from '../../documentos/services/punto-documento.service';
import { UserService } from '../../../core/services/user.service';

import { DrawerComponent } from '../../../shared/components/drawer/drawer.component';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { AuthService } from '../../auth/services/auth.service';
import { SelectOption } from '../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-cotizacion-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BreadcrumbComponent,
        VentaDetalleModalComponent,
        DrawerComponent,
        PageHeaderComponent,
        FormInputComponent,
        SearchableSelectComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './cotizacion-list.component.html'
})
export class CotizacionListComponent implements OnInit {
    cotizaciones = signal<any[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    // Filtros Avanzados
    serie = signal('');
    numero = signal('');
    fechaDesde = signal(new Date().toISOString().split('T')[0]);
    fechaHasta = signal(new Date().toISOString().split('T')[0]);
    vendedor = signal('TODOS');
    condicionPago = signal('TODOS');
    estado = signal('TODOS');
    seriesOptions = signal<SelectOption[]>([]);
    vendedorOptions = signal<SelectOption[]>([]);
    selectedCotizacion = signal<any>(null);
    isModalOpen = signal(false);
    isDrawerOpen = signal(false);

    // Totales
    subtotalGeneral = signal(0);
    igvGeneral = signal(0);
    descuentoGeneral = signal(0);
    totalGeneral = signal(0);

    // Lógica de filtros activos
    hasActiveFilters = computed(() => {
        return this.serie() !== '' ||
            this.numero() !== '' ||
            this.vendedor() !== 'TODOS' ||
            this.condicionPago() !== 'TODOS';
    });

    activeFiltersLabels = computed(() => {
        const labels: { key: string, label: string }[] = [];
        if (this.serie()) labels.push({ key: 'serie', label: `Serie: ${this.serie()}` });
        if (this.numero()) labels.push({ key: 'numero', label: `N°: ${this.numero()}` });
        if (this.vendedor() !== 'TODOS') labels.push({ key: 'vendedor', label: `Vendedor: ${this.vendedor()}` });
        if (this.condicionPago() !== 'TODOS') labels.push({ key: 'condicionPago', label: `Pago: ${this.condicionPago()}` });
        return labels;
    });

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Ventas', route: '/ventas' },
        { label: 'Cotizaciones' }
    ];

    columns: Columna[] = [
        { field: 'tipoDocDesc', header: 'TIPO DOC.', tipo: 'text', subField: [] },
        { field: 'numeroDocumento', header: 'DOCUMENTO', tipo: 'text', subField: [] },
        { field: 'facturadoA', header: 'CLIENTE', tipo: 'text', subField: [] },
        { field: 'ruc', header: 'DOCUMENTO ID', tipo: 'text', subField: [] },
        { field: 'nombrePaciente', header: 'PACIENTE', tipo: 'text', subField: [] },
        { field: 'fecha', header: 'FECHA', tipo: 'date', subField: [] },
        { field: 'condPago', header: 'COND. PAGO', tipo: 'text', subField: [] },
        { field: 'moneda', header: 'MON.', tipo: 'text', subField: [] },
        { field: 'baseImp', header: 'BASE IMP.', tipo: 'currency', subField: [] },
        { field: 'descuento', header: 'DESC.', tipo: 'currency', subField: [] },
        { field: 'igv', header: 'I.G.V.', tipo: 'currency', subField: [] },
        { field: 'total', header: 'TOTAL', tipo: 'currency', subField: [] },
        { field: 'estado', header: 'ESTADO', tipo: 'status', subField: [] }
    ];

    constructor(
        private ventaService: VentaRegistroService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private puntoDocumentoService: PuntoDocumentoService,
        private userService: UserService,
        private router: Router,
        private impresionService: DocumentoImpresionService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.cargarSeries();
        this.cargarVendedores();
        this.cargarCotizaciones();
    }

    cargarVendedores(): void {
        this.userService.getActiveUsers(0, 50).subscribe({
            next: (res) => {
                if (res && res.content) {
                    const vendedores = res.content.map(u => ({
                        label: u.nombreCompleto || u.username,
                        value: u.username
                    }));
                    this.vendedorOptions.set([
                        { label: 'TODOS', value: 'TODOS' },
                        ...vendedores
                    ]);
                }
            }
        });
    }

    cargarSeries(): void {
        this.puntoDocumentoService.obtenerTodos(0, 100).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const series = res.data.content
                        .filter(pd => pd.modulo === 'VENTA' || pd.modulo === 'COTIZACION')
                        .map(pd => ({
                            label: `${pd.serie} - ${pd.tipoDocumentoNombre || pd.tipoDoc}`,
                            value: pd.serie
                        }));

                    this.seriesOptions.set([
                        { label: 'TODOS', value: '' },
                        ...series
                    ]);
                }
            }
        });
    }

    cargarCotizaciones(): void {
        this.loading.set(true);

        this.ventaService.search(
            this.currentPage(),
            this.pageSize(),
            this.serie(),
            this.numero(),
            '', // numeroDesde
            '', // numeroHasta
            this.fechaDesde(),
            this.fechaHasta(),
            this.vendedor() === 'TODOS' ? '' : this.vendedor(),
            this.condicionPago() === 'TODOS' ? '' : this.condicionPago(),
            this.estado() === 'TODOS' ? '' : this.estado(),
            'C' // tipoDoc (Cotizaciones)
        ).pipe(
            switchMap(res => {
                if (!res.success || !res.data || !res.data.content || res.data.content.length === 0) return of(res);
                const cotizaciones: any[] = res.data.content;

                cotizaciones.forEach(v => {
                    v.tipoDocDesc = v.tipoDoc === '03' ? 'Bol. Electrónica' : v.tipoDoc === '01' ? 'Fact. Electrónica' : (v.tipoDoc === '12' ? 'Ticket' : 'Nota de Venta');
                    v.moneda = v.moneda || 'SOLES';
                    v.condPago = v.condPago || 'CONTADO';
                    v.facturadoA = v.facturadoA || v.nombrePaciente;
                    v.ruc = v.ruc || '00000000000';
                    v.baseImp = Number(v.baseImp) || 0;
                    v.descuento = Number(v.descuento) || 0;
                    if (v.serie && (v.numero || v.numdoc || v.numDocumento)) {
                        v.numeroDocumento = `${v.serie}-${v.numero || v.numdoc || v.numDocumento}`;
                    } else {
                        v.numeroDocumento = `COT-${String(v.idVenta || v.id).padStart(5, '0')}`;
                    }
                });

                const enriquecidas$ = cotizaciones.map(v => {
                    const idNum = Number(v.idPersonal);
                    if (v.idPersonal && !isNaN(idNum) && (!v.nombrePaciente || v.nombrePaciente === v.idPersonal)) {
                        return this.ventaService.obtenerPaciente(idNum).pipe(
                            map(userRes => {
                                if (userRes?.data) {
                                    const u = userRes.data;
                                    const nombre = [u.nombre, u.apepat, u.apemat].filter(Boolean).join(' ');
                                    v.nombrePaciente = nombre || v.idPersonal;
                                    v.facturadoA = v.facturadoA || v.nombrePaciente;
                                }
                                return v;
                            })
                        );
                    }
                    return of(v);
                });

                return forkJoin(enriquecidas$).pipe(
                    map(ventasEnriquecidas => {
                        res.data.content = ventasEnriquecidas;
                        return res;
                    })
                );
            })
        ).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const pageData = res.data;
                    this.cotizaciones.set(pageData.content || []);
                    this.totalElements.set(pageData.totalElements || 0);
                    this.totalPages.set(pageData.totalPages || 0);
                    this.calcularTotales(pageData.content || []);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar cotizaciones', 'error');
            }
        });
    }

    calcularTotales(cotizaciones: any[]): void {
        let sub = 0;
        let igv = 0;
        let desc = 0;
        let tot = 0;
        cotizaciones.forEach(v => {
            sub += Number(v.baseImp) || 0;
            igv += Number(v.igv) || 0;
            desc += Number(v.descuento) || 0;
            tot += Number(v.total) || 0;
        });
        this.subtotalGeneral.set(sub);
        this.igvGeneral.set(igv);
        this.descuentoGeneral.set(desc);
        this.totalGeneral.set(tot);
    }

    onFiltrar(): void {
        this.isDrawerOpen.set(false);
        this.currentPage.set(0);
        this.cargarCotizaciones();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarCotizaciones();
    }

    verDetalleAction(cotizacion: any): void {
        const id = cotizacion.idVenta || cotizacion.id;
        if (!id) return;

        this.loading.set(true);
        this.ventaService.obtenerPorId(id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.selectedCotizacion.set(res.data);
                    this.isModalOpen.set(true);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al obtener detalles', 'error');
            }
        });
    }

    cerrarModal(): void {
        this.isModalOpen.set(false);
        this.selectedCotizacion.set(null);
    }

    limpiarFiltros(): void {
        this.serie.set('');
        this.numero.set('');
        this.vendedor.set('TODOS');
        this.condicionPago.set('TODOS');
        this.estado.set('TODOS');

        const hoy = new Date().toISOString().split('T')[0];
        this.fechaDesde.set(hoy);
        this.fechaHasta.set(hoy);

        this.currentPage.set(0);
        this.cargarCotizaciones();
    }

    eliminarFiltro(key: string): void {
        switch (key) {
            case 'serie': this.serie.set(''); break;
            case 'numero': this.numero.set(''); break;
            case 'vendedor': this.vendedor.set('TODOS'); break;
            case 'condicionPago': this.condicionPago.set('TODOS'); break;
            case 'estado': this.estado.set('TODOS'); break;
        }
        this.currentPage.set(0);
        this.cargarCotizaciones();
    }

    imprimir(cotizacion: any): void {
        const id = cotizacion.idVenta || cotizacion.id;
        if (!id) return;

        const puntoId = this.authService.getPuntoIdFromToken();
        if (!puntoId) {
            this.alertService.error('No se pudo identificar su punto de venta actual');
            return;
        }

        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res) => {
                let idPlantilla = 1; // Fallback
                if (res.success && res.data) {
                    const docCotizacion = res.data.find((pd: any) => pd.modulo === 'COTIZACION');
                    if (docCotizacion && docCotizacion.idPlantilla) {
                        idPlantilla = docCotizacion.idPlantilla;
                    } else {
                        this.alertService.warning('No tiene una plantilla asignada para Cotizaciones. Usando formato por defecto.');
                    }
                }

                this.impresionService.imprimirVenta(idPlantilla, cotizacion).catch(() => {
                    this.alertService.error('Error al imprimir la cotización');
                });
            }
        });
    }



    facturar(cotizacion: any): void {
        const id = cotizacion.idVenta || cotizacion.id;
        if (!id) return;
        this.router.navigate(['/venta/venta'], { queryParams: { cotizacionId: id } });
    }

    async anular(cotizacion: any): Promise<void> {
        const id = cotizacion.idVenta || cotizacion.id;
        if (!id) return;

        const result = await this.alertService.confirm(
            '¿Anular cotización?',
            `¿Está seguro de anular la cotización ${cotizacion.serie}-${cotizacion.numero || cotizacion.numdoc}?`,
            'Sí, anular',
            'Cancelar'
        );

        if (!result.isConfirmed) return;

        this.ventaService.anularCotizacion(id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success('Anulada', 'Cotización anulada correctamente');
                    this.cargarCotizaciones();
                } else {
                    this.alertService.error('Error', res.message || 'Error al anular cotización');
                }
            },
            error: () => this.alertService.error('Error', 'Error de conexión al anular cotización')
        });
    }
}
