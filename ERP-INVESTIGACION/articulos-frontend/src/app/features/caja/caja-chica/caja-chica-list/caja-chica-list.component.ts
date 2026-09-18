import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaChica, Movimiento } from '../../models/caja-chica.model';
import { CajaChicaService } from '../../services/caja-chica.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { AuthService } from '../../../auth/services/auth.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { CajaChicaFormComponent } from '../caja-chica-form/caja-chica-form.component';
import { MetodoPagoService } from '../../../almacen/service/atributo.service';
import { CajaArqueoModalComponent } from '../../arqueo-list/caja-arqueo-modal/caja-arqueo-modal.component';

@Component({
    selector: 'app-caja-chica-list',
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
        ModalComponent,
        BreadcrumbComponent,
        CajaChicaFormComponent,
        CajaArqueoModalComponent
    ],
    templateUrl: './caja-chica-list.component.html'
})
export class CajaChicaListComponent implements OnInit {
    movimientos = signal<Movimiento[]>([]);
    cajaActual = signal<CajaChica | null>(null);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    showModalMovimiento = signal(false);
    showModalCrearCaja = signal(false);
    nombreNuevaCaja = 'Caja General';
    tipoMovimientoSeleccionado = signal<'INGRESO' | 'EGRESO'>('INGRESO');

    totalIngresos = signal(0);
    totalEgresos = signal(0);
    saldoFinal = signal(0);

    // Totales dinámicos
    metodosPago = signal<any[]>([]);
    totalesPorMetodo = signal<{ [key: string]: number }>({});
    resumenExpandido = signal(true);

    saldoInicialCaja = signal(0);
    showArqueoModal = signal(false);
    selectedCajaId = signal<number | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja', route: '/caja' },
        { label: 'Caja Chica' }
    ];

    columns: Columna[] = [
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'tipo', header: 'Tipo', tipo: 'caja-status', subField: [] },
        { field: 'monto', header: 'Monto', tipo: 'currency', subField: [] },
        { field: 'metodoPago', header: 'Método de Pago', tipo: 'text', subField: [] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text', subField: [] },
        { field: 'referencia', header: 'Referencia', tipo: 'text', subField: [] },
        { field: 'usuario', header: 'Usuario', tipo: 'text', subField: [] }
    ];

    constructor(
        private cajaChicaService: CajaChicaService,
        private alertService: AlertService,
        private authService: AuthService,
        private metodoPagoService: MetodoPagoService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarMetodosPago();
    }

    cargarMetodosPago(): void {
        this.metodoPagoService.listarActivos().subscribe({
            next: (res) => {
                if (res.success && res.data?.content) {
                    this.metodosPago.set(res.data.content);
                }
                this.cargarCajaYMovimientos();
            },
            error: () => {
                this.cargarCajaYMovimientos();
            }
        });
    }

    cargarCajaYMovimientos(): void {
        this.loading.set(true);
        this.cajaChicaService.listarCajas().subscribe({
            next: (res) => {
                if (res.success && res.data.length > 0) {
                    // Priorizar mostrar la caja abierta si existe
                    const cajaAbierta = res.data.find(c => c.estado === 'ABIERTA');
                    const cajaAMostrar = cajaAbierta || res.data[0];

                    this.cajaActual.set(cajaAMostrar);
                    this.cargarMovimientos(cajaAMostrar.id!);
                } else {
                    this.cajaActual.set(null);
                    this.loading.set(false);
                }
            },
            error: () => this.loading.set(false)
        });
    }

    cargarResumen(cajaId: number): void {
        this.cajaChicaService.obtenerResumen(cajaId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const resumen = res.data;
                    const metodos = this.metodosPago();
                    const totales: { [key: string]: number } = {};

                    // Inicializar en 0 para cada método
                    metodos.forEach(m => {
                        totales[m.descripcion.toUpperCase()] = 0;
                    });

                    // Sumar ingresos de cada método de pago
                    resumen.ingresos.forEach(i => {
                        const key = i.metodoPago.toUpperCase();
                        totales[key] = (totales[key] || 0) + i.monto;
                    });

                    // Restar egresos de cada método de pago
                    resumen.egresos.forEach(e => {
                        const key = e.metodoPago.toUpperCase();
                        totales[key] = (totales[key] || 0) - e.monto;
                    });

                    this.totalIngresos.set(resumen.totalIngresos);
                    this.totalEgresos.set(resumen.totalEgresos);
                    this.saldoFinal.set(resumen.saldoFinalTeorico);
                    this.totalesPorMetodo.set(totales);
                }
            }
        });
    }

    cargarMovimientos(cajaId: number): void {
        this.cajaChicaService.listarMovimientos(cajaId, this.currentPage(), this.pageSize()).subscribe({
            next: (res) => {
                if (res.success) {
                    this.movimientos.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                    this.cargarResumen(cajaId);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        if (this.cajaActual()) {
            this.cargarMovimientos(this.cajaActual()!.id!);
        }
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        if (this.cajaActual()) {
            this.cargarMovimientos(this.cajaActual()!.id!);
        }
    }

    abrirModalMovimiento(tipo: 'INGRESO' | 'EGRESO'): void {
        this.tipoMovimientoSeleccionado.set(tipo);
        this.showModalMovimiento.set(true);
    }

    cerrarModal(): void {
        this.showModalMovimiento.set(false);
    }

    onMovimientoGuardado(): void {
        this.cerrarModal();
        this.alertService.toast('Movimiento registrado correctamente', 'success');
        this.cargarCajaYMovimientos();
    }

    abrirModalCrearCaja(): void {
        this.saldoInicialCaja.set(0);
        this.nombreNuevaCaja = 'Caja General';
        this.showModalCrearCaja.set(true);
    }

    confirmarAbrirCaja(): void {
        const sucursalId = this.authService.getSucursalIdFromToken();
        const puntoId = this.authService.getPuntoIdFromToken();
        if (!sucursalId || !puntoId) {
            this.alertService.toast('No se pudo identificar la sucursal o punto de venta', 'error');
            return;
        }

        this.loading.set(true);
        this.cajaChicaService.crearCaja({
            nombre: this.nombreNuevaCaja,
            idSucursal: sucursalId,
            idPuntoVenta: puntoId,
            saldoInicial: this.saldoInicialCaja()
        }).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.toast('Caja abierta correctamente', 'success');
                    this.showModalCrearCaja.set(false);
                    this.cargarCajaYMovimientos();
                } else {
                    this.alertService.toast(res.message || 'Error al abrir caja', 'error');
                    this.loading.set(false);
                }
            },
            error: () => {
                this.alertService.toast('Error de conexión', 'error');
                this.loading.set(false);
            }
        });
    }

    cerrarCaja(): void {
        const caja = this.cajaActual();
        if (!caja || !caja.id) return;
        this.selectedCajaId.set(caja.id);
        this.showArqueoModal.set(true);
    }

    onArqueoDone(success: boolean): void {
        this.showArqueoModal.set(false);
        if (success) {
            this.cargarCajaYMovimientos();
        }
    }

    anularMovimiento(mov: Movimiento): void {
        this.alertService.confirm(
            '¿Está seguro de anular este movimiento?',
            'Esta acción revertirá el impacto en el saldo actual.',
            'warning'
        ).then(result => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.cajaChicaService.eliminarMovimiento(mov.id!).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.toast('Movimiento anulado', 'success');
                            this.cargarCajaYMovimientos();
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

    confirmarCierreConArqueo(): void {
        const caja = this.cajaActual();
    }
}
