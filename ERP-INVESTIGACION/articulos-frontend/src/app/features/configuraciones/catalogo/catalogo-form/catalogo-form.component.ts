import { Component, EventEmitter, Input, OnInit, OnChanges, SimpleChanges, Output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormTextareaComponent } from '../../../../shared/components/forms/form-textarea/form-textarea.component';
import { FormLabelComponent } from '../../../../shared/components/forms/form-label/form-label.component';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { UnidadMedidaQuickFormComponent } from './unidad-medida-quick-form/unidad-medida-quick-form.component';
import { CatalogoRequest, CatalogoResponse } from '../../models/catalogo.model';
import { CatalogoService } from '../../services/catalogo.service';
import { AccionTerapeuticaService, CategoriaService, PrincipioActivoService } from '../../../almacen/service/atributo.service';
import { UnidadMedidaService } from '../../../almacen/service/unidad-medida.service';
import { AlertService } from '../../../../core/services/alert.service';
@Component({
    selector: 'app-catalogo-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        SearchableSelectComponent,
        FormInputComponent,
        FormTextareaComponent,
        FormLabelComponent,
        ModalComponent,
        UnidadMedidaQuickFormComponent
    ],
    templateUrl: './catalogo-form.component.html',
})
export class CatalogoFormComponent implements OnInit, OnChanges {
    @Input() catalogo: CatalogoResponse | null = null;
    @Output() guardado = new EventEmitter<void>();
    @Output() cerrar = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    error = signal<string | null>(null);

    categorias = signal<any[]>([]);
    principiosActivos = signal<any[]>([]);
    accionesTerapeuticas = signal<any[]>([]);
    unidades = signal<any[]>([]);

    // Quick Unit Modal State
    showQuickUnitModal = signal(false);
    quickUnitIsAgrupador = signal(false);
    quickUnitTargetField = signal<string>('');

    // Auxiliary signals to make form values reactive in computed filters
    private idUnidadBaseVal = signal<any>(null);
    private idUnidadIntermediaVal = signal<any>(null);
    private idUnidadMayorVal = signal<any>(null);

    unidadesBase = computed(() => {
        const unidades = this.unidades();
        const currentId = this.idUnidadBaseVal();
        return unidades.filter(u => !u.esAgrupador || u.id == currentId);
    });

    unidadesAgrupadoras = computed(() => {
        const unidades = this.unidades();
        const currentIntermedia = this.idUnidadIntermediaVal();
        const currentMayor = this.idUnidadMayorVal();
        return unidades.filter(u => u.esAgrupador || u.id == currentIntermedia || u.id == currentMayor);
    });

    constructor(
        private fb: FormBuilder,
        private catalogoService: CatalogoService,
        private categoriaService: CategoriaService,
        private principioActivoService: PrincipioActivoService,
        private accionTerapeuticaService: AccionTerapeuticaService,
        private unidadMedidaService: UnidadMedidaService,
        private alertService: AlertService
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.setupValidators();
        this.cargarListas();
        this.setupAutoDescripcion();

        // Si ya hay un catálogo al inicializar (modo edición)
        if (this.catalogo) {
            const patchValue = { ...this.catalogo };
            if (patchValue.estado) {
                patchValue.estado = this.isActivo(patchValue.estado);
            }
            this.form.patchValue(patchValue);
            // Actualizar señales de unidades para disparar los computed filters
            this.idUnidadBaseVal.set(this.catalogo.idUnidadBase);
            this.idUnidadIntermediaVal.set(this.catalogo.idUnidadIntermedia);
            this.idUnidadMayorVal.set(this.catalogo.idUnidadMayor);

            // Asegurar que los paneles se expandan si hay IDs aunque el booleano venga mal
            if (this.catalogo.idUnidadIntermedia) this.form.get('manejaBlister')?.setValue(true);
            if (this.catalogo.idUnidadMayor) this.form.get('manejaCaja')?.setValue(true);
        }

        // Suscribirse a los cambios de los controles de unidad para actualizar las señales
        this.form.get('idUnidadBase')?.valueChanges.subscribe(val => this.idUnidadBaseVal.set(val));
        this.form.get('idUnidadIntermedia')?.valueChanges.subscribe(val => this.idUnidadIntermediaVal.set(val));
        this.form.get('idUnidadMayor')?.valueChanges.subscribe(val => this.idUnidadMayorVal.set(val));
    }

    setupValidators() {
        // Validación condicional para Blister / Intermedia
        this.form.get('manejaBlister')?.valueChanges.subscribe(maneja => {
            const idControl = this.form.get('idUnidadIntermedia');
            const factorControl = this.form.get('factorBlister');
            if (maneja) {
                idControl?.setValidators([Validators.required]);
                factorControl?.setValidators([Validators.required, Validators.min(2)]);
                if ((factorControl?.value || 0) < 2) {
                    factorControl?.setValue(2, { emitEvent: false });
                }
            } else {
                idControl?.clearValidators();
                factorControl?.setValidators([Validators.min(1)]);
                factorControl?.setValue(1, { emitEvent: false });
            }
            idControl?.updateValueAndValidity();
            factorControl?.updateValueAndValidity();
        });

        // Validación condicional para Caja / Mayor
        this.form.get('manejaCaja')?.valueChanges.subscribe(maneja => {
            const idControl = this.form.get('idUnidadMayor');
            const factorControl = this.form.get('factorCaja');
            if (maneja) {
                idControl?.setValidators([Validators.required]);
                factorControl?.setValidators([Validators.required, Validators.min(2)]);
                if ((factorControl?.value || 0) < 2) {
                    factorControl?.setValue(2, { emitEvent: false });
                }
            } else {
                idControl?.clearValidators();
                factorControl?.setValidators([Validators.min(1)]);
                factorControl?.setValue(1, { emitEvent: false });
            }
            idControl?.updateValueAndValidity();
            factorControl?.updateValueAndValidity();
        });

        const updatePresentacionValidity = () => {
            const manejaUnidad = this.form.get('manejaUnidad')?.value;
            const manejaBlister = this.form.get('manejaBlister')?.value;
            const manejaCaja = this.form.get('manejaCaja')?.value;
            const presControl = this.form.get('presentacion');

            if (manejaUnidad || manejaBlister || manejaCaja) {
                presControl?.setValidators([Validators.required]);
            } else {
                presControl?.clearValidators();
            }
            presControl?.updateValueAndValidity({ emitEvent: false });
        };

        this.form.get('manejaUnidad')?.valueChanges.subscribe(updatePresentacionValidity);
        this.form.get('manejaBlister')?.valueChanges.subscribe(updatePresentacionValidity);
        this.form.get('manejaCaja')?.valueChanges.subscribe(updatePresentacionValidity);
        updatePresentacionValidity();
    }

    setupAutoDescripcion() {
        const fieldsToWatch = [
            'manejaCaja', 'factorCaja', 'idUnidadMayor',
            'manejaBlister', 'factorBlister', 'idUnidadIntermedia',
            'manejaUnidad', 'idUnidadBase'
        ];

        fieldsToWatch.forEach(field => {
            this.form.get(field)?.valueChanges.subscribe(() => {
                // Usar setTimeout para asegurar que el valor del formulario se haya actualizado completamente
                setTimeout(() => this.generarDescripcion(), 0);
            });
        });
    }

    abrirQuickUnit(field: string, esAgrupador: boolean) {
        this.quickUnitTargetField.set(field);
        this.quickUnitIsAgrupador.set(esAgrupador);
        this.showQuickUnitModal.set(true);
    }

    onQuickUnitSaved(id: number) {
        this.showQuickUnitModal.set(false);
        // Recargar la lista de unidades del servicio
        this.unidadMedidaService.listarActivas().subscribe(res => {
            if (res.success && res.data) {
                this.unidades.set(res.data.content || []);
                // Pre-seleccionar la nueva unidad en el campo correspondiente
                const field = this.quickUnitTargetField();
                if (field) {
                    this.form.get(field)?.setValue(id);
                }
            }
        });
    }

    generarDescripcion() {
        const form = this.form.value;
        const unidades = this.unidades();
        let descripcion = '';

        const getNombreUnidad = (id: any) => {
            const u = unidades.find(u => u.id == id);
            return u ? u.nombre : '';
        };

        const pluralizar = (nombre: string) => {
            if (!nombre) return '';
            const vocales = ['A', 'E', 'I', 'O', 'U'];
            const ultimaLetra = nombre.trim().toUpperCase().slice(-1);
            return vocales.includes(ultimaLetra) ? `${nombre}S` : `${nombre}ES`;
        };

        if (form.manejaCaja && form.idUnidadMayor) {
            const nombreCaja = getNombreUnidad(form.idUnidadMayor);
            const factorCaja = form.factorCaja || 1;

            let contenido = '';
            if (form.manejaBlister && form.idUnidadIntermedia) {
                const nombreBlister = getNombreUnidad(form.idUnidadIntermedia);
                // Si hay blister, agregamos el detalle del blister
                let detalleBlister = pluralizar(nombreBlister);

                if (form.manejaUnidad && form.idUnidadBase) {
                    const nombreBase = getNombreUnidad(form.idUnidadBase);
                    const factorBlister = form.factorBlister || 1;
                    detalleBlister += ` DE ${factorBlister} ${pluralizar(nombreBase)} C/U`;
                }
                contenido = detalleBlister;

            } else if (form.manejaUnidad && form.idUnidadBase) {
                const nombreBase = getNombreUnidad(form.idUnidadBase);
                contenido = pluralizar(nombreBase);
            }

            descripcion = `${nombreCaja} X ${factorCaja} ${contenido}`;
        }
        else if (form.manejaBlister && form.idUnidadIntermedia) {
            const nombreBlister = getNombreUnidad(form.idUnidadIntermedia);
            const factor = form.factorBlister || 1;
            let contenido = '';

            if (form.manejaUnidad && form.idUnidadBase) {
                const nombreBase = getNombreUnidad(form.idUnidadBase);
                contenido = pluralizar(nombreBase);
            }

            descripcion = `${nombreBlister} X ${factor} ${contenido}`;
        }
        else if (form.manejaUnidad && form.idUnidadBase) {
            descripcion = getNombreUnidad(form.idUnidadBase);
        }

        if (descripcion) {
            this.form.patchValue({ presentacion: descripcion.trim().toUpperCase() }, { emitEvent: false });
        }
    }

    cargarListas() {
        this.categoriaService.listar(0, 1000, '').subscribe((res: any) => {
            if (res.success && res.data) this.categorias.set(res.data.content);
        });
        this.unidadMedidaService.listarActivas().subscribe((res: any) => {
            if (res.success && res.data) {
                this.unidades.set(res.data.content || []);
            }
        });
        this.principioActivoService.listar(0, 1000, '').subscribe((res: any) => {
            if (res.success && res.data) this.principiosActivos.set(res.data.content);
        });
        this.accionTerapeuticaService.listar(0, 1000, '').subscribe((res: any) => {
            if (res.success && res.data) this.accionesTerapeuticas.set(res.data.content);
        });
    }
    // Define la lista de presentaciones basadas en los códigos de distribuidoras
    presentaciones = [
        { nombre: 'Frasco', codigo: 'FCO', descripcion: 'Frasco (Gotas, Jarabes, Soluciones)' },
        { nombre: 'Tubo', codigo: 'TBO', descripcion: 'Tubo (Cremas, Ungüentos, Geles)' },
        { nombre: 'Caja', codigo: 'CJA', descripcion: 'Caja (Contiene múltiples unidades o blisters)' },
        { nombre: 'Ampolla', codigo: 'AMP', descripcion: 'Inyectables' },
        { nombre: 'Tableta', codigo: 'TAB', descripcion: 'Tabletas o comprimidos' },
        { nombre: 'Sachet', codigo: 'SCH', descripcion: 'Sobres o geles individuales' }
    ];
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['catalogo'] && this.form) {
            if (this.catalogo) {
                const patchValue = { ...this.catalogo };
                if (patchValue.estado) {
                    patchValue.estado = this.isActivo(patchValue.estado);
                }
                this.form.patchValue(patchValue);
                this.idUnidadBaseVal.set(this.catalogo.idUnidadBase);
                this.idUnidadIntermediaVal.set(this.catalogo.idUnidadIntermedia);
                this.idUnidadMayorVal.set(this.catalogo.idUnidadMayor);

                if (this.catalogo.idUnidadIntermedia) this.form.get('manejaBlister')?.setValue(true);
                if (this.catalogo.idUnidadMayor) this.form.get('manejaCaja')?.setValue(true);
            } else {
                this.form.reset();
                this.initForm();
            }
        }
    }

    initForm(): void {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.maxLength(255)]],
            codigo: ['', Validators.required],
            detalle: [''],
            tipo: ['PRODUCTO'],
            tipoMedicamento: [''],

            idCategoria: [null, Validators.required],
            idPrincipioActivo: [null],
            idAccionTerapeutica: [null],

            regSanitario: [''],

            esGenerico: [null],
            esControlado: [false],
            ventaConReceta: [false],
            manejaLotes: [true],

            presentacion: [''],
            tiempoEntregaMinutos: [0],
            estado: [true],
            idNivel: [1, Validators.required],

            // Unidades
            idUnidadBase: [null, Validators.required],
            manejaUnidad: [true],

            manejaBlister: [false],
            idUnidadIntermedia: [null],
            factorBlister: [1],

            manejaCaja: [false],
            idUnidadMayor: [null],

            factorCaja: [1],
            precioKairos: [0],

            tipoAfectacion: ['GRAVADO', Validators.required]
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        const formValue = this.form.value;

        const request: CatalogoRequest = {
            ...formValue,
            idNivel: Number(formValue.idNivel), // Asegurar tipos
            estado: formValue.estado ? 'ACTIVO' : 'INACTIVO'
        };

        const observable = this.catalogo?.id
            ? this.catalogoService.actualizar(this.catalogo.id, request)
            : this.catalogoService.crear(request);

        observable.subscribe({
            next: (res) => {
                if (res.success) {
                    this.guardado.emit();
                    this.resetFormToDefaults();
                }
                this.loading.set(false);
            },
            error: (err) => {
                const errorMsg = err.error?.message || 'Error al guardar el artículo';
                this.error.set(errorMsg);
                this.loading.set(false);
                this.alertService.error('Operación no permitida', errorMsg);
            }
        });
    }

    private resetFormToDefaults() {
        this.form.reset({
            tipo: 'PRODUCTO',
            factorBlister: 1,
            factorCaja: 1,
            idNivel: 1,
            estado: true,
            manejaUnidad: true,
            manejaLotes: true,
            tipoAfectacion: 'GRAVADO'
        });

        // Reset manual de señales auxiliares
        this.idUnidadBaseVal.set(null);
        this.idUnidadIntermediaVal.set(null);
        this.idUnidadMayorVal.set(null);

        // Limpiar errores previos
        this.error.set(null);
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
        if (typeof estado === 'object') return estado.name === 'ACTIVO' || estado.valor === 1 || estado.valor === 9 || estado.name === 'VIGENTE';
        return !!estado;
    }

}
