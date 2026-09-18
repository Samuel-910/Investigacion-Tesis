import { Component, Input, Output, EventEmitter, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormInputComponent } from '../../../components/forms/form-input/form-input.component';
import { FormSelectComponent, SelectOption } from '../../../components/forms/form-select/form-select.component';
import { FormSwitchComponent } from '../../../components/forms/form-switch/form-switch.component';
import { PrimaryButtonComponent } from '../../../components/primary-button/primary-button';
import { AtributoRequest, BaseAtributo } from '../../../../features/configuraciones/models/atributo.model';

@Component({
    selector: 'app-atributo-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormInputComponent,
        PrimaryButtonComponent,
        FormSwitchComponent
    ],
    templateUrl: './atributo-form.component.html'
})
export class AtributoFormComponent {
    visible = signal(false);
    data = signal<BaseAtributo | null>(null);
    titulo = signal<string>('Item'); // e.g. "Categoría"
    loading = signal(false);

    estadoOptions: SelectOption[] = [
        { label: 'ACTIVO', value: 'A' },
        { label: 'INACTIVO', value: 'I' }
    ];

    @Input("visible") set _visible(val: boolean) {
        this.visible.set(val);
    }

    @Input("data") set _data(val: BaseAtributo | null) {
        this.data.set(val);
    }

    @Input("titulo") set _titulo(val: string) {
        this.titulo.set(val);
    }

    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<AtributoRequest>();

    form: FormGroup;

    constructor(private fb: FormBuilder) {
        this.form = this.fb.group({
            descripcion: ['', [Validators.required]],
            estado: ['ACTIVO', [Validators.required]]
        });

        effect(() => {
            if (this.visible()) {
                const item = this.data();
                if (item) {
                    this.form.patchValue({
                        descripcion: item.descripcion,
                        estado: this.isActivo(item.estado)
                    });
                } else {
                    this.form.reset({
                        descripcion: '',
                        estado: true
                    });
                }
            }
        });
    }

    isFieldInvalid(field: string): boolean {
        const control = this.form.get(field);
        return !!(control && control.invalid && (control.dirty || control.touched));
    }

    getError(field: string): string | null {
        const control = this.form.get(field);
        if (control?.invalid && (control.dirty || control.touched)) {
            if (control.errors?.['required']) return 'Este campo es obligatorio';
        }
        return null;
    }

    cerrar() {
        this.onClose.emit();
        this.form.reset();
    }

    guardar() {
        if (this.form.valid) {
            this.loading.set(true);
            const val = this.form.value;
            const request: AtributoRequest = {
                descripcion: val.descripcion.toUpperCase(),
                estado: val.estado ? 'ACTIVO' : 'INACTIVO'
            };

            this.onSave.emit(request);
            // Parent handles loading state reset
        } else {
            this.form.markAllAsTouched();
        }
    }

    setLoading(isLoading: boolean) {
        this.loading.set(isLoading);
    }

    private isActivo(estado: any): boolean {
        if (!estado) return true;
        if (typeof estado === 'string') return estado === 'A' || estado === 'ACTIVO' || estado === '1';
        if (typeof estado === 'object') return estado.name === 'ACTIVO' || estado.valor === 1;
        return !!estado;
    }
}
