import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PuntoDocumento, PuntoDocumentoService } from '../services/punto-documento.service';
import { PuntoService } from '../../configuraciones/services/punto.service';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { AuthService } from '../../auth/services/auth.service';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { AlertService } from '../../../core/services/alert.service';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';

@Component({
    selector: 'app-punto-documento-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, PrimaryButtonComponent, SearchableSelectComponent, FormInputComponent],
    templateUrl: './punto-documento-form.component.html'
})
export class PuntoDocumentoFormComponent implements OnInit, OnChanges {
    @Input() documento: PuntoDocumento | null = null;
    @Input() puntoId: number | null | undefined = null;
    @Output() onSave = new EventEmitter<any>();
    @Output() onCancel = new EventEmitter<void>();

    form: FormGroup;
    loading = signal(false);
    puntos = signal<any[]>([]);
    plantillas = signal<any[]>([]);
    tiposDocumentoBase = signal<any[]>([]);

    modulos = [
        { label: 'Venta', value: 'VENTA' },
        { label: 'Compra', value: 'COMPRA' },
        { label: 'Cotización', value: 'COTIZACION' },
        { label: 'Nota de Venta', value: 'NOTA_VENTA' },
        { label: 'Ingresos Diversos', value: 'INGRESOS_DIVERSOS' },
        { label: 'Salidas Diversas', value: 'SALIDAS_DIVERSAS' }
    ];

    constructor(
        private fb: FormBuilder,
        private puntoDocumentoService: PuntoDocumentoService,
        private puntoService: PuntoService,
        private authService: AuthService,
        private alertService: AlertService
    ) {
        this.form = this.fb.group({
            id: [null],
            puntoId: [this.authService.getPuntoIdFromToken(), Validators.required],
            modulo: [null, Validators.required],
            tipoDoc: [{ value: null, disabled: true }, Validators.required],
            idPlantilla: [{ value: null, disabled: true }, Validators.required],
            serie: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]],
            numero: [null, [Validators.required, Validators.min(0), Validators.maxLength(15)]],
            ip: ['', [Validators.maxLength(20)]],
            estado: ['ACTIVO'],
            idDocimp: [null],
            x: [''],
            idPersonalUser: [null],
            selecc: [''],
            nota: [''],
            lpt: [''],
            detNc: [''],
            refact: [''],
            refactDia: [0]
        });
    }

    ngOnInit(): void {
        this.cargarPuntos();
        this.cargarTiposDocumento();

        // Si es edición, el flujo se habilita en ngOnChanges

        // Flujo Módulo -> Documento
        this.form.get('modulo')?.valueChanges.subscribe(val => {
            if (val) {
                this.form.get('tipoDoc')?.enable();
                this.form.get('tipoDoc')?.setValue(null, { emitEvent: false });
                this.form.get('idPlantilla')?.disable();
                this.form.get('idPlantilla')?.setValue(null, { emitEvent: false });
                this.cargarDocumentosAsignados();
            } else {
                this.form.get('tipoDoc')?.disable();
                this.form.get('idPlantilla')?.disable();
            }
        });

        // Flujo Documento -> Plantilla
        this.form.get('tipoDoc')?.valueChanges.subscribe(val => {
            if (val) {
                this.form.get('idPlantilla')?.enable();
                this.form.get('idPlantilla')?.setValue(null, { emitEvent: false });
                this.cargarPlantillas();
            } else {
                this.form.get('idPlantilla')?.disable();
            }
        });

        // Al cambiar el punto, refrescar asignados si hay módulo
        this.form.get('puntoId')?.valueChanges.subscribe(() => {
            if (this.form.get('modulo')?.value) {
                this.cargarDocumentosAsignados();
            }
        });
    }

    private documentosAsignados = signal<string[]>([]);
    private cargarDocumentosAsignados(): void {
        const puntoId = this.form.get('puntoId')?.value;
        const modulo = this.form.get('modulo')?.value;
        if (!puntoId || !modulo) return;

        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Solo guardamos los tipos de documento que ya están en este módulo
                    // y que no sean el documento actual (si estamos editando)
                    const actualId = this.form.get('id')?.value;
                    const asignados = res.data
                        .filter(pd => pd.modulo === modulo && pd.id !== actualId)
                        .map(pd => pd.tipoDoc);
                    this.documentosAsignados.set(asignados);
                }
            }
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['documento'] && this.documento) {
            // Usar emitEvent: false para evitar que las suscripciones a valueChanges 
            // reseteen los campos dependientes (Documento y Plantilla) al cargar el formulario
            this.form.patchValue({
                ...this.documento,
                estado: this.isActivo(this.documento.estado) ? 'ACTIVO' : 'INACTIVO'
            }, { emitEvent: false });
            
            this.form.get('tipoDoc')?.enable();
            this.form.get('idPlantilla')?.enable();
            
            this.cargarDocumentosAsignados();
            this.cargarPlantillas();
        } else if (changes['documento'] && !this.documento) {
            this.form.reset({
                puntoId: this.puntoId || this.authService.getPuntoIdFromToken(),
                modulo: null,
                numero: null,
                estado: 'ACTIVO'
            });
            this.form.get('tipoDoc')?.disable();
            this.form.get('idPlantilla')?.disable();
            this.documentosAsignados.set([]);
        }
    }

    cargarTiposDocumento(): void {
        this.puntoDocumentoService.obtenerTiposDocumentos().subscribe({
            next: (res) => {
                if (res.success) {
                    this.tiposDocumentoBase.set(res.data);
                }
            }
        });
    }

    onModuloChange(modulo: string): void {
        this.cargarPlantillas();
    }

    cargarPlantillas(): void {
        const modulo = this.form.get('modulo')?.value;
        const tipo = this.form.get('tipoDoc')?.value;

        if (!modulo) return;

        this.puntoDocumentoService.obtenerPlantillasPorModulo(modulo, tipo).subscribe({
            next: (res) => {
                if (res.success) {
                    this.plantillas.set(res.data);
                    // Si estamos editando, forzamos el valor de la plantilla de nuevo 
                    // para que el componente select lo reconozca ahora que los datos están cargados
                    if (this.documento && this.documento.idPlantilla) {
                        this.form.get('idPlantilla')?.setValue(this.documento.idPlantilla, { emitEvent: false });
                    }
                }
            }
        });
    }

    get tiposDocFiltrados() {
        const asignados = this.documentosAsignados();
        return this.tiposDocumentoBase()
            .filter(t => !asignados.includes(t.tipoDoc))
            .map(t => ({ label: t.nombre, value: t.tipoDoc }));
    }

    cargarPuntos(): void {
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (!sucursalId) return;

        this.puntoService.listarTodosSinPaginacion(sucursalId).subscribe({
            next: (res: any) => {
                if (res.success) {
                    this.puntos.set(res.data);
                }
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

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.alertService.toast('Por favor complete todos los campos obligatorios', 'warning');
            return;
        }

        const puntoId = this.form.get('puntoId')?.value;
        const modulo = this.form.get('modulo')?.value;
        const tipoDoc = this.form.get('tipoDoc')?.value;
        const idActual = this.form.get('id')?.value;

        this.loading.set(true);

        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const existente = res.data.find((pd: any) =>
                        pd.modulo === modulo &&
                        pd.tipoDoc === tipoDoc &&
                        this.isActivo(pd.estado) &&
                        pd.id !== idActual
                    );

                    if (existente) {
                        this.loading.set(false);
                        const tipoNombre = this.tiposDocumentoBase().find(t => t.tipoDoc === tipoDoc)?.nombre || tipoDoc;
                        this.alertService.toast(`Ya existe un documento de tipo ${tipoNombre} asignado para ${modulo} en este punto.`, 'warning');
                        return;
                    }
                }
                this.guardarDocumento();
            },
            error: () => this.guardarDocumento()
        });
    }

    private guardarDocumento(): void {
        this.puntoDocumentoService.guardar(this.form.value).subscribe({
            next: (res: any) => {
                this.loading.set(false);
                if (res.success) {
                    this.alertService.toast('Configuración guardada correctamente', 'success');
                    this.form.reset({
                        puntoId: this.authService.getPuntoIdFromToken(),
                        modulo: null,
                        numero: null,
                        estado: 'ACTIVO'
                    });
                    this.form.get('tipoDoc')?.disable();
                    this.form.get('idPlantilla')?.disable();
                    this.onSave.emit(res.data);
                } else {
                    this.alertService.toast(res.message || 'Error al guardar', 'error');
                }
            },
            error: (err) => {
                this.loading.set(false);
                const errorMsg = err.error?.message || 'Error de conexión con el servidor';
                this.alertService.toast(errorMsg, 'error');
            }
        });
    }

    private isActivo(estado: any): boolean {
        if (!estado) return true;
        if (typeof estado === 'string') return estado === 'A' || estado === 'ACTIVO' || estado === '1';
        if (typeof estado === 'object') return estado.name === 'ACTIVO' || estado.valor === 1;
        return !!estado;
    }
}
