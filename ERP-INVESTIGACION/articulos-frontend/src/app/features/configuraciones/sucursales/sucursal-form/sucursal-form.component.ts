import { Component, EventEmitter, Input, Output, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button'; import { SucursalService } from '../../../../core/services/sucursal.service';
import { AlertService } from '../../../../core/services/alert.service';
import { Sucursal } from '../../models/sucursal.model';

@Component({
    selector: 'app-sucursal-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormInputComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './sucursal-form.component.html'
})
export class SucursalFormComponent implements OnInit {
    @Input() sucursal: Sucursal | null = null;
    @Output() onSave = new EventEmitter<void>();
    @Output() onCancel = new EventEmitter<void>();

    form: FormGroup;
    loading = signal(false);

    constructor(
        private fb: FormBuilder,
        private sucursalService: SucursalService,
        private alertService: AlertService
    ) {
        this.form = this.fb.group({
            nombreSucursal: ['', [Validators.required, Validators.maxLength(100)]],
            direccion: ['', [Validators.maxLength(255)]],
            telefono: ['', [Validators.maxLength(9), Validators.pattern('^[0-9]*$')]],
            celular: ['', [Validators.maxLength(9), Validators.pattern('^[0-9]*$')]],
            estado: ['ACTIVO', [Validators.required]]
        });
    }

    ngOnInit(): void { }

    ngOnChanges(changes: any): void {
        if (changes.sucursal) {
            if (this.sucursal) {
                this.form.patchValue({
                    ...this.sucursal,
                    estado: this.getNormalizedEstado(this.sucursal.estado)
                });
            } else {
                this.form.reset({ estado: 'ACTIVO' });
            }
        }
    }

    cerrar() {
        this.onCancel.emit();
        this.form.reset({ estado: 'ACTIVO' });
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const data = this.form.value;

        const request = this.sucursal ?
            this.sucursalService.actualizar(this.sucursal.idSucursal, data) :
            this.sucursalService.crear(data);

        request.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.sucursal ? 'Sucursal actualizada' : 'Sucursal creada');
                    this.onSave.emit();
                    this.cerrar();
                } else {
                    this.alertService.error(res.message || 'Error en la operación');
                }
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Error sucursal:', err);
                const errorMessage = err.error?.message || 'Error de conexión';
                this.alertService.error(errorMessage);
                this.loading.set(false);
            }
        });
    }

    getError(controlName: string): string {
        const control = this.form.get(controlName);
        if (control?.touched && control.errors) {
            if (control.errors['required']) return 'Este campo es obligatorio';
            if (control.errors['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
            if (control.errors['minlength']) return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
            if (control.errors['pattern']) return 'Solo se permiten números';
        }
        return '';
    }

    private getNormalizedEstado(estado: any): string {
        if (!estado) return 'ACTIVO';
        if (typeof estado === 'string') return (estado === 'A' || estado === '1') ? 'ACTIVO' : (estado === 'I' || estado === '0' ? 'INACTIVO' : estado);
        if (typeof estado === 'object') return estado.name || 'ACTIVO';
        return 'ACTIVO';
    }
}
