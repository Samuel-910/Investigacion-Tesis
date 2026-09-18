import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../auth/services/auth.service';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { TransferenciaSucursal, EstadoTransferencia } from '../../models/transferencia.model';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertService } from '../../../../core/services/alert.service';

// Sub-componentes
import { TransferenciaSolicitarComponent } from '../transferencia-form/transferencia-solicitar.component';
import { TransferenciaDetalleComponent } from '../transferencia-detalle/transferencia-detalle.component';
import { TransferenciaService } from '../../service/transferencia.service';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-transferencia-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        ModalComponent,
        TransferenciaSolicitarComponent,
        TransferenciaDetalleComponent,
        SearchGenericComponent,
        PaginationComponent
    ],
    templateUrl: './transferencia-list.component.html',
})
export class TransferenciaListComponent implements OnInit {
    private transferenciaService = inject(TransferenciaService);
    private authService = inject(AuthService);
    private sucursalService = inject(SucursalService);
    private alertService = inject(AlertService);
    public sidebarService = inject(SidebarService);

    transferencias = signal<TransferenciaSucursal[]>([]);
    loading = signal<boolean>(false);
    sucursales = signal<any[]>([]);
    estadoFiltro = signal<string>('TODAS');

    // Control de Búsqueda y Paginación
    tieneTransferenciasIniciales = signal<boolean>(false);
    paginaActual = signal<number>(0);
    tamanoPagina = signal<number>(10);
    searchQuery = signal<string>('');
    searchType = signal<string>('ALL');

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'ID', value: 'ID' },
        { label: 'Sucursal Origen', value: 'ORIGEN' },
        { label: 'Sucursal Destino', value: 'DESTINO' },
        { label: 'Motivo', value: 'MOTIVO' },
        { label: 'Estado', value: 'ESTADO' }
    ];

    transferenciasPaginadas = signal<TransferenciaSucursal[]>([]);

    // Modales
    showModalSolicitar = signal<boolean>(false);
    showModalDetalle = signal<boolean>(false);
    idTransferenciaSeleccionada = signal<number | null>(null);

    countPendientes = computed(() => this.transferencias().filter(t => this.getEstadoNombre(t.estado) === 'SOLICITADO' || this.getEstadoNombre(t.estado) === 'ENVIADO').length);
    countAceptadas = computed(() => this.transferencias().filter(t => this.getEstadoNombre(t.estado) === 'RECIBIDO').length);
    countCanceladas = computed(() => this.transferencias().filter(t => this.getEstadoNombre(t.estado) === 'CANCELADO').length);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Transferencias entre Sucursales' }
    ];

    // Totales calculados de manera reactiva para el paginador
    totalRegistros = signal<number>(0);
    totalPages = computed(() => Math.ceil(this.totalRegistros() / this.tamanoPagina()));

    ngOnInit() {
        this.cargarSucursales();
        this.cargarTransferencias();
    }

    cargarSucursales() {
        this.sucursalService.getActivas().subscribe(res => {
            if (res.success) {
                this.sucursales.set(res.data);
            }
        });
    }

    cargarTransferencias(isEventTriggered: boolean = false) {
        if (!isEventTriggered) {
            this.loading.set(true);
        }
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (!sucursalId) {
            this.loading.set(false);
            return;
        }

        this.transferenciaService.listar(
            sucursalId,
            this.searchQuery(),
            this.searchType(),
            this.paginaActual(),
            this.tamanoPagina()
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    this.transferencias.set(res.data);
                    if (!isEventTriggered && this.searchQuery() === '') {
                        this.tieneTransferenciasIniciales.set(res.data.length > 0);
                    }
                    this.aplicarFiltroYPaginaTransferencias();
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    getNombreSucursal(id: number) {
        const suc = this.sucursales().find(s => s.idSucursal === id);
        return suc ? suc.nombreSucursal : `Sucursal ${id}`;
    }

    aplicarFiltroYPaginaTransferencias(): void {
        const t = this.transferencias();
        const f = this.estadoFiltro();
        const q = this.searchQuery().toLowerCase().trim();
        const type = this.searchType();

        // 1. Filtrar por estado
        let filtrados = t;
        if (f !== 'TODAS') {
            filtrados = t.filter(item => {
                const estadoName = this.getEstadoNombre(item.estado);
                if (f === 'PENDIENTES') return estadoName === 'SOLICITADO' || estadoName === 'ENVIADO';
                if (f === 'ACEPTADAS') return estadoName === 'RECIBIDO';
                if (f === 'CANCELADAS') return estadoName === 'CANCELADO';
                return true;
            });
        }

        // 2. Filtrar por búsqueda de texto
        if (q) {
            filtrados = filtrados.filter(item => {
                const idMatch = item.id?.toString().includes(q);
                const origenMatch = (item.sucursalOrigenNombre || '').toLowerCase().includes(q) || item.idSucursalOrigen?.toString().includes(q);
                const destinoMatch = (item.sucursalDestinoNombre || '').toLowerCase().includes(q) || item.idSucursalDestino?.toString().includes(q);
                const motivoMatch = (item.motivo || '').toLowerCase().includes(q);
                const estadoMatch = this.getEstadoNombre(item.estado).toLowerCase().includes(q);

                if (type === 'ID') return idMatch;
                if (type === 'ORIGEN') return origenMatch;
                if (type === 'DESTINO') return destinoMatch;
                if (type === 'MOTIVO') return motivoMatch;
                if (type === 'ESTADO') return estadoMatch;

                return idMatch || origenMatch || destinoMatch || motivoMatch || estadoMatch;
            });
        }

        // 3. Establecer total
        this.totalRegistros.set(filtrados.length);

        // 4. Segmentar para la página activa
        const inicio = this.paginaActual() * this.tamanoPagina();
        const fin = inicio + this.tamanoPagina();
        this.transferenciasPaginadas.set(filtrados.slice(inicio, fin));
    }

    // --- Acciones de Modales ---
    nuevaTransferencia() {
        this.showModalSolicitar.set(true);
    }

    verDetalle(t: TransferenciaSucursal) {
        this.idTransferenciaSeleccionada.set(t.id!);
        this.showModalDetalle.set(true);
    }

    onSolicitudGuardada() {
        this.showModalSolicitar.set(false);
        this.cargarTransferencias();
    }

    onAccionDetalleCompletada() {
        this.showModalDetalle.set(false);
        this.cargarTransferencias();
    }

    esOrigen(t: TransferenciaSucursal): boolean {
        return t.idSucursalOrigen === this.authService.getSucursalIdFromToken();
    }

    cambiarFiltro(estado: string) {
        this.estadoFiltro.set(estado);
        this.paginaActual.set(0); // Reiniciar paginación al cambiar filtro
        this.cargarTransferencias();
    }

    handleSearch(event: { q: string, type: string }) {
        this.searchQuery.set(event.q);
        this.searchType.set(event.type);
        this.paginaActual.set(0); // Reiniciar paginación al buscar
        this.cargarTransferencias(true);
    }

    onPageChange(page: number) {
        this.paginaActual.set(page);
        this.cargarTransferencias();
    }

    onPageSizeChange(size: number) {
        this.tamanoPagina.set(Number(size));
        this.paginaActual.set(0); // Reiniciar paginación al cambiar tamaño
        this.cargarTransferencias();
    }

    getEstadoNombre(estado: any): string {
        if (!estado) return '';
        if (typeof estado === 'string') return estado;
        if (typeof estado === 'object') return estado.name || estado.valor?.toString() || '';
        return String(estado);
    }
}
