import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Clasificacion, ClasificacionRequest } from '../../../procesos/models/clasificacion.model';
import { ClasificacionService } from '../../../procesos/service/clasificacion.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { FormSelectComponent, SelectOption } from '../../../../shared/components/forms/form-select/form-select.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';

@Component({
    selector: 'app-clasificacion-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormInputComponent,
        FormSelectComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './clasificacion-form.component.html',
})
export class ClasificacionFormComponent implements OnInit, OnChanges {
    @Input() clasificacion: Clasificacion | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    error = signal<string | null>(null);

    tiposMovimiento: SelectOption[] = [
        { label: 'Ingreso', value: 'INGRESO' },
        { label: 'Salida', value: 'SALIDA' },
        { label: 'Ambos', value: 'AMBOS' }
    ];

    estados: SelectOption[] = [
        { label: 'Activo', value: 'ACTIVO' },
        { label: 'Inactivo', value: 'INACTIVO' }
    ];

    constructor(
        private fb: FormBuilder,
        private clasificacionService: ClasificacionService
    ) { }

    ngOnInit(): void {
        this.initForm();
        if (this.clasificacion) {
            this.form.patchValue({
                ...this.clasificacion,
                estado: this.isActivo(this.clasificacion.estado) ? 'ACTIVO' : 'INACTIVO'
            });
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['clasificacion'] && this.form) {
            if (this.clasificacion) {
                this.form.patchValue({
                    ...this.clasificacion,
                    estado: this.isActivo(this.clasificacion.estado) ? 'ACTIVO' : 'INACTIVO'
                });
            } else {
                this.form.reset();
                this.initForm();
            }
        }
    }

    initForm(): void {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.maxLength(100)]],
            tipo: ['AMBOS', [Validators.required]],
            estado: ['ACTIVO', [Validators.required]]
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const request: ClasificacionRequest = this.form.value;

        const observable = this.clasificacion?.id
            ? this.clasificacionService.actualizar(this.clasificacion.id, request)
            : this.clasificacionService.crear(request);

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.guardado.emit();
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(err.error?.message || 'Error al guardar la clasificación');
                this.loading.set(false);
            }
        });
    }

    onClose(): void {
        this.cerrar.emit();
    }

    isFieldInvalid(fieldName: string): boolean {
        const field = this.form.get(fieldName);
        return !!(field && field.invalid && (field.dirty || field.touched));
    }

    private isActivo(estado: any): boolean {
        if (!estado) return true;
        if (typeof estado === 'string') return estado === 'A' || estado === 'ACTIVO' || estado === '1';
        if (typeof estado === 'object') return estado.name === 'ACTIVO' || estado.valor === 1;
        return !!estado;
    }
}
