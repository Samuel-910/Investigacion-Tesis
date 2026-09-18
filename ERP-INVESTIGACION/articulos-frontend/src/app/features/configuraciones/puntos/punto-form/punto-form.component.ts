import { Component, Input, Output, EventEmitter, OnInit, signal, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PuntoService } from '../../services/punto.service';
import { Punto } from '../../models/punto.model';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { TipoService, ProcesoService } from '../../../almacen/service/atributo.service';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { AlmacenService } from '../../../almacen/service/almacen.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSwitchComponent } from '../../../../shared/components/forms/form-switch/form-switch.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-punto-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        SearchableSelectComponent,
        FormInputComponent,
        FormSwitchComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './punto-form.component.html'
})
export class PuntoFormComponent implements OnInit, OnChanges {
    @Input() punto: Punto | null = null;
    @Output() onSave = new EventEmitter<void>();
    @Output() onCancel = new EventEmitter<void>();

    form: FormGroup;
    loading = signal(false);
    tipos = signal<any[]>([]);
    procesos = signal<any[]>([]);
    almacenes = signal<any[]>([]);
    currentSucursalId: number = 0;

    constructor(
        private fb: FormBuilder,
        private puntoService: PuntoService,
        private tipoService: TipoService,
        private procesoService: ProcesoService,
        private almacenService: AlmacenService,
        private authService: AuthService
    ) {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.maxLength(100)]],
            idSucursal: [null, Validators.required],
            idTipo: [null, Validators.required],
            idProceso: [null, Validators.required],
            tipo: ['CAJA'],
            tippro: ['ADMISION'],
            valido: [true, Validators.required],
            ipAccesoModulo: ['', [Validators.maxLength(20)]],
            idAlmacen: [null]
        });
    }

    ngOnInit() {
        this.currentSucursalId = this.authService.getSucursalIdFromToken() || 0;
        this.form.patchValue({ idSucursal: this.currentSucursalId });

        this.cargarTipos();
        this.cargarProcesos();
        this.cargarAlmacenes();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['punto'] && this.punto) {
            this.form.patchValue({
                ...this.punto,
                valido: this.punto.valido === 'S'
            }, { emitEvent: false });
        } else if (changes['punto'] && !this.punto) {
            this.form.reset({
                valido: true,
                tipo: 'CAJA',
                tippro: 'ADMISION',
                idSucursal: this.currentSucursalId
            });
        }
    }

    cargarTipos() {
        this.tipoService.listarActivos().subscribe({
            next: (response) => {
                const data = response.data?.content || [];
                this.tipos.set(data);
            },
            error: (err) => {
                console.error('Error cargando tipos', err);
                this.tipos.set([]);
            }
        });
    }

    cargarProcesos() {
        this.procesoService.listarActivos().subscribe({
            next: (response) => {
                const data = response.data?.content || [];
                this.procesos.set(data);
            },
            error: (err) => {
                console.error('Error cargando procesos', err);
                this.procesos.set([]);
            }
        });
    }

    cargarAlmacenes() {
        if (!this.currentSucursalId) return;
        this.almacenService.getBySucursal(this.currentSucursalId).subscribe({
            next: (res: any) => {
                const data = res.data;
                const almacenes = Array.isArray(data) ? data : (data?.content ? data.content : (data || []));
                this.almacenes.set(almacenes);
            },
            error: (err) => {
                console.error('Error cargando almacenes', err);
                this.almacenes.set([]);
            }
        });
    }

    getError(controlName: string): string | null {
        const control = this.form.get(controlName);
        if (control?.invalid && control?.touched) {
            if (control.errors?.['required']) return 'Este campo es obligatorio';
            if (control.errors?.['maxlength']) return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
            return 'Campo inválido';
        }
        return null;
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const data = this.form.getRawValue();

        // Formatear estado para el backend (S = Activo, N = Inactivo)
        data.valido = data.valido ? 'S' : 'N';

        const request$ = this.punto
            ? this.puntoService.actualizar(this.punto.punto, data)
            : this.puntoService.crear(data);

        request$.subscribe({
            next: (response) => {
                this.loading.set(false);
                if (response.success) {
                    this.form.reset();
                    this.onSave.emit();
                } else {
                    Swal.fire('Error', response.message || 'Error al guardar', 'error');
                }
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                const errorMessage = err.error?.message || 'No se pudo guardar el punto';
                Swal.fire('Error', errorMessage, 'error');
            }
        });
    }
}