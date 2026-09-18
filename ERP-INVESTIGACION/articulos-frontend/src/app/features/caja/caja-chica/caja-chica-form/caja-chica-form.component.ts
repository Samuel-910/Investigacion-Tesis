import { Component, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CajaChicaService } from '../../services/caja-chica.service';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { AlertService } from '../../../../core/services/alert.service';
import { MetodoPagoService } from '../../../almacen/service/atributo.service';

@Component({
    selector: 'app-caja-chica-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, PrimaryButtonComponent],
    templateUrl: './caja-chica-form.component.html'
})
export class CajaChicaFormComponent implements OnInit {
    @Input() cajaId!: number;
    @Input() tipo: 'INGRESO' | 'EGRESO' = 'INGRESO';
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    metodosPago = signal<string[]>([]);

    constructor(
        private fb: FormBuilder,
        private cajaChicaService: CajaChicaService,
        private alertService: AlertService,
        private metodoPagoService: MetodoPagoService
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.cargarMetodosPago();
    }

    private cargarMetodosPago(): void {
        this.metodoPagoService.listarActivos().subscribe({
            next: (res) => {
                if (res.success && res.data?.content) {
                    this.metodosPago.set(res.data.content.map(m => m.descripcion.toUpperCase()));
                }
            }
        });
    }

    private initForm(): void {
        this.form = this.fb.group({
            monto: [null, [Validators.required, Validators.min(0.01)]],
            descripcion: ['', [Validators.required, Validators.minLength(5)]],
            metodoPago: ['EFECTIVO', Validators.required],
            referencia: ['']
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        if (!this.cajaId) {
            this.alertService.toast('No se ha seleccionado una caja válida', 'error');
            return;
        }

        this.loading.set(true);
        const request = {
            ...this.form.value,
            cajaChicaId: this.cajaId,
            tipo: this.tipo
        };

        this.cajaChicaService.registrarMovimiento(request).subscribe({
            next: (res) => {
                if (res.success) {
                    this.form.reset({
                        monto: null,
                        descripcion: '',
                        metodoPago: 'EFECTIVO',
                        referencia: ''
                    });
                    this.guardado.emit();
                } else {
                    this.alertService.toast(res.message || 'Error al registrar', 'error');
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error de conexión', 'error');
            }
        });
    }

    onCancel(): void {
        this.cerrar.emit();
    }
}
