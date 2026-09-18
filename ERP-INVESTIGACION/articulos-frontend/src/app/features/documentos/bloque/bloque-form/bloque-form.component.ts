import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal, inject, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Bloque } from '../../models/documento.model';
import { DocumentoService } from '../../services/documento.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { FormTextareaComponent } from '../../../../shared/components/forms/form-textarea/form-textarea.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-bloque-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormInputComponent,
        FormTextareaComponent,
        FormSelectComponent
    ],
    templateUrl: './bloque-form.component.html',
})
export class BloqueFormComponent implements OnInit, OnChanges, OnDestroy {
    @Input() bloque: Bloque | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    error = signal<string | null>(null);
    previewHtml = signal<SafeHtml>('');

    private formSub?: Subscription;
    private sanitizer = inject(DomSanitizer);

    categorias = [
        { label: 'Encabezado', value: 'ENCABEZADO' },
        { label: 'Contenido / Cuerpo', value: 'CONTENIDO' },
        { label: 'Tabla', value: 'TABLA' },
        { label: 'Firma', value: 'FIRMA' },
        { label: 'Pie de Página', value: 'PIE' }
    ];

    modulosOpts = [
        { label: 'Venta', value: 'VENTA' },
        { label: 'Compra', value: 'COMPRA' },
        { label: 'Inventario', value: 'INVENTARIO' },
        { label: 'Ingresos Diversos', value: 'INGRESOS_DIVERSOS' },
        { label: 'Salidas Diversas', value: 'SALIDAS_DIVERSAS' }
    ];

    constructor(
        private fb: FormBuilder,
        private documentoService: DocumentoService
    ) { }

    ngOnInit(): void {
        this.initForm();
        if (this.bloque) {
            this.form.patchValue({
                ...this.bloque,
                modulo: this.bloque.modulos && this.bloque.modulos.length > 0 ? this.bloque.modulos[0] : null
            });
        }
        this.updatePreview();
    }

    ngOnDestroy(): void {
        this.formSub?.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['bloque'] && this.form) {
            if (this.bloque) {
                this.form.patchValue({
                    ...this.bloque,
                    modulo: this.bloque.modulos && this.bloque.modulos.length > 0 ? this.bloque.modulos[0] : null
                });
            } else {
                this.form.reset();
                this.initForm();
            }
            this.updatePreview();
        }
    }

    initForm(): void {
        // Limpiar suscripción anterior si existe
        this.formSub?.unsubscribe();

        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.maxLength(100)]],
            categoria: ['CONTENIDO', Validators.required],
            modulo: [null],
            htmlContenido: ['', [Validators.required]],
            cssEstilo: ['']
        });

        // Suscribirse al nuevo formulario
        this.formSub = this.form.valueChanges.subscribe(() => {
            this.updatePreview();
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const formValue = this.form.value;
        const request = {
            nombre: formValue.nombre,
            categoria: formValue.categoria,
            htmlContenido: formValue.htmlContenido,
            cssEstilo: formValue.cssEstilo,
            modulos: formValue.modulo ? [formValue.modulo] : []
        };

        const observable = this.bloque?.id
            ? this.documentoService.actualizarBloque(this.bloque.id, request)
            : this.documentoService.guardarBloque(request);

        observable.subscribe({
            next: () => {
                this.guardado.emit();
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(err.error?.message || 'Error al guardar el bloque');
                this.loading.set(false);
            }
        });
    }

    onClose(): void {
        this.cerrar.emit();
    }

    private updatePreview(): void {
        const { htmlContenido, cssEstilo } = this.form.value;
        const combined = `
            <style>${cssEstilo || ''}</style>
            <div class="block-preview-container">
                ${htmlContenido || ''}
            </div>
        `;
        this.previewHtml.set(this.sanitizer.bypassSecurityTrustHtml(combined));
    }

    getError(controlName: string): string | null {
        const control = this.form.get(controlName);
        if (control && control.errors && control.touched) {
            if (control.errors['required']) return 'Este campo es requerido';
            if (control.errors['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
        }
        return null;
    }
}
