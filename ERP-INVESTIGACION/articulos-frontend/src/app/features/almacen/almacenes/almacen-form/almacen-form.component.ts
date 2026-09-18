import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Almacen, AlmacenRequest } from '../../models/almacen.model';
import { AlmacenService } from '../../service/almacen.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { UserService } from '../../../../features/configuraciones/services/user.service';
import { UserResponse } from '../../../../features/configuraciones/models/user.model';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { map } from 'rxjs';

@Component({
    selector: 'app-almacen-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormInputComponent, FormLabelComponent, SearchableSelectComponent],
    templateUrl: './almacen-form.component.html',
})
export class AlmacenFormComponent implements OnInit, OnChanges {
    @Input() almacen: Almacen | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    error = signal<string | null>(null);
    usuarios = signal<{ id: number, nombreCompleto: string }[]>([]);

    constructor(
        private fb: FormBuilder,
        private almacenService: AlmacenService,
        private alertService: AlertService,
        private authService: AuthService,
        private userService: UserService
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.cargarUsuarios();
        if (this.almacen) {
            this.form.patchValue({
                ...this.almacen,
                estado: this.isActivo(this.almacen.estado)
            });
        }
    }

    cargarUsuarios(): void {
        this.userService.getActiveUsers(0, 50).pipe(
            map(res => res.data.content.map(u => ({
                id: u.id,
                nombreCompleto: `${u.firstName} ${u.lastName}`
            })))
        ).subscribe({
            next: (users) => this.usuarios.set(users),
            error: () => this.alertService.toast('Error al cargar usuarios', 'error')
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['almacen'] && this.form) {
            if (this.almacen) {
                this.form.patchValue({
                    ...this.almacen,
                    estado: this.isActivo(this.almacen.estado)
                });
            } else {
                this.form.reset({
                    estado: 'ACTIVO',
                    esPrincipal: false,
                    idSucursal: this.authService.getSucursalIdFromToken() || 0
                });
            }
        }
    }

    initForm(): void {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.maxLength(255)]],
            codigo: ['', [Validators.maxLength(50)]],
            ubicacion: ['', [Validators.maxLength(255)]],
            responsable: ['', [Validators.maxLength(255)]],
            esPrincipal: [false],
            estado: ['ACTIVO'],
            idSucursal: [this.authService.getSucursalIdFromToken() || 0]
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const request: AlmacenRequest = {
            ...this.form.value,
            estado: this.form.value.estado ? 'ACTIVO' : 'INACTIVO'
        };

        const observable = this.almacen?.id
            ? this.almacenService.update(this.almacen.id, request)
            : this.almacenService.create(request);

        observable.subscribe({
            next: (res: any) => {
                this.guardado.emit();
                this.loading.set(false);
            },
            error: (err) => {
                const msg = err.error?.message || 'Error al guardar el almacén';
                this.error.set(msg);
                this.alertService.error('Error', msg);
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
