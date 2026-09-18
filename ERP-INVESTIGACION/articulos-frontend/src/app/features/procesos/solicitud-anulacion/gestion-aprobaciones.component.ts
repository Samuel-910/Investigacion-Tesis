import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SucursalService } from '../../../core/services/sucursal.service';
import { PuntoService } from '../../configuraciones/services/punto.service';
import { AuthService } from '../../auth/services/auth.service';
import { AprobacionService, SolicitudAnulacionDTO } from '../service/aprobacion.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../core/services/alert.service';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { VentaDetalleModalComponent } from '../../ventas/listado-ventas/venta-detalle-modal/venta-detalle-modal.component';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { RegistroDetalleComponent } from '../../compra/registro/registro-detalle/registro-detalle.component';
import { VentaRegistroService } from '../../ventas/services/venta-registro.service';
import { MovimientoDetalleModalComponent } from '../movimientos/movimiento-detalle-modal/movimiento-detalle-modal.component';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';

@Component({
    selector: 'app-gestion-aprobaciones',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        PaginationComponent,
        VentaDetalleModalComponent,
        ModalComponent,
        RegistroDetalleComponent,
        MovimientoDetalleModalComponent,
        AdvancedFilterDrawerComponent
    ],
    templateUrl: './gestion-aprobaciones.component.html'
})
export class GestionAprobacionesComponent implements OnInit {
    aprobacionService = inject(AprobacionService);
    sidebarService = inject(SidebarService);
    alertService = inject(AlertService);
    ventaService = inject(VentaRegistroService);

    solicitudes = signal<SolicitudAnulacionDTO[]>([]);
    historial = signal<SolicitudAnulacionDTO[]>([]);
    tabActiva = signal<'pendientes' | 'historial'>('pendientes');
    loading = signal(false);

    tieneSolicitudesIniciales = signal(false);
    tieneHistorialInicial = signal(false);
    sucursalService = inject(SucursalService);
    puntoService = inject(PuntoService);
    authService = inject(AuthService);

    sucursalId = signal<number | 'TODOS'>('TODOS');
    sucursalOptions = signal<any[]>([]);
    puntoId = signal<number | 'TODOS'>('TODOS');
    puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);


    // Modales de Detalle
    selectedVenta = signal<any>(null);
    isVentaModalOpen = signal(false);

    selectedCompraId = signal<number | null>(null);
    isCompraModalOpen = signal(false);

    selectedMovimiento = signal<any>(null);
    isMovimientoModalOpen = signal(false);

    // Filtro Rápido por Tipo (Aplica a Pendientes e Historial)
    filtroTipoRapido = signal<'TODOS' | 'COMPRA' | 'VENTA' | 'CREDITO' | 'MOVIMIENTO'>('TODOS');

    // Paginación y Filtrado Pendientes
    paginaActualPendientes = signal(0);
    tamanoPaginaPendientes = signal(10);
    totalRegistrosPendientes = signal(0);
    solicitudesPaginadas = signal<SolicitudAnulacionDTO[]>([]);
    totalPagesPendientes = computed(() => Math.ceil(this.totalRegistrosPendientes() / this.tamanoPaginaPendientes()));

    // Paginación y Filtrado Historial
    paginaActualHistorial = signal(0);
    tamanoPaginaHistorial = signal(10);
    totalRegistrosHistorial = signal(0);
    historialPaginado = signal<SolicitudAnulacionDTO[]>([]);
    totalPagesHistorial = computed(() => Math.ceil(this.totalRegistrosHistorial() / this.tamanoPaginaHistorial()));

    // Advanced Filter variables
    searchTerm = signal('');
    activeFiltersCount = signal(0);
    defaultSucursalId = signal<number | 'TODOS'>('TODOS');
    fechaDesde = signal<string>('');
    fechaHasta = signal<string>('');
    serieFilter = signal<string>('');
    numeroFilter = signal<string>('');
    tipoDocumentoFilter = signal<string>('TODOS');



    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Configuración', url: '/configuracion' },
        { label: 'Aprobaciones', url: '/aprobaciones' }
    ];

    ngOnInit(): void {
        this.cargarSucursales();
    }

    cargarSucursales(): void {
        this.sucursalService.getActivas().subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const ops = [{ label: 'Todas', value: 'TODOS' }];
                    res.data.forEach((s: any) => {
                        ops.push({ label: s.nombreSucursal, value: s.idSucursal });
                    });
                    this.sucursalOptions.set(ops);
                    const defaultSucId = this.authService.getSucursalIdFromToken();
                    this.defaultSucursalId.set(defaultSucId || 'TODOS');
                    this.sucursalId.set(defaultSucId || 'TODOS');
                    this.cargarPuntos(this.sucursalId());
                    if (this.tabActiva() === 'pendientes') {
                        this.cargarSolicitudes();
                    } else {
                        this.cargarHistorial();
                    }
                }
            }
        });
    }

    cargarPuntos(idSucursal: number | 'TODOS'): void {
        if (idSucursal === 'TODOS') {
            this.puntoOptions.set([{ label: 'Todos', value: 'TODOS' }]);
            return;
        }
        this.puntoService.listarTodosSinPaginacion(idSucursal as number).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const ops = [{ label: 'Todos', value: 'TODOS' }];
                    res.data.forEach((p: any) => {
                        ops.push({ label: p.nombrePunto || p.nombre, value: p.idPunto });
                    });
                    this.puntoOptions.set(ops);
                }
            }
        });
    }

    onFiltersApplied(filters: AdvancedFilters) {
        this.sucursalId.set(filters.sucursalId);
        this.puntoId.set(filters.puntoVentaId);
        this.fechaDesde.set(filters.fechaDesde);
        this.fechaHasta.set(filters.fechaHasta);
        this.serieFilter.set(filters.serie || '');
        this.numeroFilter.set(filters.numeroComprobante || '');
        this.tipoDocumentoFilter.set(filters.tipoDocumento || 'TODOS');

        const tipoMap: { [key: string]: string } = {
            'TODOS': 'TODOS',
            'COMPRA': 'COMPRA',
            'VENTA': 'VENTA',
            'CREDITO_DEBITO': 'CREDITO',
            'OTROS': 'MOVIMIENTO'
        };
        if (filters.tipoMovimiento) {
            this.filtroTipoRapido.set(tipoMap[filters.tipoMovimiento] as any || 'TODOS');
        }

        if (this.tabActiva() === 'pendientes') {
            this.cargarSolicitudes(this.searchTerm(), 'ALL');
        } else {
            this.cargarHistorial(this.searchTerm(), 'ALL');
        }
    }

    onGlobalSearch(term: string) {
        this.searchTerm.set(term);
        if (this.tabActiva() === 'pendientes') {
            this.cargarSolicitudes(term, 'ALL');
        } else {
            this.cargarHistorial(term, 'ALL');
        }
    }

    cargarSolicitudes(q?: string, type?: string) {
        if (!q) {
            this.loading.set(true);
        }
        this.aprobacionService.obtenerPendientes(
            q,
            type,
            this.paginaActualPendientes(),
            this.tamanoPaginaPendientes(),
            this.sucursalId(),
            this.puntoId()
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    this.solicitudes.set(res.data);
                    if (!q) {
                        this.tieneSolicitudesIniciales.set(res.data.length > 0);
                    }
                    this.aplicarFiltroYPaginaPendientes();
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    cargarHistorial(q?: string, type?: string) {
        if (!q) {
            this.loading.set(true);
        }
        this.aprobacionService.obtenerHistorial(
            q,
            type,
            this.paginaActualHistorial(),
            this.tamanoPaginaHistorial(),
            this.sucursalId(),
            this.puntoId()
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    // Ordenar por fecha de atención descendente
                    const sorted = res.data.sort((a, b) =>
                        new Date(b.fechaAtiende || b.fechaSolicitud).getTime() -
                        new Date(a.fechaAtiende || a.fechaSolicitud).getTime()
                    );
                    this.historial.set(sorted);
                    if (!q) {
                        this.tieneHistorialInicial.set(res.data.length > 0);
                    }
                    this.aplicarFiltroYPaginaHistorial();
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    setTab(tab: 'pendientes' | 'historial') {
        this.tabActiva.set(tab);
        if (tab === 'pendientes') {
            this.cargarSolicitudes(this.searchTerm(), 'ALL');
        } else {
            this.cargarHistorial(this.searchTerm(), 'ALL');
        }
    }

    setFiltroTipoRapido(filtro: 'TODOS' | 'COMPRA' | 'VENTA' | 'CREDITO' | 'MOVIMIENTO') {
        this.filtroTipoRapido.set(filtro);
        this.paginaActualPendientes.set(0);
        this.paginaActualHistorial.set(0);
        if (this.tabActiva() === 'pendientes') {
            this.aplicarFiltroYPaginaPendientes();
        } else {
            this.aplicarFiltroYPaginaHistorial();
        }
    }

    aplicarFiltroYPaginaPendientes(): void {
        let filtrados = this.solicitudes();
        
        const fIni = this.fechaDesde();
        const fFin = this.fechaHasta();
        if (fIni) {
            filtrados = filtrados.filter(s => {
                const dateStr = s.fechaSolicitud;
                if (!dateStr) return false;
                const d = new Date(dateStr).toISOString().split('T')[0];
                return d >= fIni;
            });
        }
        if (fFin) {
            filtrados = filtrados.filter(s => {
                const dateStr = s.fechaSolicitud;
                if (!dateStr) return false;
                const d = new Date(dateStr).toISOString().split('T')[0];
                return d <= fFin;
            });
        }
        
        const serie = this.serieFilter().trim().toLowerCase();
        if (serie) {
            filtrados = filtrados.filter(s => s.documentoReferencia && s.documentoReferencia.toLowerCase().includes(serie));
        }
        
        const numero = this.numeroFilter().trim().toLowerCase();
        if (numero) {
            filtrados = filtrados.filter(s => s.documentoReferencia && s.documentoReferencia.toLowerCase().includes(numero));
        }

        const tipoDoc = this.tipoDocumentoFilter();
        if (tipoDoc && tipoDoc !== 'TODOS') {
            filtrados = filtrados.filter(s => s.tipoDocumento && s.tipoDocumento.toLowerCase() === tipoDoc.toLowerCase());
        }

        const f = this.filtroTipoRapido();
        if (f !== 'TODOS') {
            if (f === 'CREDITO') {
                filtrados = filtrados.filter(s => s.tipoDocumento && (s.tipoDocumento.toUpperCase().includes('CRÉDITO') || s.tipoDocumento.toUpperCase().includes('CREDITO') || s.tipoDocumento.toUpperCase().includes('DÉBITO')));
            } else if (f === 'MOVIMIENTO') {
                filtrados = filtrados.filter(s => s.tipo === 'MOVIMIENTO_DIVERSO');
            } else {
                filtrados = filtrados.filter(s => s.tipo === f);
            }
        }

        this.totalRegistrosPendientes.set(filtrados.length);
        const inicio = this.paginaActualPendientes() * this.tamanoPaginaPendientes();
        const fin = inicio + this.tamanoPaginaPendientes();
        this.solicitudesPaginadas.set(filtrados.slice(inicio, fin));
    }

    onPageChangePendientes(page: number): void {
        this.paginaActualPendientes.set(page);
        this.cargarSolicitudes(this.searchTerm(), 'ALL');
    }

    onPageSizeChangePendientes(size: number): void {
        this.tamanoPaginaPendientes.set(size);
        this.paginaActualPendientes.set(0);
        this.cargarSolicitudes(this.searchTerm(), 'ALL');
    }

    onPageChangeHistorial(page: number): void {
        this.paginaActualHistorial.set(page);
        this.cargarHistorial(this.searchTerm(), 'ALL');
    }

    onPageSizeChangeHistorial(size: number): void {
        this.tamanoPaginaHistorial.set(size);
        this.paginaActualHistorial.set(0);
        this.cargarHistorial(this.searchTerm(), 'ALL');
    }

    aplicarFiltroYPaginaHistorial(): void {
        let filtrados = this.historial();
        
        const fIni = this.fechaDesde();
        const fFin = this.fechaHasta();
        if (fIni) {
            filtrados = filtrados.filter(s => {
                const dateStr = s.fechaAtiende || s.fechaSolicitud;
                if (!dateStr) return false;
                const d = new Date(dateStr).toISOString().split('T')[0];
                return d >= fIni;
            });
        }
        if (fFin) {
            filtrados = filtrados.filter(s => {
                const dateStr = s.fechaAtiende || s.fechaSolicitud;
                if (!dateStr) return false;
                const d = new Date(dateStr).toISOString().split('T')[0];
                return d <= fFin;
            });
        }
        
        const serie = this.serieFilter().trim().toLowerCase();
        if (serie) {
            filtrados = filtrados.filter(s => s.documentoReferencia && s.documentoReferencia.toLowerCase().includes(serie));
        }
        
        const numero = this.numeroFilter().trim().toLowerCase();
        if (numero) {
            filtrados = filtrados.filter(s => s.documentoReferencia && s.documentoReferencia.toLowerCase().includes(numero));
        }

        const tipoDoc = this.tipoDocumentoFilter();
        if (tipoDoc && tipoDoc !== 'TODOS') {
            filtrados = filtrados.filter(s => s.tipoDocumento && s.tipoDocumento.toLowerCase() === tipoDoc.toLowerCase());
        }
        
        const f = this.filtroTipoRapido();
        if (f !== 'TODOS') {
            if (f === 'CREDITO') {
                filtrados = filtrados.filter(s => s.tipoDocumento && (s.tipoDocumento.toUpperCase().includes('CRÉDITO') || s.tipoDocumento.toUpperCase().includes('CREDITO') || s.tipoDocumento.toUpperCase().includes('DÉBITO')));
            } else if (f === 'MOVIMIENTO') {
                filtrados = filtrados.filter(s => s.tipo === 'MOVIMIENTO_DIVERSO');
            } else {
                filtrados = filtrados.filter(s => s.tipo === f);
            }
        }

        this.totalRegistrosHistorial.set(filtrados.length);
        const inicio = this.paginaActualHistorial() * this.tamanoPaginaHistorial();
        const fin = inicio + this.tamanoPaginaHistorial();
        this.historialPaginado.set(filtrados.slice(inicio, fin));
    }


    procesar(solicitud: SolicitudAnulacionDTO, aprobada: boolean) {
        const accion = aprobada ? 'aprobar' : 'rechazar';
        this.alertService.prompt(
            `¿${aprobada ? 'Aprobar' : 'Rechazar'} solicitud?`,
            `Ingrese una observación para el documento ${solicitud.documentoReferencia}:`,
            'text'
        ).then((result) => {
            if (result.isConfirmed) {
                const observacion = result.value || (aprobada ? 'Aprobado' : 'Rechazado');
                this.loading.set(true);
                this.aprobacionService.atender(solicitud.id, aprobada, observacion).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.success('Procesado', `La solicitud ha sido ${aprobada ? 'aprobada' : 'rechazada'}.`);
                            this.cargarSolicitudes();
                        }
                        this.loading.set(false);
                    },
                    error: (err) => {
                        this.loading.set(false);
                        this.alertService.error('Error', err.error?.message || 'No se pudo procesar la solicitud.');
                    }
                });
            }
        });
    }

    verDetalle(solicitud: SolicitudAnulacionDTO) {
        if (solicitud.tipo === 'VENTA' || (!solicitud.tipoDocumento && solicitud.tipo !== 'COMPRA' && solicitud.tipo !== 'MOVIMIENTO_DIVERSO') || solicitud.tipoDocumento === 'Factura' || solicitud.tipoDocumento === 'Boleta' || solicitud.tipoDocumento === 'Nota de Crédito' || solicitud.tipoDocumento === 'Nota de Débito') {
            this.loading.set(true);
            this.ventaService.obtenerPorId(solicitud.referenciaId).subscribe({
                next: (res) => {
                    this.loading.set(false);
                    if (res.success) {
                        this.selectedVenta.set(res.data);
                        this.isVentaModalOpen.set(true);
                    } else {
                        this.alertService.error('Error', 'No se pudieron cargar los detalles de la venta.');
                    }
                },
                error: (err) => {
                    this.loading.set(false);
                    this.alertService.error('Error', 'Error al obtener detalles.');
                }
            });
        } else if (solicitud.tipo === 'COMPRA') {
            this.selectedCompraId.set(solicitud.referenciaId);
            this.isCompraModalOpen.set(true);
        } else if (solicitud.tipo === 'MOVIMIENTO_DIVERSO') {
            this.selectedMovimiento.set(solicitud);
            this.isMovimientoModalOpen.set(true);
        } else {
            this.alertService.toast('Detalles para este tipo de documento aún no están disponibles.', 'info');
        }
    }
}
