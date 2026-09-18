import { Component, EventEmitter, Input, OnInit, Output, signal, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { ProductoService } from '../../service/producto.service';
import { AlmacenService } from '../../service/almacen.service';
import {
    CategoriaService,
    LaboratorioService,
    PrincipioActivoService,
    AccionTerapeuticaService,
    UbicacionService
} from '../../service/atributo.service';
import { UnidadMedidaService } from '../../service/unidad-medida.service';
import { UnidadMedidaResponse } from '../../../configuraciones/models/unidad-medida.model';
import { Producto } from '../../models/producto.model';
// import Swal from 'sweetalert2'; // Eliminado: Usar alertService en su lugar
import { AuthService } from '../../../auth/services/auth.service';
import { CompraService } from '../../../compra/services/compra.service';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { AlertService } from '../../../../core/services/alert.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';
import { ModalComponent } from '../../../../shared/components/modal/modal';

// New Helper Classes
import { ProveedorService } from '../../../compra/services/proveedor.service';
import { ProductoRequest } from '../../models/producto.model';
import { ProductoMapper } from '../../models/producto.mapper';
import { ProductoFormFactory } from './producto-form.factory';
import { ProductoPricingManager } from './producto-pricing.manager';
import { CatalogoService } from '../../../configuraciones/services/catalogo.service';
import { RegistroDetalleComponent } from "../../../compra/registro/registro-detalle/registro-detalle.component";

interface DetalleCompraResult {
    id: number;
    producto: { id: number; nombre: string; codigo: string; presentacion?: string };
    precioUnitario: number;
    fechaVencimiento: string;
    lote: string;
    cantidad: number;
    unidad: string;
    idCompra?: number;
    idProveedor?: number;
    proveedorRazonSocial?: string;
    serie?: string;
    correlativo?: string;
    presentacion?: string;
    factorConversion?: number;
}

@Component({
    selector: 'app-producto-servicio-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, SearchableSelectComponent, FormInputComponent, FormSelectComponent, ModalComponent, RegistroDetalleComponent],
    templateUrl: './producto-form.component.html',
    styleUrls: ['./producto-form.component.css']
})
export class ProductoServicioFormComponent implements OnInit, OnChanges {
    @Input() producto: Producto | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    pricingManager!: ProductoPricingManager;

    loading = signal(false);
    error = signal<string | null>(null);

    categorias = signal<any[]>([]);
    laboratorios = signal<any[]>([]);
    principiosActivos = signal<any[]>([]);
    accionesTerapeuticas = signal<any[]>([]);
    ubicaciones = signal<any[]>([]);
    almacenes = signal<any[]>([]);
    unidades = signal<UnidadMedidaResponse[]>([]);
    proveedores = signal<any[]>([]);
    currentSucursalId: number | null = null;
    tipoGananciaOptions = [
        { label: '%', value: 'PORCENTAJE' },
        { label: 'S/', value: 'FIJO' }
    ];

    // Signals from PricingManager (exposed via getters or direct reference if needed)
    unitProfit = signal<{ igv: number, noIgv: number } | null>(null);
    unitProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    unitProfitMax = signal<{ igv: number, noIgv: number } | null>(null);
    blisterProfit = signal<{ igv: number, noIgv: number } | null>(null);
    blisterProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    blisterProfitMax = signal<{ igv: number, noIgv: number } | null>(null);
    cajaProfit = signal<{ igv: number, noIgv: number } | null>(null);
    cajaProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    cajaProfitMax = signal<{ igv: number, noIgv: number } | null>(null);


    constructor(
        private fb: FormBuilder,
        private catalogoService: CatalogoService,
        private productoService: ProductoService,
        private categoriaService: CategoriaService,
        private laboratorioService: LaboratorioService,
        private principioActivoService: PrincipioActivoService,
        private accionTerapeuticaService: AccionTerapeuticaService,
        private ubicacionService: UbicacionService,
        private almacenService: AlmacenService,
        private unidadMedidaService: UnidadMedidaService,
        private proveedorService: ProveedorService,
        private authService: AuthService,
        private compraService: CompraService,
        private alertService: AlertService,
        private cdr: ChangeDetectorRef
    ) { }

    isDarkMode = signal(false);
    importStep = signal<1 | 2>(1);
    compraSearchResults = signal<DetalleCompraResult[]>([]);
    showCompraSearch = signal(false);
    searchCompraTerm = signal('');
    selectedImportItems = signal<DetalleCompraResult[]>([]);
    importForms = signal<FormGroup[]>([]);

    // Signals for Purchase Detail Modal
    showCompraDetalle = signal(false);
    selectedCompraIdView = signal<number | null>(null);

    // New signals for Historial
    // Signals for Historial (Modified to track active card index)
    activeHistorialIndex = signal<number | null>(null);
    historialProductos = signal<Producto[]>([]);

    ngOnInit(): void {
        this.initializeSucursal();
        this.initForm(); // Creates this.form and this.pricingManager
        this.cargarListas();

        // Link manager signals to local signals for template compatibility
        this.linkManagerSignals();

        if (this.producto) {
            this.cargarDatosProducto();
        }

        this.onSearchCompra('');
    }

    initForm(): void {
        this.form = ProductoFormFactory.create(this.fb);
        this.pricingManager = new ProductoPricingManager(this.form);
        this.pricingManager.setupAutoCalculations();

        // Setup validation logic
        ProductoFormFactory.setupPriceValidators(this.form, (f) => ProductoFormFactory.validatePrices(f));
        this.setupUnitValidationSubscriptions(this.form);
    }

    private setupUnitValidationSubscriptions(form: FormGroup): void {
        const units = ['manejaBlister', 'manejaCaja'];
        units.forEach(unit => {
            form.get(unit)?.valueChanges.subscribe(() => {
                this.updateUnitValidators(form);
            });
        });
        this.updateUnitValidators(form);
    }

    private updateUnitValidators(form: FormGroup): void {
        const controls = {
            blister: ['precioVentaBlister', 'precioBlisterMin', 'gananciaBlister', 'gananciaBlisterMin'],
            caja: ['precioVentaCaja', 'precioCajaMin', 'gananciaCaja', 'gananciaCajaMin']
        };

        const update = (unit: 'blister' | 'caja', active: boolean) => {
            controls[unit].forEach(name => {
                const ctrl = form.get(name);
                if (ctrl) {
                    if (active) {
                        ctrl.setValidators([Validators.required]);
                    } else {
                        ctrl.clearValidators();
                        ctrl.setErrors(null);
                    }
                    ctrl.updateValueAndValidity({ emitEvent: false });
                }
            });

            // Validación específica para factores
            const factorName = unit === 'blister' ? 'factorBlister' : 'factorCaja';
            const factorCtrl = form.get(factorName);
            if (active) {
                factorCtrl?.setValidators([Validators.required, Validators.min(2)]);
                if ((factorCtrl?.value || 0) < 2) {
                    factorCtrl?.setValue(2, { emitEvent: false });
                }
            } else {
                factorCtrl?.setValidators([Validators.min(1)]);
                factorCtrl?.setValue(1, { emitEvent: false });
            }
            factorCtrl?.updateValueAndValidity({ emitEvent: false });
        };

        update('blister', !!form.get('manejaBlister')?.value);
        update('caja', !!form.get('manejaCaja')?.value);
    }

    linkManagerSignals() {
        this.unitProfit = this.pricingManager.unitProfit;
        this.unitProfitMin = this.pricingManager.unitProfitMin;
        this.unitProfitMax = this.pricingManager.unitProfitMax;

        this.blisterProfit = this.pricingManager.blisterProfit;
        this.blisterProfitMin = this.pricingManager.blisterProfitMin;
        this.blisterProfitMax = this.pricingManager.blisterProfitMax;

        this.cajaProfit = this.pricingManager.cajaProfit;
        this.cajaProfitMin = this.pricingManager.cajaProfitMin;
        this.cajaProfitMax = this.pricingManager.cajaProfitMax;
    }

    private initializeSucursal() {
        this.currentSucursalId = this.authService.getSucursalIdFromToken();
    }

    eliminarCard(index: number): void {
        const form = this.importForms()[index];
        const idProd = form.get('idProducto')?.value;
        if (idProd) {
            this.productoService.eliminar(idProd).subscribe({
                next: () => {
                    this.alertService.success('Eliminado', 'Producto eliminado correctamente');
                    const currentForms = this.importForms();
                    currentForms.splice(index, 1);
                    this.importForms.set([...currentForms]);
                },
                error: (err) => {
                    this.alertService.error('Error', 'No se pudo eliminar el producto');
                    console.error('Error al eliminar producto:', err);
                }
            });
        }
    }

    toggleImportSelection(item: DetalleCompraResult): void {
        const current = this.selectedImportItems();
        const exists = current.find(i => i.id === item.id);

        if (exists) {
            this.selectedImportItems.set(current.filter(i => i.id !== item.id));
        } else {
            this.selectedImportItems.set([...current, item]);
        }
    }

    verDetalleCompra(item: DetalleCompraResult, event: Event): void {
        event.stopPropagation();
        if (item.idCompra) {
            this.selectedCompraIdView.set(item.idCompra);
            this.showCompraDetalle.set(true);
        } else {
            this.alertService.info('Sin Datos', 'Este registro no tiene asociada una compra válida.');
        }
    }

    cerrarDetalleCompra(): void {
        this.showCompraDetalle.set(false);
        this.selectedCompraIdView.set(null);
    }

    isSelected(item: DetalleCompraResult): boolean {
        return this.selectedImportItems().some(i => i.id === item.id);
    }

    continueToImportEdit(): void {
        if (this.selectedImportItems().length === 0) return;

        this.initializeImportForms();
        this.importStep.set(2);
    }

    onSearchCompra(term: string): void {
        this.searchCompraTerm.set(term);
        const idSucursal = this.authService.getSucursalIdFromToken() || 0;
        this.compraService.buscarDetallesPorProducto(term, idSucursal).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Manejar tanto ApiResponse normal como PageResponse
                    const results = res.data.content ? res.data.content : res.data;
                    this.compraSearchResults.set(results);
                    this.showCompraSearch.set(true);
                }
            },
            error: (err) => console.error(err)
        });
    }

    selectCompraDetalle(detalle: any): void {
        this.toggleImportSelection(detalle);
    }

    initializeImportForms(): void {
        const forms: FormGroup[] = [];

        this.selectedImportItems().forEach(detalle => {
            // Use Factory
            const f = ProductoFormFactory.create(this.fb);
            f.addControl('idDetalleCompra', new FormControl(detalle.id));

            const mgr = new ProductoPricingManager(f);
            mgr.setupAutoCalculations();

            ProductoFormFactory.setupPriceValidators(f, (form) => ProductoFormFactory.validatePrices(form));
            this.setupUnitValidationSubscriptions(f);

            let parsingResult = { nivel: 'MENOR', factores: [] as number[] };
            if (detalle.producto?.presentacion) {
                parsingResult = ProductoMapper.parsePresentation(detalle.producto.presentacion);
            }

            let formConfig: any = {
                manejaUnidad: true,
                manejaBlister: false,
                factorBlister: 1,
                manejaCaja: false,
                factorCaja: 1
            };

            // Set up form switches based on the generic format string
            const n1 = parsingResult.factores[0] || 1;
            const n2 = parsingResult.factores[1] || 1;

            if (parsingResult.nivel === 'MENOR') {
                formConfig.manejaBlister = false;
                formConfig.manejaCaja = false;
            } else if (parsingResult.nivel === 'INTERMEDIO') {
                formConfig.manejaBlister = true;
                formConfig.factorBlister = n1;
                formConfig.manejaCaja = false;
            } else if (parsingResult.nivel === 'MAYOR') {
                formConfig.manejaCaja = true;
                formConfig.factorCaja = n1 * n2;
                formConfig.manejaBlister = true;
                formConfig.factorBlister = n2;
            }

            // USE EXACT PURCHASE UNIT DATA FOR PRICING
            const factorCompra = detalle.factorConversion || 1;
            const unidadCompra = detalle.presentacion || detalle.unidad || 'UNIDAD';

            let precioBase = detalle.precioUnitario || 0;
            if (factorCompra > 1) {
                precioBase = detalle.precioUnitario / factorCompra;
            }

            f.patchValue({
                nombre: detalle.producto?.nombre || '',
                codigo: detalle.producto?.codigo || '',
                presentacion: detalle.producto?.presentacion || '',
                unidadBaseNombre: (detalle.producto as any)?.unidadBase || '',
                unidadIntermediaNombre: (detalle.producto as any)?.unidadIntermedia || '',
                unidadMayorNombre: (detalle.producto as any)?.unidadMayor || '',
                precioCompra: precioBase,
                fechaVencimiento: detalle.fechaVencimiento,
                nroLote: detalle.lote || '',
                tipo: 'PRODUCTO',
                cantidadCompra: detalle.cantidad,
                unidadCompra: unidadCompra,
                factorCompra: factorCompra,
                totalUnidades: (detalle.cantidad || 0) * factorCompra,
                stock: (detalle.cantidad || 0) * factorCompra,
                idCatalogo: detalle.producto?.id,
                idProducto: 0,
                idProveedor: detalle.idProveedor || null,
                proveedorRazonSocial: detalle.proveedorRazonSocial || '',
                ...formConfig
            });

            // Trigger initial calculations 
            mgr.calculatePrice(f, 'unit', 'standard');
            mgr.calculatePrice(f, 'unit', 'min');
            if (formConfig.manejaBlister) {
                mgr.calculatePrice(f, 'blister', 'standard');
                mgr.calculatePrice(f, 'blister', 'min');
            }
            if (formConfig.manejaCaja) {
                mgr.calculatePrice(f, 'caja', 'standard');
                mgr.calculatePrice(f, 'caja', 'min');
            }

            forms.push(f);
        });

        this.importForms.set(forms);
        setTimeout(() => this.cdr.detectChanges(), 0);
    }

    getMayorPrice(form: FormGroup): number {
        const base = form.get('precioCompra')?.value || 0;
        if (form.get('manejaCaja')?.value) {
            let factorRealCaja = form.get('factorCaja')?.value || 1;
            if (form.get('manejaBlister')?.value) {
                factorRealCaja *= (form.get('factorBlister')?.value || 1);
            }
            return base * factorRealCaja;
        }
        if (form.get('manejaBlister')?.value) {
            return base * (form.get('factorBlister')?.value || 1);
        }
        return base;
    }

    getMayorLabel(form: FormGroup): string {
        if (form.get('manejaCaja')?.value) return `Costo (${form.get('unidadMayorNombre')?.value || 'N/A'})`;
        if (form.get('manejaBlister')?.value) return `Costo (${form.get('unidadIntermediaNombre')?.value || 'N/A'})`;
        return `Costo (${form.get('unidadBaseNombre')?.value || 'N/A'})`;
    }

    // Used in template for import cards
    calculateProfitForDisplay(form: FormGroup, unitType: 'unit' | 'blister' | 'caja', priceType: 'standard' | 'min' | 'max') {
        const mgr = new ProductoPricingManager(form);

        let costo = form.get('precioCompra')?.value || 0;
        let venta = 0;

        if (unitType === 'blister') {
            costo *= (form.get('factorBlister')?.value || 1);
            if (priceType === 'standard') venta = form.get('precioVentaBlister')?.value || 0;
            if (priceType === 'min') venta = form.get('precioBlisterMin')?.value || 0;
        } else if (unitType === 'caja') {
            let factorRealCaja = form.get('factorCaja')?.value || 1;
            if (form.get('manejaBlister')?.value) {
                factorRealCaja *= (form.get('factorBlister')?.value || 1);
            }
            costo *= factorRealCaja;
            if (priceType === 'standard') venta = form.get('precioVentaCaja')?.value || 0;
            if (priceType === 'min') venta = form.get('precioCajaMin')?.value || 0;
        } else {
            if (priceType === 'standard') venta = form.get('precioVentaUnitario')?.value || 0;
            if (priceType === 'min') venta = form.get('precioUnitarioMin')?.value || 0;
        }

        if (venta <= 0 || costo <= 0) return null;

        const stats = mgr.calculateProfitStats(costo, venta);
        // Margen sobre Ventas
        const margen = ((venta - costo) / venta) * 100;

        return { ...stats, margen: margen.toFixed(2) };
    }

    cancelImport(): void {
        this.onClose();
    }

    currentProductoId: number | null = null;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['producto']) {
            // Asegurar que el formulario esté inicializado antes de proceder
            if (!this.form) {
                this.initForm();
                this.linkManagerSignals();
            }

            if (this.producto?.idProducto) {
                // Usar setTimeout para evitar ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => this.loading.set(true), 0);

                this.productoService.obtenerPorId(this.producto.idProducto).subscribe({
                    next: (res: any) => {
                        this.loading.set(false);
                        if (res && res.data) {
                            this.producto = { ...this.producto, ...res.data! };
                            this.cargarDatosProducto();
                        } else {
                            this.cargarDatosProducto();
                        }
                    },
                    error: (err) => {
                        console.error('Error loading product details', err);
                        this.loading.set(false);
                        this.cargarDatosProducto();
                    }
                });
            } else {
                this.resetState();
            }
        }
    }

    cargarDatosProducto(): void {
        if (this.producto) {
            this.currentProductoId = this.producto.idProducto || this.producto.id || null;

            const catalogoData: any = this.producto.catalogo || {};
            const formData: any = {
                idProducto: this.currentProductoId,
                nombre: catalogoData.nombre || (this.producto as any).nombre || '',
                codigo: catalogoData.codigo || (this.producto as any).codigo || '',
                descripcion: catalogoData.descripcion || (this.producto as any).descripcion || '',
                tipo: catalogoData.tipo || (this.producto as any).tipo || 'PRODUCTO',
                presentacion: catalogoData.presentacion || this.producto.presentacion || '',
                unidadBaseNombre: catalogoData.unidadBase || '',
                unidadIntermediaNombre: catalogoData.unidadIntermedia || '',
                unidadMayorNombre: catalogoData.unidadMayor || '',
                ...this.producto
            };

            console.group('🔍 DEBUG LOADING PRODUCT');
            console.log('Original Product:', this.producto);
            console.log('Final FormData:', formData);
            console.groupEnd();

            this.form.patchValue(formData, { emitEvent: false });
            this.importForms.set([this.form]);
            this.importStep.set(2);

            // Recalculate everything after patch
            setTimeout(() => {
                if (this.pricingManager) {
                    this.pricingManager.recalculateAllPrices();
                }
                this.cdr.markForCheck();
            }, 50);
        }
    }

    onSubmit(): void {
        ProductoFormFactory.validatePrices(this.form);

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            const priceErrors: string[] = [];
            const collect = (field: string, label: string) => {
                const ctrl = this.form.get(field);
                if (ctrl?.hasError('minPriceBase')) priceErrors.push(`${label}: Menor al Precio Compra`);
                if (ctrl?.hasError('maxPriceSale')) priceErrors.push(`${label}: Mayor al Precio Venta`);
                if (ctrl?.hasError('minPriceSale')) priceErrors.push(`${label}: Menor al Precio Venta`);
            };

            collect('precioVentaUnitario', 'P. Venta Unidad');
            collect('precioUnitarioMin', 'P. Min Unidad');

            if (this.form.get('manejaBlister')?.value) {
                collect('precioVentaBlister', 'P. Venta Blister');
                collect('precioBlisterMin', 'P. Min Blister');
            }
            if (this.form.get('manejaCaja')?.value) {
                collect('precioVentaCaja', 'P. Venta Caja');
                collect('precioCajaMin', 'P. Min Caja');
            }

            if (priceErrors.length > 0) {
                this.alertService.error('Errores de Validación', `Errores de Precios: ${priceErrors.join(', ')}`);
            } else {
                // Log all invalid fields for debugging
                const invalidFields = [];
                const controls = this.form.controls;
                for (const name in controls) {
                    if (controls[name].invalid) {
                        invalidFields.push({ name, errors: controls[name].errors });
                    }
                }
                console.error('❌ Formulario Inválido. Campos afectados:', invalidFields);
                this.alertService.warning('Formulario Incompleto', 'Por favor complete todos los campos requeridos marcados en rojo.');
            }
            return;
        }

        this.loading.set(true);
        this.error.set(null);

        const formValue = this.form.getRawValue();
        const idCatalogo = formValue.idCatalogo || this.producto?.idCatalogo || this.producto?.catalogo?.id;

        if (!idCatalogo) {
            this.handleError('Error: No se ha seleccionado un producto del catálogo');
            return;
        }

        const productId = formValue.idProducto || this.currentProductoId || 0;
        const productoRequest = ProductoMapper.toRequest(formValue, productId, this.currentSucursalId);
        productoRequest.idCatalogo = idCatalogo;

        const productoObservable = productId > 0
            ? this.productoService.actualizar(productId, productoRequest)
            : this.productoService.crear(productoRequest);

        productoObservable.subscribe({
            next: (prodRes) => {
                if (prodRes.success) {
                    this.loading.set(false);
                    this.alertService.success('Éxito', 'Producto guardado correctamente');
                    this.resetState();
                    this.guardado.emit();
                    this.cerrar.emit();
                } else {
                    this.handleError('Error al guardar datos del producto');
                }
            },
            error: (err) => this.handleError(err.error?.message || 'Error al guardar producto')
        });
    }

    private resetState(): void {
        if (this.form) {
            this.form.reset();
        }
        this.currentProductoId = null;
        this.producto = null;
        this.importForms.set([]);
        this.selectedImportItems.set([]);
        this.importStep.set(1);
        this.compraSearchResults.set([]);
        this.showCompraSearch.set(false);
        this.searchCompraTerm.set('');
        this.activeHistorialIndex.set(null);
        this.historialProductos.set([]);

        // Re-init defaults
        this.initForm();
        this.cdr.markForCheck();
    }

    async onBulkSubmit() {
        let hasErrors = false;
        this.importForms().forEach(f => {
            ProductoFormFactory.validatePrices(f);
            if (f.invalid) {
                f.markAllAsTouched();
                hasErrors = true;
            }
        });

        if (hasErrors) {
            console.group('❌ Error en Guardado Masivo');
            this.importForms().forEach((f, idx) => {
                if (f.invalid) {
                    const invalidFields = [];
                    for (const name in f.controls) {
                        if (f.controls[name].invalid) {
                            invalidFields.push({ name, errors: f.controls[name].errors });
                        }
                    }
                    console.error(`Tarjeta #${idx + 1} inválida:`, invalidFields);
                }
            });
            console.groupEnd();
            this.alertService.warning('Formularios Incompletos', 'Por favor revise los campos rojos en las tarjetas de producto.');
            return;
        }

        this.loading.set(true);
        let successCount = 0;
        let errors = 0;

        for (const f of this.importForms()) {
            const formValue = f.getRawValue();

            try {
                const idCatalogo = formValue.idCatalogo;
                if (!idCatalogo) {
                    errors++;
                    continue;
                }

                const productId = formValue.idProducto || 0;
                const productoRequest = ProductoMapper.toRequest(formValue, productId, this.currentSucursalId);
                productoRequest.idCatalogo = idCatalogo;

                const productoObservable = productId > 0
                    ? this.productoService.actualizar(productId, productoRequest)
                    : this.productoService.crear(productoRequest);

                const prodRes = await productoObservable.toPromise();

                if (prodRes && prodRes.success) {
                    successCount++;
                } else {
                    errors++;
                }
            } catch (e) {
                console.error(e);
                errors++;
            }
        }

        this.loading.set(false);
        if (errors === 0) {
            this.alertService.success('Éxito', `Se ${this.currentProductoId ? 'actualizó' : 'importaron'} ${successCount} productos correctamente`);
            this.resetState();
            this.guardado.emit();
            this.cerrar.emit();
        } else {
            this.alertService.warning('Atención', `Operación completada. Éxito: ${successCount}. Fallos: ${errors}.`);
            if (successCount > 0) this.guardado.emit();
        }
    }

    handleError(msg: string): void {
        this.error.set(msg);
        this.loading.set(false);
        this.alertService.error('Error', msg);
    }

    onClose(): void {
        if (confirm('¿Está seguro de cerrar? Los cambios no guardados se perderán.')) {
            this.cerrar.emit();
        }
    }

    isFieldInvalid(form: FormGroup, fieldName: string): boolean {
        const field = form.get(fieldName);
        return !!(field && field.invalid && (field.dirty || field.touched));
    }

    getErrorMessage(form: FormGroup, fieldName: string): string {
        const field = form.get(fieldName);
        if (!field || !(field.invalid && (field.dirty || field.touched))) return '';

        if (field.hasError('required')) return 'Este campo es requerido';
        if (field.hasError('maxlength')) return 'Longitud máxima excedida';
        if (field.hasError('min')) return 'El valor debe ser mayor o igual a 0';
        if (field.hasError('minPriceBase')) return 'Precio menor al costo';
        if (field.hasError('outOfRange')) return 'P. Venta menor al mínimo';
        if (field.hasError('maxPriceSale')) return 'Mínimo supera al P. Venta';

        return 'Inválido';
    }

    get isEditMode(): boolean {
        return !!this.producto?.idProducto;
    }

    get modalTitle(): string {
        return this.isEditMode ? 'Editar Producto' : 'Nuevo Producto';
    }

    cargarListas() {
        this.categoriaService.listar(0, 1000, '').subscribe(res => {
            if (res.success && res.data) this.categorias.set(res.data.content);
        });
        this.laboratorioService.listar(0, 1000, '').subscribe(res => {
            if (res.success && res.data) this.laboratorios.set(res.data.content);
        });
        this.principioActivoService.listar(0, 1000, '').subscribe(res => {
            if (res.success && res.data) this.principiosActivos.set(res.data.content);
        });
        this.accionTerapeuticaService.listar(0, 1000, '').subscribe(res => {
            if (res.success && res.data) this.accionesTerapeuticas.set(res.data.content);
        });
        this.ubicacionService.listar(0, 1000, '').subscribe(res => {
            if (res.success && res.data) this.ubicaciones.set(res.data.content);
        });
        if (this.currentSucursalId) {
            this.almacenService.getBySucursal(this.currentSucursalId).subscribe((res: any) => {
                const resData = res.data || res;
                const data = Array.isArray(resData) ? resData : (resData?.content ? resData.content : []);
                this.almacenes.set(data);
            });
        }
        this.unidadMedidaService.listarActivas().subscribe(res => {
            if (res.success && res.data) this.unidades.set(res.data.content || []);
        });
        this.proveedorService.listarTodos(0, 1000).subscribe(res => {
            if (res.success && res.data) this.proveedores.set(res.data.content);
        });
    }

    autoCompletarReferencia(form: FormGroup): void {
        const idCatalogo = Number(form.get('idCatalogo')?.value);
        const idLaboratorio = Number(form.get('idLaboratorio')?.value);

        if (!idCatalogo) {
            this.alertService.warning('Atención', 'Primero debe seleccionar un producto del catálogo.');
            return;
        }

        if (!idLaboratorio) {
            this.alertService.warning('Atención', 'Seleccione un laboratorio para buscar una coincidencia.');
            return;
        }

        this.loading.set(true);
        this.productoService.listarPorCatalogo(idCatalogo).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data && res.data.length > 0) {
                    // Buscar el producto más reciente con el mismo laboratorio
                    const referencia = res.data
                        .filter(p => Number(p.idLaboratorio || p.catalogo?.idLaboratorio) === idLaboratorio)
                        .sort((a, b) => (b.idProducto || 0) - (a.idProducto || 0))[0];

                    if (referencia) {
                        this.aplicarDatosDeReferencia(form, referencia);
                        this.alertService.success('Referencia Aplicada', 'Se han cargado los datos de ubicación, técnicos y márgenes de utilidad.');
                    } else {
                        this.alertService.info('Sin Referencia', 'No se encontraron registros previos de este producto con el laboratorio seleccionado.');
                    }
                } else {
                    this.alertService.info('Sin Datos', 'Este producto aún no tiene historial registrado en el almacén.');
                }
            },
            error: (err) => {
                this.loading.set(false);
                console.error('Error al buscar referencia:', err);
                this.alertService.error('Error', 'No se pudo consultar el historial del producto.');
            }
        });
    }

    llenadoRapido(form: FormGroup): void {
        const idCatalogo = Number(form.get('idCatalogo')?.value);
        if (!idCatalogo) {
            this.alertService.warning('Atención', 'Debe haber un catálogo seleccionado.');
            return;
        }

        this.loading.set(true);
        this.productoService.listarPorCatalogo(idCatalogo).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data && res.data.length > 0) {
                    // Obtener el más reciente por ID
                    const referencia = [...res.data].sort((a, b) => (b.idProducto || 0) - (a.idProducto || 0))[0];
                    this.aplicarDatosDeReferencia(form, referencia);
                    this.alertService.success('Llenado Rápido', 'Se han cargado los datos del último producto registrado.');
                } else {
                    this.alertService.info('Sin Historial', 'No se encontraron registros previos de este catálogo.');
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudo consultar el historial.');
            }
        });
    }

    // Modificado para rastrear índice de tarjeta activa


    // ... (rest of code logic remains same until toggleHistorial)

    toggleHistorial(form: FormGroup, index: number): void {
        if (this.activeHistorialIndex() === index) {
            this.activeHistorialIndex.set(null);
            return;
        }

        const idCatalogo = Number(form.get('idCatalogo')?.value);
        if (!idCatalogo) {
            this.alertService.warning('Atención', 'Debe haber un catálogo seleccionado.');
            return;
        }

        this.loading.set(true);
        this.productoService.listarPorCatalogo(idCatalogo).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    this.historialProductos.set(res.data.sort((a, b) => (b.idProducto || 0) - (a.idProducto || 0)));
                    this.activeHistorialIndex.set(index);
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudo cargar el historial.');
            }
        });
    }

    seleccionarDeHistorial(form: FormGroup, referencia: Producto): void {
        this.aplicarDatosDeReferencia(form, referencia);
        this.activeHistorialIndex.set(null);
        this.alertService.success('Historial Aplicado', 'Datos cargados correctamente.');
    }

    private aplicarDatosDeReferencia(form: FormGroup, referencia: any): void {
        form.patchValue({
            idLaboratorio: referencia.idLaboratorio || referencia.catalogo?.idLaboratorio,
            idAlmacen: referencia.idAlmacen,
            idUbicacion: referencia.idUbicacion,
            diasAlertaVencimiento: referencia.diasAlertaVencimiento || 30,
            codDigemid: referencia.codDigemid,
            codigoBarra: referencia.codigoBarra,
            tipoGananciaUnidad: referencia.tipoGananciaUnidad || 'PORCENTAJE',
            gananciaUnidad: referencia.gananciaUnidad,
            gananciaUnidadMin: referencia.gananciaUnidadMin,
            manejaBlister: referencia.manejaBlister,
            factorBlister: referencia.factorBlister,
            tipoGananciaBlister: referencia.tipoGananciaBlister || 'PORCENTAJE',
            gananciaBlister: referencia.gananciaBlister,
            gananciaBlisterMin: referencia.gananciaBlisterMin,
            manejaCaja: referencia.manejaCaja,
            factorCaja: referencia.factorCaja,
            tipoGananciaCaja: referencia.tipoGananciaCaja || 'PORCENTAJE',
            gananciaCaja: referencia.gananciaCaja,
            gananciaCajaMin: referencia.gananciaCajaMin
        });

        // Forzar el recálculo inmediato de precios usando los márgenes cargados
        const mgr = new ProductoPricingManager(form);
        mgr.recalculateAllPrices();
    }

    fillTestData(set: number): void {
        const dataSets = [
            {
                tipo: 'PRODUCTO',
                nombre: 'PARACETAMOL 500MG TAB',
                detalle: 'Caja x 100 tabletas, analgésico y antipirético',
                idNivel: 1,
                precioCompra: 15.50,
                precioVentaUnitario: 0.50,
                precioUnitarioMin: 0.40,
                precioUnitarioMax: 0.80,
                tipoGananciaUnidad: 'FIJO',
                manejaUnidad: true,
                stockUnidad: 1000,
                manejaBlister: true,
                factorBlister: 10,
                precioVentaBlister: 4.50,
                manejaCaja: true,
                factorCaja: 100,
                precioVentaCaja: 40.00,
                estado: true
            },
            // ... others
        ];
        if (set >= 1 && set <= 3) {
            this.form.patchValue(dataSets[set - 1]);
        }
    }
}