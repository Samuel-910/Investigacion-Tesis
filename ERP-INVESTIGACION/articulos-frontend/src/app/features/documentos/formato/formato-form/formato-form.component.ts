import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { DocumentoFormato } from '../../models/documento.model';
import { FormatoService } from '../../services/formato.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { FormTextareaComponent } from '../../../../shared/components/forms/form-textarea/form-textarea.component';

@Component({
    selector: 'app-formato-form',
    standalone: true,
    imports: [
        CommonModule, 
        FormsModule, 
        ReactiveFormsModule, 
        PrimaryButtonComponent,
        FormInputComponent,
        FormTextareaComponent
    ],
    templateUrl: './formato-form.component.html'
})
export class FormatoFormComponent implements OnInit, OnChanges {
    @Input() formato: DocumentoFormato | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form: FormGroup;
    loading = false;

    private fb = inject(FormBuilder);
    private formatoService = inject(FormatoService);

    constructor() {
        this.form = this.fb.group({
            id: [null],
            nombre: ['', [Validators.required]],
            anchoPx: [null, [Validators.required, Validators.min(1)]],
            altoPx: [null, [Validators.min(1)]],
            descripcion: ['']
        });
    }

    ngOnInit(): void {
        if (this.formato) {
            this.form.patchValue(this.formato);
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['formato'] && this.form) {
            if (this.formato) {
                this.form.patchValue(this.formato);
            } else {
                this.form.reset({
                    id: null,
                    nombre: '',
                    anchoPx: null,
                    altoPx: null,
                    descripcion: ''
                });
            }
        }
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading = true;
        const data = this.form.value;

        this.formatoService.guardar(data).subscribe({
            next: (res) => {
                if (res.success) {
                    this.guardado.emit();
                }
                this.loading = false;
            },
            error: () => {
                this.loading = false;
            }
        });
    }

    onCancelar(): void {
        this.form.reset();
        this.cerrar.emit();
    }

    getError(controlName: string): string | null {
        const control = this.form.get(controlName);
        if (control && control.errors && control.touched) {
            if (control.errors['required']) return 'Este campo es requerido';
            if (control.errors['min']) return `Mínimo ${control.errors['min'].min}`;
        }
        return null;
    }
}
