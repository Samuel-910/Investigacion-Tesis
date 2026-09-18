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
import { VentaDetalleModalComponent } from './venta-detalle-modal/venta-detalle-modal.component';
import { DocumentoImpresionService } from '../../documentos/services/documento-impresion.service';
import { PuntoDocumentoService } from '../../documentos/services/punto-documento.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../auth/services/auth.service';

import { DrawerComponent } from '../../../shared/components/drawer/drawer.component';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { SelectOption } from '../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-venta-list',
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
        SearchableSelectComponent
    ],
    templateUrl: './venta-list.component.html'
})
export class VentaListComponent implements OnInit {
    ventas = signal<any[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    // Nuevos Filtros según imagen
    serie = signal('');
    numero = signal('');
    fechaDesde = signal(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]);
    fechaHasta = signal(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]);
    vendedor = signal('TODOS');
    condicionPago = signal('TODOS');
    estado = signal('TODOS');
    seriesOptions = signal<SelectOption[]>([]);
    vendedorOptions = signal<SelectOption[]>([]);
    selectedVenta = signal<any>(null);
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
            this.condicionPago() !== 'TODOS' ||
            (this.estado() !== 'TODOS' && this.estado() !== '');
        // Las fechas se omiten si queremos que siempre haya un rango por defecto
    });

    activeFiltersLabels = computed(() => {
        const labels: { key: string, label: string }[] = [];
        if (this.serie()) labels.push({ key: 'serie', label: `Serie: ${this.serie()}` });
        if (this.numero()) labels.push({ key: 'numero', label: `N°: ${this.numero()}` });
        if (this.vendedor() !== 'TODOS') labels.push({ key: 'vendedor', label: `Vendedor: ${this.vendedor()}` });
        if (this.condicionPago() !== 'TODOS') labels.push({ key: 'condicionPago', label: `Pago: ${this.condicionPago()}` });
        if (this.estado() !== 'TODOS' && this.estado() !== '') labels.push({ key: 'estado', label: `Estado: ${this.estado()}` });
        return labels;
    });

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Ventas', route: '/ventas' },
        { label: 'Listado de Ventas' }
    ];

    columns: Columna[] = [
        { field: 'tipoTra', header: 'TIPO TRA.', tipo: 'text', subField: [] },
        { field: 'numeroDocumento', header: 'DOCUMENTO', tipo: 'text', subField: [] },
        { field: 'facturadoA', header: 'FACTURADO A', tipo: 'text', subField: [] },
        { field: 'ruc', header: 'R.U.C.', tipo: 'text', subField: [] },
        { field: 'nombrePaciente', header: 'PACIENTE', tipo: 'text', subField: [] },
        { field: 'fecha', header: 'FECHA', tipo: 'date', subField: [] },
        { field: 'condPago', header: 'COND. PAGO', tipo: 'text', subField: [] },
        { field: 'moneda', header: 'MON.', tipo: 'text', subField: [] },
        { field: 'baseImp', header: 'BASE IMP.', tipo: 'currency', subField: [] },
        { field: 'valorInaf', header: 'INAF.', tipo: 'currency', subField: [] },
        { field: 'valorExo', header: 'EXON.', tipo: 'currency', subField: [] },
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
        private authService: AuthService,
        private router: Router,
        private impresionService: DocumentoImpresionService
    ) { }

    ngOnInit(): void {
        this.cargarSeries();
        this.cargarVendedores();
        this.cargarVentas();
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
                        .filter(pd => pd.modulo === 'VENTA')
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

    cargarVentas(): void {
        this.loading.set(true);

        // Adaptando los parámetros a lo que el servicio espera, 
        // pero enviando los nuevos filtros si el backend lo permite
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
            'OFICIAL' // Solamente facturas y boletas (excluye NV y CT)
        ).pipe(
            switchMap(res => {
                if (!res.success || !res.data || !res.data.content || res.data.content.length === 0) return of(res);
                const ventas: any[] = res.data.content;

                // Mapeo inicial para asegurar campos de la imagen
                ventas.forEach(v => {
                    v.tipoTra = v.tipoTra || 'SERVICIOS';
                    v.moneda = v.moneda || 'SOLES';
                    v.condPago = v.condPago || 'CONTADO';
                    v.facturadoA = v.facturadoA || v.nombrePaciente;
                    v.ruc = v.ruc || '00000000000';
                    v.baseImp = Number(v.baseImp) || 0;
                    v.valorInaf = Number(v.valorInaf) || 0;
                    v.valorExo = Number(v.valorExo) || 0;
                    v.descuento = Number(v.descuento) || 0;
                });

                // Por cada venta que tenga idPersonal numérico, buscar el nombre
                const enriquecidas$ = ventas.map(v => {
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
                    this.ventas.set(pageData.content || []);
                    this.totalElements.set(pageData.totalElements || 0);
                    this.totalPages.set(pageData.totalPages || 0);
                    this.calcularTotales(pageData.content || []);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar ventas', 'error');
            }
        });
    }

    calcularTotales(ventas: any[]): void {
        let sub = 0;
        let igv = 0;
        let desc = 0;
        let tot = 0;
        ventas.forEach(v => {
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
        this.cargarVentas();
    }


    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarVentas();
    }

    cambiarPageSize(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarVentas();
    }

    abrirNuevaVenta(): void {
        this.router.navigate(['/ventas/notas-venta']);
    }

    verDetalleAction(venta: any): void {
        const id = venta.idVenta || venta.id;
        if (!id) return;

        this.loading.set(true);
        this.ventaService.obtenerPorId(id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.selectedVenta.set(res.data);
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
        this.selectedVenta.set(null);
    }

    anular(venta: any): void {
        if (!venta) {
            this.alertService.toast('Seleccione una venta para anular', 'info');
            return;
        }
        const id = venta.idVenta || venta.id;
        if (!id) return;

        if (venta.estado === 'A' || venta.estado?.name === 'ANULADO' || venta.estado?.valor === 10) {
            this.alertService.toast('La venta ya se encuentra anulada', 'warning');
            return;
        }

        if (venta.estado === 'P' || venta.estado?.name === 'PENDIENTE_ANULACION' || venta.estado?.valor === 12) {
            this.alertService.toast('Ya existe una solicitud de anulación pendiente', 'info');
            return;
        }

        this.alertService.prompt(
            'Solicitar Anulación',
            `Ingrese el motivo para anular el documento "${venta.numeroDocumento || venta.numDocumento}":`,
            'text'
        ).then((res) => {
            if (res.isConfirmed && res.value) {
                this.loading.set(true);
                this.ventaService.anular(id, res.value).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.toast('Solicitud de anulación enviada correctamente', 'success');
                            this.cargarVentas();
                        } else {
                            this.alertService.toast(res.message || 'Error al solicitar', 'error');
                        }
                        this.loading.set(false);
                    },
                    error: (err) => {
                        this.loading.set(false);
                        this.alertService.toast('Error de conexión al solicitar', 'error');
                    }
                });
            }
        });
    }

    imprimir(venta: any): void {
        const authPuntoId = this.authService.getPuntoIdFromToken();
        if (!authPuntoId) {
            this.alertService.error('Error', 'No se ha detectado el punto de venta asociado al usuario');
            return;
        }

        const moduleType = venta.tipoDoc === 'NV' ? 'NOTA_VENTA' : 'VENTA';
        
        this.puntoDocumentoService.obtenerPorPunto(authPuntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const asignacion = res.data.find(a => 
                        a.modulo === moduleType && 
                        a.estado === 'ACTIVO' &&
                        a.tipoDoc === venta.tipoDoc
                    );
                    
                    if (!asignacion || !asignacion.idPlantilla) {
                        this.alertService.error('Error', `No hay plantilla de impresión configurada para este punto y tipo de documento (${venta.tipoDoc})`);
                        return;
                    }

                    const idPlantilla = asignacion.idPlantilla;

                    this.loading.set(true);
                    this.ventaService.obtenerPorId(venta.idVenta || venta.id).subscribe({
                        next: (resVenta) => {
                            this.loading.set(false);
                            if (resVenta.success && resVenta.data) {
                                this.impresionService.imprimirVenta(idPlantilla, resVenta.data)
                                    .then(() => this.alertService.toast('Documento enviado a impresión', 'success'))
                                    .catch(() => this.alertService.error('Error', 'No se pudo generar la impresión'));
                            }
                        },
                        error: () => {
                            this.loading.set(false);
                            this.alertService.error('Error', 'No se pudieron cargar los detalles de la venta para imprimir');
                        }
                    });
                } else {
                    this.alertService.error('Error', 'No se pudo obtener la configuración de documentos del punto de venta');
                }
            },
            error: () => this.alertService.error('Error', 'No se pudo obtener la configuración de documentos del punto de venta')
        });
    }

    limpiarFiltros(): void {
        this.serie.set('');
        this.numero.set('');
        this.vendedor.set('TODOS');
        this.condicionPago.set('TODOS');
        this.estado.set('TODOS');

        const hoy = new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0];
        this.fechaDesde.set(hoy);
        this.fechaHasta.set(hoy);

        this.currentPage.set(0);
        this.cargarVentas();
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
        this.cargarVentas();
    }
}
