import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormInputComponent } from '../../../../../shared/components/forms/form-input/form-input.component';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';
import { AlertService } from '../../../../../core/services/alert.service';
import { UnidadMedidaService } from '../../../../almacen/service/unidad-medida.service';

@Component({
    selector: 'app-unidad-medida-quick-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormInputComponent, PrimaryButtonComponent],
    template: `
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="p-1 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <app-form-input 
                    label="Nombre" 
                    formControlName="nombre" 
                    placeholder="Ej: TABLETA" 
                    [required]="true"
                    [error]="isFieldInvalid('nombre') ? 'Requerido' : null">
                </app-form-input>
                <app-form-input 
                    label="Símbolo" 
                    formControlName="simbolo" 
                    placeholder="Ej: TAB" 
                    [required]="true"
                    [error]="isFieldInvalid('simbolo') ? 'Requerido' : null">
                </app-form-input>
            </div>
            
            <app-form-input 
                label="Código SUNAT (Opcional)" 
                formControlName="codigoSunat" 
                placeholder="Ej: NIU">
            </app-form-input>

            <div class="flex justify-end gap-3 pt-2">
                <button type="button" (click)="cerrar.emit()" 
                    class="px-4 py-2 text-sm font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors">
                    Cancelar
                </button>
                <app-primary-button 
                    [label]="loading() ? 'Guardando...' : 'Guardar Unidad'" 
                    [loading]="loading()"
                    type="submit">
                </app-primary-button>
            </div>
        </form>
    `
})
export class UnidadMedidaQuickFormComponent {
    @Input() esAgrupador: boolean = false;
    @Output() guardado = new EventEmitter<number>();
    @Output() cerrar = new EventEmitter<void>();

    form: FormGroup;
    loading = signal(false);

    constructor(
        private fb: FormBuilder,
        private unidadService: UnidadMedidaService,
        private alertService: AlertService
    ) {
        this.form = this.fb.group({
            nombre: ['', [Validators.required]],
            simbolo: ['', [Validators.required]],
            codigoSunat: [''],
            estado: ['ACTIVO'],
            esAgrupador: [false]
        });
    }

    ngOnInit() {
        this.form.patchValue({ esAgrupador: this.esAgrupador });
    }

    isFieldInvalid(field: string): boolean {
        const control = this.form.get(field);
        return !!(control && control.invalid && (control.dirty || control.touched));
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const rawValue = this.form.value;
        const request = {
            ...rawValue,
            nombre: rawValue.nombre.toUpperCase(),
            simbolo: rawValue.simbolo.toUpperCase(),
            codigoSunat: rawValue.codigoSunat?.toUpperCase()
        };

        this.unidadService.crear(request).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    this.alertService.toast('Unidad creada correctamente', 'success');
                    this.guardado.emit(Number(res.data.id));
                    this.form.reset({
                        estado: 'ACTIVO',
                        esAgrupador: this.esAgrupador
                    });
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error', err.error?.message || 'No se pudo crear la unidad');
            }
        });
    }
}
