import { Component, inject, signal, OnInit, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { InventarioService } from '../../../procesos/service/movimiento.service';
import { AuthService } from '../../../auth/services/auth.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { AlertService } from '../../../../core/services/alert.service';
import { PrimaryButtonComponent } from "../../../../shared/components/primary-button/primary-button";
import { FormInputComponent } from "../../../../shared/components/forms/form-input/form-input.component";
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { FormSelectComponent } from "../../../../shared/components/forms/form-select/form-select.component";
import { Router } from '@angular/router';
import { MovimientoDetalleModalComponent } from '../movimiento-detalle-modal/movimiento-detalle-modal.component';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';
import { PuntoService } from '../../../../features/configuraciones/services/punto.service';
import { PuntoDocumentoService } from '../../../../features/documentos/services/punto-documento.service';

@Component({
    selector: 'app-movimiento-historial',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PrimaryButtonComponent,
        MovimientoDetalleModalComponent,
        AdvancedFilterDrawerComponent
    ],
    templateUrl: './movimiento-historial.component.html'
})
export class MovimientoHistorialComponent implements OnInit, OnDestroy {
    private inventarioService = inject(InventarioService);
    private sucursalService = inject(SucursalService);
    private puntoService = inject(PuntoService);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    private authService = inject(AuthService);
    public sidebarService = inject(SidebarService);
    private alertService = inject(AlertService);
    private router = inject(Router);
    movimientos = signal<any[]>([]);
    loading = signal(false);
    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    // Modal de Detalles
    showModalDetalles = signal(false);
    movimientoSeleccionado = signal<any>(null);

    // Filtros
    searchTerm = signal('');
    defaultSucursalId = signal<number | 'TODOS'>('TODOS');

    // New Drawer count state
    activeFiltersCount = signal(0);
    sucursalOptions = signal<any[]>([{ label: 'Todas', value: 'TODOS' }]);
    puntoVentaOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);
    tipoDocumentoOptions = signal<any[]>([
        { label: 'Todos', value: 'TODOS' }
    ]);

    // Advanced Filters state overrides old signals
    advancedFilters = signal<AdvancedFilters>({
        fechaDesde: '',
        fechaHasta: '',
        sucursalId: 'TODOS',
        estado: 'TODOS',
        tipoMovimiento: 'TODOS',
        tipoDocumento: 'TODOS',
        serie: '',
        numeroComprobante: '',
        vendedorId: 'TODOS',
        puntoVentaId: 'TODOS',
        condicionPagoId: 'TODOS'
    });

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos' },
        { label: 'Ingreso y Salidas Diversas' }
    ];

    columns: Columna[] = [
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'tipo', header: 'Tipo', tipo: 'movimiento-status', subField: [] },
        { field: 'numDocumento', header: 'Nro. Doc', tipo: 'text', subField: [] },
        { field: 'motivo', header: 'Motivo', tipo: 'text', subField: [] },
        { field: 'usuarioNombre', header: 'Usuario', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    estadoOptions = [
        { label: 'Todos', value: 'TODOS' },
        { label: 'Activo', value: 'ACTIVO' },
        { label: 'Borrador (Cot.)', value: 'COTIZACION' },
        { label: 'Anulado', value: 'ANULADO' }
    ];

    ngOnInit(): void {
        const authSucursalId = this.authService.getSucursalIdFromToken();
        if (authSucursalId) {
            this.defaultSucursalId.set(authSucursalId);
            this.advancedFilters.update(f => ({ ...f, sucursalId: authSucursalId }));
        }

        this.cargarSucursales();
        this.cargarMovimientos();
        this.cargarPuntos(this.defaultSucursalId());
        this.cargarTiposDocumento(this.defaultSucursalId());
    }

    cargarPuntos(idSucursal: any): void {
        if (!idSucursal || idSucursal === 'TODOS') {
            this.puntoVentaOptions.set([{ label: 'Todos', value: 'TODOS' }]);
            return;
        }
        this.puntoService.buscarPorSucursal(idSucursal).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const ops = [{ label: 'Todos', value: 'TODOS' }];
                    res.data.forEach((p: any) => ops.push({ 
                        label: p.nombre || p.nombrePunto, 
                        value: p.punto || p.idPunto || p.id 
                    }));
                    this.puntoVentaOptions.set(ops);
                }
            }
        });
    }

    // Lista completa de documentos para poder auto-completar la serie
    documentosDisponibles = signal<any[]>([]);

    cargarTiposDocumento(idSucursal: any, idPuntoVenta?: any): void {
        const modulos = ['INGRESOS_DIVERSOS', 'SALIDAS_DIVERSAS'];
        const obs = (idPuntoVenta && idPuntoVenta !== 'TODOS') 
            ? this.puntoDocumentoService.obtenerPorPunto(idPuntoVenta, modulos)
            : this.puntoDocumentoService.obtenerDocumentosActivosPorSucursal(idSucursal, modulos);

        obs.subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    this.documentosDisponibles.set(res.data);
                    
                    // Extraer los tipos únicos
                    const ops = [{ label: 'Todos', value: 'TODOS' }];
                    const mapTipos = new Map<string, any>();
                    
                    res.data.forEach((doc: any) => {
                        if (doc.tipoDoc && doc.serie) {
                            const compoundValue = `${doc.tipoDoc}|${doc.serie}`;
                            if (!mapTipos.has(compoundValue)) {
                                mapTipos.set(compoundValue, doc);
                                let label = doc.tipoDocumentoNombre || doc.tipoDoc;
                                if (doc.modulo === 'INGRESOS_DIVERSOS') {
                                    label += ' (Ingreso)';
                                } else if (doc.modulo === 'SALIDAS_DIVERSAS') {
                                    label += ' (Salida)';
                                }
                                ops.push({ label, value: compoundValue });
                            }
                        } else if (doc.tipoDoc && !mapTipos.has(doc.tipoDoc)) {
                            mapTipos.set(doc.tipoDoc, doc);
                            ops.push({ label: doc.tipoDocumentoNombre || doc.tipoDoc, value: doc.tipoDoc });
                        }
                    });
                    this.tipoDocumentoOptions.set(ops);
                }
            }
        });
    }

    cargarSucursales(): void {
        this.sucursalService.getActivas().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const ops = [{ label: 'Todas', value: 'TODOS' }];
                    res.data.forEach((s: any) => {
                        ops.push({ label: s.nombreSucursal, value: s.idSucursal });
                    });
                    this.sucursalOptions.set(ops);
                }
            }
        });
    }

    onFiltersApplied(filters: AdvancedFilters): void {
        const oldFilters = this.advancedFilters();
        this.advancedFilters.set(filters);
        
        if (oldFilters.sucursalId !== filters.sucursalId) {
            this.cargarPuntos(filters.sucursalId);
            this.cargarTiposDocumento(filters.sucursalId, filters.puntoVentaId);
        } else if (oldFilters.puntoVentaId !== filters.puntoVentaId) {
            this.cargarTiposDocumento(filters.sucursalId, filters.puntoVentaId);
        }

        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    ngOnDestroy(): void {
    }

    esCotizacion = (item: any): boolean => {
        return item.estado === 'COTIZACION';
    }

    irNuevoMovimiento(tipo: 'INGRESO' | 'SALIDA'): void {
        this.router.navigate(['/procesos/nuevo-movimiento'], { queryParams: { tipo } });
    }

    continuarCotizacion(idMovimiento: number): void {
        this.router.navigate(['/procesos/nuevo-movimiento'], { queryParams: { idMovimiento } });
    }

    cargarMovimientos(): void {
        const filters = this.advancedFilters();
        const idSuc = filters.sucursalId === 'TODOS' ? 'TODOS' : filters.sucursalId;

        this.loading.set(true);
        this.inventarioService.buscarMovimientosDiversos(
            idSuc,
            filters.fechaDesde,
            filters.fechaHasta,
            filters.estado === 'TODOS' ? '' : filters.estado,
            this.searchTerm(), // Global search term
            this.currentPage(),
            this.pageSize(),
            filters.puntoVentaId === 'TODOS' ? undefined : filters.puntoVentaId as number,
            filters.tipoDocumento === 'TODOS' ? undefined : (filters.tipoDocumento.includes('|') ? filters.tipoDocumento.split('|')[0] : filters.tipoDocumento),
            filters.serie === '' ? undefined : filters.serie,
            filters.numeroComprobante === '' ? undefined : filters.numeroComprobante
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    const content = res.data.content.map((m: any) => {
                        let tipo = 'MIXTO';
                        if (m.detalles && m.detalles.length > 0) {
                            const primerTipo = m.detalles[0].tipo;
                            const todosIguales = m.detalles.every((d: any) => d.tipo === primerTipo);
                            if (todosIguales) {
                                tipo = primerTipo;
                            }
                        }
                        return { ...m, tipo };
                    });
                    this.movimientos.set(content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudieron cargar los movimientos');
            }
        });
    }

    onGlobalSearch(term: string): void {
        this.searchTerm.set(term);
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarMovimientos();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    verDetallesMovimiento(movimiento: any): void {
        this.movimientoSeleccionado.set(movimiento);
        this.showModalDetalles.set(true);
    }

    cerrarModalDetalles(): void {
        this.showModalDetalles.set(false);
        this.movimientoSeleccionado.set(null);
    }

    anularMovimiento(item: any): void {
        const motivo = item.motivo || 'Desconocido';
        const doc = item.numDocumento || 'S/N';

        this.alertService.prompt(
            'Solicitar Anulación',
            `¿Está seguro de solicitar la anulación del movimiento "${motivo}" (${doc})? Ingrese el motivo de la anulación:`,
            'text'
        ).then((result) => {
            if (result.isConfirmed) {
                const motivoAnulacion = result.value;
                if (!motivoAnulacion) {
                    this.alertService.error('Error', 'Debe ingresar un motivo para solicitar la anulación.');
                    return;
                }

                this.loading.set(true);
                this.inventarioService.solicitarAnulacion(item.id, motivoAnulacion).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.success('Enviada', 'La solicitud de anulación ha sido enviada para su aprobación.');
                            this.cargarMovimientos();
                        }
                        this.loading.set(false);
                    },
                    error: (err) => {
                        this.loading.set(false);
                        this.alertService.error('Error', err.error?.message || 'Ocurrió un error al enviar la solicitud.');
                    }
                });
            }
        });
    }
}
