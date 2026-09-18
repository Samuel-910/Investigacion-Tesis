import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaGeneralService, CajaGeneralMovimiento, CajaGeneral } from '../../../../core/services/caja-general.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { AuthService } from '../../../auth/services/auth.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { CajaGeneralFormComponent } from '../caja-general-form/caja-general-form.component';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';


@Component({
    selector: 'app-caja-general-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        ModalComponent,
        CajaGeneralFormComponent,
        SearchGenericComponent
    ],
    templateUrl: './caja-general-list.component.html'
})
export class CajaGeneralListComponent implements OnInit {
    private cajaGeneralService = inject(CajaGeneralService);
    private alertService = inject(AlertService);
    public sidebarService = inject(SidebarService);
    private authService = inject(AuthService);

    movimientos = signal<CajaGeneralMovimiento[]>([]);
    caja = signal<CajaGeneral | null>(null);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);
    showModal = signal(false);
    movimientoSeleccionado = signal<CajaGeneralMovimiento | null>(null);
    metodoPagoSeleccionado = signal<string | null>(null);
    startIndex = signal(0);
    filtroMetodoPago = signal('');
    searchTerm = signal('');
    searchType = signal('ALL');

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Descripción', value: 'DESCRIPCION' },
        { label: 'Referencia', value: 'REFERENCIA' },
        { label: 'Usuario', value: 'USUARIO' },
        { label: 'Tipo', value: 'TIPO' }
    ];

    metodosFiltrados = computed(() => {
        const saldos = this.caja()?.saldosPorMetodo || [];
        const query = this.filtroMetodoPago().toLowerCase().trim();
        if (!query) return saldos;
        return saldos.filter(s => s.metodoPago.toLowerCase().includes(query));
    });

    saldosPaginados = computed(() => {
        const saldos = this.metodosFiltrados();
        const start = this.startIndex();
        return saldos.slice(start, start + 4);
    });

    movimientosFiltrados = computed(() => {
        return this.movimientos();
    });

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja', route: '/caja' },
        { label: 'Caja General' }
    ];

    columns: Columna[] = [
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
        {
            field: 'tipo', header: 'Tipo', tipo: 'caja-status', subField: [], badgeConfig: {
                from: 'from-blue-100', to: 'to-blue-200', text: 'text-blue-800', border: 'border-blue-300'
            }
        },
        { field: 'monto', header: 'Monto', tipo: 'currency', subField: [] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text', subField: [] },
        { field: 'metodoPago', header: 'Método Pago', tipo: 'text', subField: [] },
        { field: 'referencia', header: 'Referencia', tipo: 'text', subField: [] },
        { field: 'usuario', header: 'Usuario', tipo: 'text', subField: [] }
    ];

    ngOnInit(): void {
        this.cargarCajaYSaldos();
    }

    get idSucursal(): number {
        return this.authService.getSucursalIdFromToken() || 0;
    }

    cargarCajaYSaldos(): void {
        this.loading.set(true);
        this.cajaGeneralService.obtenerSaldo(this.idSucursal).subscribe({
            next: (res) => {
                if (res.success) {
                    this.caja.set(res.data);
                    this.cargarMovimientos();
                } else {
                    this.loading.set(false);
                }
            },
            error: () => this.loading.set(false)
        });
    }

    cargarMovimientos(): void {
        this.loading.set(true);
        this.cajaGeneralService.listarMovimientos(
            this.idSucursal, 
            this.metodoPagoSeleccionado() || undefined,
            this.searchTerm() || undefined,
            this.searchType() || undefined,
            this.currentPage(), 
            this.pageSize()
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    this.movimientos.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    seleccionarMetodo(metodo: string | null): void {
        this.metodoPagoSeleccionado.set(metodo);
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    filtrarMetodos(query: string): void {
        this.filtroMetodoPago.set(query);
        this.startIndex.set(0);
    }

    onSearch(event: { q: string; type: string }): void {
        this.searchTerm.set(event.q);
        this.searchType.set(event.type);
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    prevSaldos(): void {
        if (this.startIndex() >= 4) {
            this.startIndex.update(s => s - 4);
        } else {
            this.startIndex.set(0);
        }
    }

    nextSaldos(): void {
        const total = this.metodosFiltrados().length;
        if (this.startIndex() + 4 < total) {
            this.startIndex.update(s => s + 4);
        }
    }

    abrirModalMovimiento(): void {
        this.movimientoSeleccionado.set(null);
        this.showModal.set(true);
    }

    editarMovimiento(mov: CajaGeneralMovimiento): void {
        this.movimientoSeleccionado.set(mov);
        this.showModal.set(true);
    }

    anularMovimiento(mov: CajaGeneralMovimiento): void {
        this.alertService.confirm(
            '¿Está seguro de anular este movimiento?',
            'Esta acción revertirá el impacto en el saldo actual.',
            'warning'
        ).then(result => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.cajaGeneralService.eliminarMovimiento(mov.id).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.toast('Movimiento anulado', 'success');
                            this.cargarCajaYSaldos();
                        } else {
                            this.alertService.toast(res.message || 'Error al anular', 'error');
                            this.loading.set(false);
                        }
                    },
                    error: () => {
                        this.alertService.toast('Error de conexión', 'error');
                        this.loading.set(false);
                    }
                });
            }
        });
    }

    cerrarModal(): void {
        this.showModal.set(false);
        this.movimientoSeleccionado.set(null);
    }

    onGuardado(): void {
        this.cerrarModal();
        this.cargarCajaYSaldos();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarMovimientos();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    refresh(): void {
        this.cargarCajaYSaldos();
    }
}
