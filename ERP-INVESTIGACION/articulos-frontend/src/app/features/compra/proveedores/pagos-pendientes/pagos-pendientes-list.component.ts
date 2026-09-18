import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CuentaProveedorService } from '../../services/cuenta-proveedor.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';

@Component({
    selector: 'app-pagos-pendientes-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        HeaderComponent,
        SidebarComponent,
        BreadcrumbComponent,
        ModalComponent,
        FormInputComponent,
        FormSelectComponent,
        PaginationComponent,
        TablaGeneralComponent
    ],
    templateUrl: './pagos-pendientes-list.component.html'
})
export class PagosPendientesListComponent implements OnInit {
    private router = inject(Router);
    private cuentaService = inject(CuentaProveedorService);
    public sidebarService = inject(SidebarService);
    private fb = inject(FormBuilder);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);

    deudas = signal<any[]>([]);
    loading = signal(false);
    totalDeuda = signal(0);
    currentPage = signal(0);
    pageSize = signal(10);
    totalPages = signal(0);
    totalElements = signal(0);

    get Math() {
        return Math;
    }

    breadcrumbItems = signal<BreadcrumbItem[]>([
        { label: 'Compras', route: '/compra' },
        { label: 'Proveedores', route: '/compra/proveedores' },
        { label: 'Pagos Pendientes' }
    ]);

    searchTerm = signal('');
    private searchTimeout: any;

    columns: Columna[] = [
        { field: 'proveedorRazonSocial', header: 'Proveedor', tipo: 'text', subField: [] },
        { field: 'referenciaCargo', header: 'Documento/Ref', tipo: 'text', subField: [] },
        { field: 'fechaVencimiento', header: 'Vencimiento', tipo: 'date', subField: [] },
        { field: 'saldoPendiente', header: 'Saldo Pendiente', tipo: 'currency', subField: [] }
    ];

    onSearchChange(value: string): void {
        this.searchTerm.set(value);
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }
        this.searchTimeout = setTimeout(() => {
            this.currentPage.set(0);
            this.cargarDeudas();
        }, 300);
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarDeudas();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarDeudas();
    }

    // Modal Pago Rápido
    showModalMovimiento = signal(false);
    cuentaSeleccionada = signal<any>(null);
    formMovimiento!: FormGroup;
    metodosPago = [
        { label: 'Efectivo', value: 'EFECTIVO' },
        { label: 'Transferencia', value: 'TRANSFERENCIA' },
        { label: 'Yape', value: 'YAPE' },
        { label: 'Plin', value: 'PLIN' },
        { label: 'Tarjeta de Crédito', value: 'TARJETA_CREDITO' },
        { label: 'Tarjeta de Débito', value: 'TARJETA_DEBITO' }
    ];

    ngOnInit(): void {
        this.initForm();
        this.cargarDeudas();
    }

    initForm(): void {
        this.formMovimiento = this.fb.group({
            monto: [null, [Validators.required, Validators.min(0.01)]],
            pagarDesdeCajaGeneral: [true],
            metodoPago: ['EFECTIVO'],
            descripcion: ['Pago a cuenta', Validators.required]
        });

        this.formMovimiento.get('pagarDesdeCajaGeneral')?.valueChanges.subscribe(val => {
            if (!val) {
                this.formMovimiento.get('metodoPago')?.setValue('EFECTIVO');
            }
        });
    }

    cargarDeudas(): void {
        this.loading.set(true);
        this.cuentaService.listarDeudasPendientesPaginadas(this.currentPage(), this.pageSize(), this.searchTerm()).subscribe({
            next: (res) => {
                if (res.success) {
                    this.deudas.set(res.data.content);
                    this.totalPages.set(res.data.totalPages);
                    this.totalElements.set(res.data.totalElements);
                    // The totalDeuda of current page can be calculated, but a global total would need a separate endpoint.
                    // For now, we calculate the sum of debts on the current page to avoid confusion, or leave it blank.
                    this.totalDeuda.set(this.deudas().reduce((acc: number, item: any) => acc + item.saldoPendiente, 0));
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    verDetalle(deuda: any): void {
        this.router.navigate(['/compra/proveedores', deuda.idCuentaProveedor, 'cuenta']);
    }

    abrirPagoRapido(deuda: any): void {
        this.cuentaSeleccionada.set(deuda);
        this.formMovimiento.reset({
            monto: Math.abs(deuda.saldoPendiente), // Sugerir el total de esta deuda
            pagarDesdeCajaGeneral: true,
            metodoPago: 'EFECTIVO',
            descripcion: 'Pago de factura/deuda'
        });
        this.showModalMovimiento.set(true);
    }

    cerrarModal(): void {
        this.showModalMovimiento.set(false);
        this.cuentaSeleccionada.set(null);
    }

    guardarMovimiento(): void {
        if (this.formMovimiento.invalid || !this.cuentaSeleccionada()) {
            this.formMovimiento.markAllAsTouched();
            return;
        }

        const values = this.formMovimiento.getRawValue();
        this.cuentaService.registrarMovimiento(this.cuentaSeleccionada().idCuentaProveedor, {
            tipo: 'ABONO',
            monto: values.monto,
            descripcion: values.descripcion,
            idSucursal: this.authService.getSucursalIdFromToken(),
            pagarDesdeCajaGeneral: values.pagarDesdeCajaGeneral,
            metodoPago: values.metodoPago,
            idUser: this.authService.getUsuarioIdFromToken()
        }).subscribe(res => {
            if (res.success) {
                this.alertService.toast('Pago registrado correctamente', 'success');
                this.showModalMovimiento.set(false);
                this.cargarDeudas();
            }
        });
    }

    exportarExcel(): void {
        let csv = 'PROVEEDOR,DOCUMENTO,EMISION,VENCIMIENTO,SALDO PENDIENTE\n';
        this.deudas().forEach(d => {
            csv += `"${d.proveedorRazonSocial}",="${d.referenciaCargo || ''}","${d.fechaEmision}","${d.fechaVencimiento}",${Math.abs(d.saldoPendiente)}\n`;
        });
        const blob = new Blob([new Uint8Array([0xEF, 0xBB, 0xBF]), csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `Pagos_Pendientes_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    }
}
