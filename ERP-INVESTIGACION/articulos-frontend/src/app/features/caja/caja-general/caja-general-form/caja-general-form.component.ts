import { Component, EventEmitter, Input, OnInit, Output, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CajaGeneralService, CajaGeneralMovimiento, CajaGeneralSaldo } from '../../../../core/services/caja-general.service';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { AlertService } from '../../../../core/services/alert.service';
import { MetodoPagoService } from '../../../almacen/service/atributo.service';
import { AuthService } from '../../../auth/services/auth.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { SelectOption } from '../../../../shared/components/forms/form-select/form-select.component';
import { FormTextareaComponent } from '../../../../shared/components/forms/form-textarea/form-textarea.component';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';

@Component({
    selector: 'app-caja-general-form',
    standalone: true,
    imports: [
        CommonModule, 
        ReactiveFormsModule, 
        PrimaryButtonComponent, 
        FormInputComponent, 
        SearchableSelectComponent, 
        FormTextareaComponent
    ],
    templateUrl: './caja-general-form.component.html'
})
export class CajaGeneralFormComponent implements OnInit {
    @Input() idSucursal!: number;
    private _movimientoSeleccionado: CajaGeneralMovimiento | null = null;

    @Input() set movimientoSeleccionado(value: CajaGeneralMovimiento | null | undefined) {
        this._movimientoSeleccionado = value || null;
        if (this.form) {
            this.cargarDatosEnFormulario();
        }
    }

    get movimientoSeleccionado(): CajaGeneralMovimiento | null {
        return this._movimientoSeleccionado;
    }
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    private fb = inject(FormBuilder);
    private cajaGeneralService = inject(CajaGeneralService);
    private alertService = inject(AlertService);
    private metodoPagoService = inject(MetodoPagoService);
    private authService = inject(AuthService);

    form!: FormGroup;
    loading = signal(false);
    metodosPago = signal<string[]>([]);
    saldos = signal<CajaGeneralSaldo[]>([]);
    tiposMovimiento = [
        { value: 'INGRESO', label: 'Ingreso', color: 'text-emerald-600', bg: 'bg-emerald-50', icon: 'fa-arrow-up' },
        { value: 'EGRESO', label: 'Egreso', color: 'text-rose-600', bg: 'bg-rose-50', icon: 'fa-arrow-down' },
        { value: 'TRANSFERENCIA_ENTRE_METODOS', label: 'Transferencia', color: 'text-indigo-600', bg: 'bg-indigo-50', icon: 'fa-exchange-alt' }
    ];

    metodoOrigenSeleccionado = signal<string>('EFECTIVO');
    metodoDestinoSeleccionado = signal<string>('');

    metodoPagoOrigenOptions = computed<SelectOption[]>(() => 
        this.metodosPago()
            .filter(m => m !== this.metodoDestinoSeleccionado())
            .map(m => ({ label: m, value: m }))
    );

    metodoPagoDestinoOptions = computed<SelectOption[]>(() => 
        this.metodosPago()
            .filter(m => m !== this.metodoOrigenSeleccionado())
            .map(m => ({ label: m, value: m }))
    );

    metodoPagoSeleccionado = signal<string>('EFECTIVO');

    saldoDisponible = computed(() => {
        const metodo = this.metodoPagoSeleccionado();
        if (!metodo) return 0;
        const item = this.saldos().find(s => s.metodoPago.toUpperCase() === metodo.toUpperCase());
        return item ? item.saldo : 0;
    });

    ngOnInit(): void {
        this.initForm();
        this.cargarMetodosPago();
        this.cargarDatosEnFormulario();
    }

    private cargarDatosEnFormulario(): void {
        if (this.movimientoSeleccionado) {
            this.form.patchValue({
                tipo: this.movimientoSeleccionado.tipo,
                monto: this.movimientoSeleccionado.monto,
                descripcion: this.movimientoSeleccionado.descripcion,
                metodoPago: this.movimientoSeleccionado.metodoPago,
                referencia: this.movimientoSeleccionado.referencia
            });
            this.metodoPagoSeleccionado.set(this.movimientoSeleccionado.metodoPago);
            this.metodoOrigenSeleccionado.set(this.movimientoSeleccionado.metodoPago);
            this.metodoDestinoSeleccionado.set('');
        } else {
            this.form?.reset({
                tipo: 'INGRESO',
                monto: null,
                descripcion: '',
                metodoPago: 'EFECTIVO',
                referencia: ''
            });
            this.metodoPagoSeleccionado.set('EFECTIVO');
            this.metodoOrigenSeleccionado.set('EFECTIVO');
            this.metodoDestinoSeleccionado.set('');
        }
    }

    private cargarMetodosPago(): void {
        this.metodoPagoService.listarActivos().subscribe({
            next: (res) => {
                if (res.success && res.data?.content) {
                    this.metodosPago.set(res.data.content.map(m => m.descripcion.toUpperCase()));
                }
            }
        });

        // También cargar saldos actuales para validación
        this.cajaGeneralService.obtenerSaldo(this.idSucursal).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.saldos.set(res.data.saldosPorMetodo);
                }
            }
        });
    }

    private initForm(): void {
        this.form = this.fb.group({
            tipo: ['INGRESO', Validators.required],
            monto: [null, [Validators.required, Validators.min(0.01)]],
            descripcion: ['', [Validators.required, Validators.minLength(5)]],
            metodoPago: ['EFECTIVO', Validators.required],
            metodoPagoDestino: [''],
            referencia: ['']
        });

        this.form.get('metodoPago')?.valueChanges.subscribe(v => {
            if (v) {
                this.metodoPagoSeleccionado.set(v);
                this.metodoOrigenSeleccionado.set(v);
            }
        });

        this.form.get('metodoPagoDestino')?.valueChanges.subscribe(v => {
            this.metodoDestinoSeleccionado.set(v || '');
        });

        // Lógica para alternar validaciones de transferencia
        this.form.get('tipo')?.valueChanges.subscribe(tipo => {
            if (tipo === 'TRANSFERENCIA_ENTRE_METODOS') {
                this.form.get('metodoPagoDestino')?.setValidators(Validators.required);
                this.form.patchValue({ descripcion: 'TRANSFERENCIA INTERNA' });
            } else {
                this.form.get('metodoPagoDestino')?.clearValidators();
            }
            this.form.get('metodoPagoDestino')?.updateValueAndValidity();
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const data = this.form.value;

        // Validación de Saldo Suficiente para Egresos y Transferencias
        if (data.tipo === 'EGRESO' || data.tipo === 'TRANSFERENCIA_ENTRE_METODOS') {
            const metodoOrigen = data.metodoPago;
            const saldoItem = this.saldos().find(s => s.metodoPago.toUpperCase() === metodoOrigen.toUpperCase());
            const disponible = saldoItem ? saldoItem.saldo : 0;

            if (data.monto > disponible) {
                this.alertService.toast(`Saldo insuficiente en ${metodoOrigen}. Disponible: S/ ${disponible.toFixed(2)}`, 'warning');
                return;
            }
        }

        this.loading.set(true);
        const payload = {
            idSucursal: this.idSucursal,
            tipo: data.tipo,
            monto: data.monto,
            descripcion: data.descripcion,
            metodoPago: data.tipo === 'TRANSFERENCIA_ENTRE_METODOS' ? null : data.metodoPago,
            metodoPagoOrigen: data.tipo === 'TRANSFERENCIA_ENTRE_METODOS' ? data.metodoPago : null,
            metodoPagoDestino: data.tipo === 'TRANSFERENCIA_ENTRE_METODOS' ? data.metodoPagoDestino : null,
            referencia: data.referencia || 'MANUAL',
            usuario: this.authService.getUsuarioIdFromToken() || 'SYSTEM'
        };

        const observable = this.movimientoSeleccionado
            ? this.cajaGeneralService.actualizarMovimiento(this.movimientoSeleccionado.id, payload)
            : this.cajaGeneralService.registrarMovimiento(payload);

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.toast(this.movimientoSeleccionado ? 'Movimiento actualizado' : 'Movimiento registrado', 'success');
                    this.guardado.emit();
                    this.form.reset({
                        tipo: 'INGRESO',
                        monto: null,
                        descripcion: '',
                        metodoPago: 'EFECTIVO',
                        referencia: ''
                    });
                } else {
                    this.alertService.toast(res.message || 'Error en la operación', 'error');
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error de conexión', 'error');
            }
        });
    }

    get isEdit(): boolean {
        return !!this.movimientoSeleccionado;
    }

    onCancel(): void {
        this.cerrar.emit();
    }
}
