import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CuentaProveedorService } from '../../../services/cuenta-proveedor.service';
import { ProveedorService } from '../../../services/proveedor.service';
import { HeaderComponent } from '../../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination';
import { AlertService } from '../../../../../core/services/alert.service';
import { SidebarService } from '../../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../../shared/components/breadcrumb/breadcrumb';
import { AuthService } from '../../../../auth/services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { FormInputComponent } from '../../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-cuenta-proveedor-list',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BreadcrumbComponent,
        ReactiveFormsModule,
        ModalComponent,
        FormInputComponent,
        FormSelectComponent
    ],
    templateUrl: './cuenta-proveedor-list.component.html'
})
export class CuentaProveedorListComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private cuentaService = inject(CuentaProveedorService);
    private proveedorService = inject(ProveedorService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    private fb = inject(FormBuilder);
    public sidebarService = inject(SidebarService);

    showModalMovimiento = signal(false);
    tipoMovimiento = signal<'ABONO' | 'CARGO'>('ABONO');
    formMovimiento!: FormGroup;
    metodosPago = [
        { label: 'Efectivo', value: 'EFECTIVO' },
        { label: 'Transferencia', value: 'TRANSFERENCIA' },
        { label: 'Yape', value: 'YAPE' },
        { label: 'Plin', value: 'PLIN' },
        { label: 'Tarjeta de Crédito', value: 'TARJETA_CREDITO' },
        { label: 'Tarjeta de Débito', value: 'TARJETA_DEBITO' }
    ];

    idProveedor = signal<number | null>(null);
    proveedor = signal<any>(null);
    cuenta = signal<any>(null);
    movimientos = signal<any[]>([]);
    deudas = signal<any[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    breadcrumbItems = signal<BreadcrumbItem[]>([]);

    get Math() {
        return Math;
    }

    columns: Columna[] = [
        { field: 'fechaRegistro', header: 'Fecha', tipo: 'date', subField: [] },
        {
            field: 'tipo', header: 'Tipo', tipo: 'badge', subField: [], badgeConfig: {
                from: 'from-blue-100', to: 'to-blue-200', text: 'text-blue-800', border: 'border-blue-300'
            }
        },
        { field: 'monto', header: 'Monto', tipo: 'currency', subField: [] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text', subField: [] }
    ];

    ngOnInit(): void {
        this.initForm();
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.idProveedor.set(+id);
            this.cargarDatos();
        }
    }

    cargarDatos(): void {
        if (!this.idProveedor()) return;

        this.proveedorService.obtenerPorId(this.idProveedor()!).subscribe(res => {
            if (res.success) {
                this.proveedor.set(res.data);
                this.updateBreadcrumbs();
            }
        });

        this.cuentaService.obtenerCuenta(this.idProveedor()!).subscribe(res => {
            if (res.success) {
                this.cuenta.set(res.data);
            }
        });

        this.cuentaService.listarDeudasPendientes(this.idProveedor()!).subscribe(res => {
            if (res.success) {
                this.deudas.set(res.data);
            }
        });

        this.cargarMovimientos();
    }

    cargarMovimientos(): void {
        this.loading.set(true);
        this.cuentaService.listarMovimientos(this.idProveedor()!, this.currentPage(), this.pageSize())
            .subscribe({
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

    updateBreadcrumbs(): void {
        this.breadcrumbItems.set([
            { label: 'Compras', route: '/compra' },
            { label: 'Proveedores', route: '/compra/proveedores' },
            { label: `Cuenta: ${this.proveedor()?.razonSocial || '...'}` }
        ]);
    }

    initForm(): void {
        this.formMovimiento = this.fb.group({
            monto: [null, [Validators.required, Validators.min(0.01)]],
            pagarDesdeCajaGeneral: [false],
            metodoPago: ['EFECTIVO'],
            descripcion: ['', Validators.required]
        });

        // Al cambiar pagarDesdeCajaGeneral, reiniciar método de pago
        this.formMovimiento.get('pagarDesdeCajaGeneral')?.valueChanges.subscribe(val => {
            if (!val) {
                this.formMovimiento.get('metodoPago')?.setValue('EFECTIVO');
            }
        });
    }

    abrirModalAbono(): void {
        this.tipoMovimiento.set('ABONO');
        this.formMovimiento.reset({
            monto: null,
            pagarDesdeCajaGeneral: false,
            metodoPago: 'EFECTIVO',
            descripcion: 'Pago a cuenta'
        });
        this.showModalMovimiento.set(true);
    }

    abrirModalCargo(): void {
        this.tipoMovimiento.set('CARGO');
        this.formMovimiento.reset({
            monto: null,
            pagarDesdeCajaGeneral: false,
            metodoPago: 'EFECTIVO',
            descripcion: 'Cargo por mercadería'
        });
        // En cargo no se suele usar caja general de la misma forma, pero se puede deshabilitar
        this.formMovimiento.get('pagarDesdeCajaGeneral')?.disable();
        this.showModalMovimiento.set(true);
    }

    cerrarModal(): void {
        this.showModalMovimiento.set(false);
    }

    guardarMovimiento(): void {
        if (this.formMovimiento.invalid) {
            this.formMovimiento.markAllAsTouched();
            return;
        }

        const values = this.formMovimiento.getRawValue(); // getRawValue para incluir disabled
        this.cuentaService.registrarMovimiento(this.idProveedor()!, {
            tipo: this.tipoMovimiento(),
            monto: values.monto,
            descripcion: values.descripcion,
            idSucursal: this.authService.getSucursalIdFromToken(),
            pagarDesdeCajaGeneral: values.pagarDesdeCajaGeneral,
            metodoPago: values.metodoPago,
            idUser: this.authService.getUsuarioIdFromToken()
        }).subscribe(res => {
            if (res.success) {
                this.alertService.toast('Movimiento registrado', 'success');
                this.showModalMovimiento.set(false);
                this.cargarDatos();
            }
        });
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

    regresar(): void {
        this.router.navigate(['/compra/proveedores']);
    }
}
