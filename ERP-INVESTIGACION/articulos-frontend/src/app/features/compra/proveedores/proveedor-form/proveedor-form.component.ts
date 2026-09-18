import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Proveedor } from '../../models/proveedor.model';
import { ProveedorService } from '../../services/proveedor.service';
import Swal from 'sweetalert2';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { FormSelectComponent, SelectOption } from '../../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-proveedor-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormInputComponent, FormLabelComponent, FormSelectComponent],
    templateUrl: './proveedor-form.component.html',
})
export class ProveedorFormComponent implements OnInit, OnChanges {
    @Input() proveedor: Proveedor | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    error = signal<string | null>(null);

    tiposDocumento: SelectOption[] = [
        { label: 'R.U.C.', value: 'R.U.C.' },
        { label: 'DNI', value: 'DNI' },
        { label: 'PASAPORTE', value: 'PASAPORTE' },
        { label: 'CARNET EXTRANJERIA', value: 'CARNET EXTRANJERIA' }
    ];

    constructor(
        private fb: FormBuilder,
        private proveedorService: ProveedorService
    ) { }

    ngOnInit(): void {
        this.initForm();
        if (this.proveedor) {
            this.form.patchValue({
                ...this.proveedor,
                estado: this.isActivo(this.proveedor.estado)
            });
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['proveedor'] && this.form) {
            if (this.proveedor) {
                this.form.patchValue({
                    ...this.proveedor,
                    estado: this.isActivo(this.proveedor.estado)
                });
            } else {
                this.form.reset();
                this.initForm();
            }
        }
    }

    initForm(): void {
        this.form = this.fb.group({
            tipoDocIdent: ['R.U.C.', Validators.required],
            numDocIdent: ['', [Validators.required, Validators.maxLength(20)]],
            razonSocial: ['', [Validators.required, Validators.maxLength(255)]],
            nombreComercial: ['', [Validators.maxLength(255)]],
            direccion: [''],
            email: ['', [Validators.email]],
            telefono: [''],
            departamento: [''],
            provincia: [''],
            distrito: [''],
            estado: [true],
            plazoDias: [0]
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const request = { ...this.form.value };
        request.estado = request.estado ? 1 : 0;

        const observable = this.proveedor?.id
            ? this.proveedorService.actualizar(this.proveedor.id, request)
            : this.proveedorService.crear(request);

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.guardado.emit();
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(err.error?.message || 'Error al guardar el proveedor');
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
