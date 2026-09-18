import { Component, OnInit, signal, computed, effect, HostListener, ElementRef, Input, OnChanges, SimpleChanges, inject, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { CompraService } from '../../services/compra.service';
import { ProveedorService } from '../../services/proveedor.service';
import { CatalogoService } from '../../../configuraciones/services/catalogo.service';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';

import { EventEmitter, Output } from '@angular/core';
import { AlertService } from '../../../../core/services/alert.service';
import { UnidadMedidaService } from '../../../almacen/service/unidad-medida.service';
import { AuthService } from '../../../auth/services/auth.service';

import { ModalComponent } from '../../../../shared/components/modal/modal';
import { RowCalculationUtils } from '../../../../shared/utils/row-calculation.utils';
import { FormSwitchComponent } from '../../../../shared/components/forms/form-switch/form-switch.component';
import { CatalogoFormComponent } from '../../../configuraciones/catalogo/catalogo-form/catalogo-form.component';
import { CronogramaPagoService } from '../../services/cronograma-pago.service';
import { WebSocketService, Notification } from '../../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-registro-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, SearchableSelectComponent, FormInputComponent, FormSelectComponent, ModalComponent, CatalogoFormComponent, FormSwitchComponent],
    templateUrl: './registro-form.component.html'
})
export class RegistroFormComponent implements OnInit, OnChanges {
    @Input() idCompra: number | null = null;
    form!: FormGroup;
    loading = signal(false);
    @ViewChildren(FormInputComponent) inputs!: QueryList<FormInputComponent>;
    proveedores = signal<any[]>([]);
    datosProveedor = signal<any>(null);
    productos = signal<any[]>([]);
    selectedFilter = signal('PRODUCTO');
    // Custom product search
    searchTermProducto = signal('');
    showDropdownProducto = signal(false);
    showCatalogoModal = signal(false);

    originalTotal = signal(0);
    dimensiones = signal({ width: '210mm', height: '297mm' });

    abrirModalCatalogo() {
        this.showCatalogoModal.set(true);
    }

    cerrarModalCatalogo() {
        this.showCatalogoModal.set(false);
    }

    onCatalogoGuardado() {
        this.cerrarModalCatalogo();
        this.cargarProductos(); // Refresh list to include new item
        this.alertService.toast('Producto agregado correctamente', 'success');
    }

    filteredProductos = computed(() => {
        const term = this.searchTermProducto().toLowerCase();
        if (!term) return this.productos().slice(0, 20);
        return this.productos().filter(p =>
            p.nombre.toLowerCase().includes(term) ||
            p.codigo?.toLowerCase().includes(term)
        ).slice(0, 20);
    });

    // Options for selects
    tipoComprobanteOptions = [
        { label: '📄 Factura', value: 'FACTURA' },
        { label: '🧾 Boleta', value: 'BOLETA' },
        { label: '📦 Guía de Remisión', value: 'GUIA_REMISION' }
    ];

    condicionPagoOptions = [
        { label: '💵 Contado', value: 'CONTADO' },
        { label: '🏦 Crédito', value: 'CREDITO' }
    ];

    monedaOptions = [
        { label: 'S/ Soles', value: 'PEN' },
        { label: '$ Dólares', value: 'USD' }
    ];

    // Totals computed from the FormArray
    totals = signal({ subtotal: 0, gravada: 0, exonerada: 0, inafecta: 0, igv: 0, totalCalculado: 0, total: 0 });

    // History and Price State
    historialProducto = signal<any[]>([]);
    showHistorialModal = signal(false);
    selectedRowIndex = signal<number | null>(null);
    maxPreciosMap = new Map<number, number>(); // idCatalogo -> precioMaximo (historico)
    productDataMap = signal(new Map<number, any>()); // idCatalogo -> objeto producto completo reactive

    // Calculator State
    showCalculadoraModal = signal(false);
    calculadoraIndex = signal<number | null>(null);
    calculadoraTotal = signal(0);
    calculadoraIncluyeIgv = signal(true);

    // Kafka/Payment States
    pagoSolicitado = signal(false);
    pagoConfirmado = signal(false);

    // Nueva señal para detectar si el proveedor tiene saldo a favor suficiente
    tieneSaldoSuficiente = computed(() => {
        const prov = this.datosProveedor();
        const total = this.totals().total;
        if (!prov || total <= 0) return false;
        // Asumimos que saldo negativo en el proveedor es saldo a favor del sistema (pre-pago)
        // El usuario indica que -332.76 es saldo a favor.
        return prov.saldo < 0 && Math.abs(prov.saldo) >= total;
    });

    opcionPagoSeleccionada = signal<'COMPLETO' | 'SALDO'>('SALDO');

    protected Math = Math; // Hacer Math disponible para el template

    private wsSubscription?: Subscription;
    private webSocketService = inject(WebSocketService);

    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    constructor(
        private fb: FormBuilder,
        private compraService: CompraService,
        private proveedorService: ProveedorService,
        private catalogoService: CatalogoService,
        private router: Router,
        private elementRef: ElementRef,
        private alertService: AlertService,
        private unidadMedidaService: UnidadMedidaService,
        private authService: AuthService,
        private sanitizer: DomSanitizer,
        private cronogramaPagoService: CronogramaPagoService
    ) {
        effect(() => {
            if (this.form) {
                if (this.pagoSolicitado() || this.pagoConfirmado()) {
                    this.form.disable({ emitEvent: false });
                } else {
                    this.form.enable({ emitEvent: false });
                }
            }
        });
    }

    unidadesMap = new Map<number, string>();
    rowUnitOptions = signal<any[][]>([]);
    unidades = signal<any[]>([]);

    @HostListener('document:click', ['$event'])
    onClickOutside(event: Event) {
        if (!this.elementRef.nativeElement.contains(event.target)) {
            const clickedInside = this.elementRef.nativeElement.querySelector('.relative.group')?.contains(event.target);
            if (!clickedInside) {
                this.showDropdownProducto.set(false);
            }
        }
    }

    ngOnInit(): void {
        this.initForm();
        this.cargarProveedores();
        this.cargarProductos();
        this.cargarUnidades();
        this.setupWebSocket();
    }

    setupWebSocket() {
        this.webSocketService.connect(this.authService.getUserIdFromToken()?.toString());
        this.wsSubscription = this.webSocketService.notifications$.subscribe((notif: Notification) => {
            console.log('Notificación recibida en Formulario:', notif);

            // Lógica para detectar confirmación de pago de esta compra
            const isPayment = notif.modulo === 'PAGOS' ||
                notif.modulo === 'COMPRAS' || // También considerar compras
                notif.titulo.toUpperCase().includes('PAGO') ||
                (typeof notif.contenido === 'string' && notif.contenido.toUpperCase().includes('PAGO'));

            if (isPayment && this.idCompra) {
                // Intentar extraer el ID de diferentes formas
                let idNotif: any = null;

                if (typeof notif.contenido === 'number') {
                    idNotif = notif.contenido;
                } else if (typeof notif.contenido === 'object' && notif.contenido !== null) {
                    idNotif = notif.contenido.idCompra || notif.contenido.id;
                } else if (typeof notif.contenido === 'string') {
                    // Buscar patrón #ID (ejemplo: "Compra #3")
                    const match = notif.contenido.match(/#(\d+)/);
                    if (match) {
                        idNotif = match[1];
                    }
                }

                if (idNotif && Number(idNotif) === Number(this.idCompra)) {
                    this.pagoConfirmado.set(true);
                    this.pagoSolicitado.set(true); // Asegurar que el botón de solicitud permanezca bloqueado

                    // Refrescar Saldo del Proveedor
                    if (this.datosProveedor()?.id) {
                        this.proveedorService.obtenerPorId(this.datosProveedor().id).subscribe(res => {
                            if (res.success && res.data) this.datosProveedor.set(res.data);
                        });
                    }

                    this.alertService.success('Pago Confirmado', 'El pago ha sido procesado exitosamente.');
                    this.guardado.emit();
                }
            }
        });
    }

    ngOnDestroy() {
        if (this.wsSubscription) {
            this.wsSubscription.unsubscribe();
        }
    }

    cargarUnidades() {
        this.unidadMedidaService.listarActivas().subscribe(res => {
            if (res.success && res.data && res.data.content) {
                this.unidades.set(res.data.content);
                res.data.content.forEach((u: any) => this.unidadesMap.set(Number(u.id), u.nombre));
            }
        });
    }

    getNombreUnidad(id: number): string {
        return this.unidadesMap.get(id) || '';
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['idCompra']) {
            if (this.idCompra) {
                this.cargarCompra(this.idCompra);
            } else {
                this.resetForm();
            }
        }
    }

    cargarCompra(id: number) {
        this.loading.set(true);
        this.compraService.obtener(id).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    const data = res.data;
                    this.form.patchValue({
                        idProveedor: data.proveedor.id,
                        tipoComprobante: data.tipoComprobante,
                        serie: data.serie,
                        correlativo: data.correlativo,
                        fechaEmision: data.fechaEmision,
                        fechaVencimiento: data.fechaVencimiento,
                        condicionPago: data.condicionPago,
                        moneda: data.moneda,
                        percepcion: data.percepcion,
                        ajusteRedondeo: data.ajusteRedondeo,
                        solicitarFondo: data.solicitarFondo,
                        igv: data.igv,
                        total: data.total
                    });

                    // Se omite la llamada manual al proveedor ya que se dispara por el patchValue
                    if (data.proveedor?.id) {
                        this.proveedorService.obtenerPorId(data.proveedor.id).subscribe(resProv => {
                            if (resProv.success) this.datosProveedor.set(resProv.data);
                        });
                    }
                    // Update payment states based on purchase status
                    if (data.estado === 'SOLICITADO') {
                        this.pagoSolicitado.set(true);
                        this.pagoConfirmado.set(false);
                    } else if (data.estado === 'PAGADO' || data.estado === 'PROCESADO') {
                        this.pagoSolicitado.set(true);
                        this.pagoConfirmado.set(true);
                    } else {
                        this.pagoSolicitado.set(false);
                        this.pagoConfirmado.set(false);
                    }

                    data.detalles.forEach((d: any) => {
                        const prod = d.producto;
                        if (prod) {
                            // Usamos los datos del producto que ya vienen en la respuesta
                            this.productDataMap.update(map => {
                                map.set(prod.id, prod);
                                return new Map(map);
                            });

                            this.agregarDetalle(prod, d.cantidad);

                            const row = this.detalles.at(this.detalles.length - 1);

                            // Salvaguarda: si no viene el ID, buscar por nombre en el mapa de unidades (ya cargado en ngOnInit)
                            let idUnidad = d.idUnidadMedida;
                            if (!idUnidad && d.unidad) {
                                idUnidad = Array.from(this.unidadesMap.entries())
                                    .find(([_, name]) => name.toUpperCase() === d.unidad.toUpperCase())?.[0];
                            }

                            row.patchValue({
                                idUnidadMedida: idUnidad,
                                factorConversion: d.factorConversion || 1,
                                precioUnitario: d.precioUnitario,
                                manejaLotes: prod.manejaLotes ?? true,
                                lote: d.lote,
                                fechaVencimiento: d.fechaVencimiento,
                                porcentajeDescuento: d.porcentajeDescuento,
                                porcentajeDescuento2: d.porcentajeDescuento2,
                                tipoAfectacion: d.tipoAfectacion,
                                esBonificacion: d.esBonificacion,
                                presentacion: prod.presentacion || ''
                            });
                        }
                    });
                }
            },
            error: () => this.loading.set(false),
            complete: () => {
            }
        });
    }

    deshabilitarParaSoloResumen() {
        // Deshabilitar todo excepto los campos de ajuste y totales
        const camposPermitidos = ['percepcion', 'ajusteRedondeo', 'valorVentaGravado', 'valorVentaExonerado', 'valorVentaInafecto', 'igv', 'total', 'detalles'];
        Object.keys(this.form.controls).forEach(key => {
            if (!camposPermitidos.includes(key)) {
                this.form.get(key)?.disable({ emitEvent: false });
            }
        });

        // También deshabilitar el FormArray de detalles
        this.detalles.controls.forEach(control => {
            control.disable({ emitEvent: false });
        });
    }

    resetForm() {
        if (this.form) {
            this.form.reset();
            while (this.detalles.length !== 0) {
                this.detalles.removeAt(0);
            }
            this.rowUnitOptions.set([]); // Limpiar opciones de unidades
            this.form.patchValue({
                tipoComprobante: 'FACTURA',
                moneda: 'PEN',
                fechaEmision: new Date().toISOString().substring(0, 10),
                condicionPago: 'CREDITO',
                percepcion: 0,
                ajusteRedondeo: 0,
                solicitarFondo: true,
                valorVentaGravado: 0,
                valorVentaExonerado: 0,
                valorVentaInafecto: 0,
                igv: 0,
                total: 0
            });
            this.datosProveedor.set(null);
            this.originalTotal.set(0);
            this.totals.set({ subtotal: 0, gravada: 0, exonerada: 0, inafecta: 0, igv: 0, totalCalculado: 0, total: 0 });
        }
    }

    initForm() {
        this.form = this.fb.group({
            idProveedor: [null, Validators.required],
            tipoComprobante: ['FACTURA', Validators.required],
            serie: ['', [Validators.required, Validators.maxLength(10)]],
            correlativo: ['', [Validators.required, Validators.maxLength(20)]],
            fechaEmision: [new Date().toISOString().split('T')[0], Validators.required],
            fechaVencimiento: [null],
            condicionPago: ['CREDITO'],
            montoPagado: [0],
            moneda: ['PEN'],
            percepcion: [0],
            ajusteRedondeo: [0],
            solicitarFondo: [true],
            valorVentaGravado: [0],
            valorVentaExonerado: [0],
            valorVentaInafecto: [0],
            igv: [0],
            total: [0],
            detalles: this.fb.array([])
        });

        // Observar cambios en el proveedor para actualizar datosProveedor y mostrar el saldo desde el servicio
        this.form.get('idProveedor')?.valueChanges.subscribe(id => {
            if (id) {
                this.proveedorService.obtenerPorId(Number(id)).subscribe(res => {
                    if (res.success) {
                        this.datosProveedor.set(res.data);
                        // Resetear la selección de pago al cambiar proveedor
                        this.opcionPagoSeleccionada.set('COMPLETO');
                    }
                });
            } else {
                this.datosProveedor.set(null);
            }
        });
        // Observar cambios en condición de pago para resetear montoPagado
        this.form.get('condicionPago')?.valueChanges.subscribe(val => {
            if (val === 'CONTADO') {
                this.form.get('montoPagado')?.setValue(0);
            }
        });

        // Observar cambios generales para recalcular totales
        this.form.get('detalles')?.valueChanges.subscribe(() => {
            this.calculateTotals();
        });

        // Observar cambios en percepción y ajuste por redondeo
        this.form.get('percepcion')?.valueChanges.subscribe(() => this.calculateTotals());
        this.form.get('ajusteRedondeo')?.valueChanges.subscribe(() => this.calculateTotals());
    }

    limpiarProveedor() {
        if (!this.pagoSolicitado() && !this.pagoConfirmado() && !this.idCompra) {
            this.form.get('idProveedor')?.setValue(null);
            this.datosProveedor.set(null);
        }
    }

    get detalles() {
        return this.form.get('detalles') as FormArray;
    }

    agregarDetalle(producto: any = null, cantidad: number = 1) {
        // Prepare Unit Options
        const options: any[] = [];
        let defaultUnitId = null;
        let defaultFactor = 1;

        if (producto) {
            // Priority: Caja -> Blister -> Base (MISMA LÓGICA QUE EN GUARDAR/REGISTRO)
            if (producto.manejaCaja && producto.idUnidadMayor) {
                const name = this.unidadesMap.get(Number(producto.idUnidadMayor)) || 'Caja';
                const factorCajaAcumulado = (producto.factorCaja || 1) * (producto.manejaBlister ? (producto.factorBlister || 1) : 1);
                options.push({ id: Number(producto.idUnidadMayor), name, factor: factorCajaAcumulado });
            }
            if (producto.manejaBlister && producto.idUnidadIntermedia) {
                const name = this.unidadesMap.get(Number(producto.idUnidadIntermedia)) || 'Blister';
                options.push({ id: Number(producto.idUnidadIntermedia), name, factor: producto.factorBlister });
            }
            if (producto.manejaUnidad && producto.idUnidadBase) {
                const name = this.unidadesMap.get(Number(producto.idUnidadBase)) || 'Unidad';
                options.push({ id: Number(producto.idUnidadBase), name, factor: 1 });
            }

            // Fallback: If no units configured, default to UNIDAD with factor 1
            if (options.length === 0) {
                const defaultName = this.unidadesMap.get(1) || 'Unidad';
                options.push({ id: 1, name: defaultName, factor: 1 });
            }

            if (options.length > 0) {
                defaultUnitId = options[0].id;
                defaultFactor = options[0].factor;
            }
        } else {
            // Fallback when producto is null
            const defaultName = this.unidadesMap.get(1) || 'Unidad';
            options.push({ id: 1, name: defaultName, factor: 1 });
            defaultUnitId = 1;
        }

        // Update rowUnitOptions signal BEFORE adding the row
        this.rowUnitOptions.update(opts => [...opts, options]);

        const detalle = this.fb.group({
            idProducto: [producto?.id || null, Validators.required],
            codigo: [producto?.codigo || ''],
            nombreProducto: [producto?.nombre || ''],
            presentacion: [producto?.presentacion || ''],
            idUnidadMedida: [defaultUnitId, Validators.required],
            factorConversion: [defaultFactor],
            cantidad: [cantidad, [Validators.required, Validators.min(0.01)]],
            precioUnitario: [0, [Validators.required, Validators.min(0)]],
            porcentajeDescuento: [0, [Validators.min(0), Validators.max(100)]],
            porcentajeDescuento2: [0, [Validators.min(0), Validators.max(100)]],
            manejaLotes: [producto?.manejaLotes ?? false],
            lote: [''],
            fechaVencimiento: [''],
            tipoAfectacion: [(producto?.tipoAfectacion || '').trim() || 'GRAVADO', Validators.required],
            esBonificacion: [false],
            incluyeIgv: [true],
            totalItem: [0]
        });

        // Escuchar cambios en precio y bonificación
        detalle.get('precioUnitario')?.valueChanges.subscribe(() => this.calculateTotals());
        detalle.get('esBonificacion')?.valueChanges.subscribe(val => {
            if (val) this.aplicarPrecioBonificacion(detalle);
            this.calculateTotals();
        });
        detalle.get('incluyeIgv')?.valueChanges.subscribe(() => this.calculateTotals());

        // Escuchar cambios en unidad/factor para desglose
        detalle.get('idUnidadMedida')?.valueChanges.subscribe(newId => {
            const numId = Number(newId);
            const index = this.detalles.controls.indexOf(detalle);
            if (index >= 0) {
                const currentOpts = this.rowUnitOptions()[index];
                const opt = currentOpts?.find(o => o.id === numId);
                if (opt) {
                    const oldFactor = detalle.get('factorConversion')?.value || 1;
                    const newFactor = opt.factor || 1;

                    detalle.get('factorConversion')?.setValue(newFactor, { emitEvent: false });
                    detalle.get('presentacion')?.setValue(opt.name, { emitEvent: false });

                    // Ajustar el precio unitario si cambia la presentación (proporcional al factor)
                    const currentPrice = detalle.get('precioUnitario')?.value || 0;
                    if (currentPrice > 0 && oldFactor > 0 && newFactor > 0 && oldFactor !== newFactor) {
                        const priceBase = currentPrice / oldFactor;
                        const newPrice = priceBase * newFactor;
                        detalle.get('precioUnitario')?.setValue(Number(newPrice.toFixed(4)));
                    }
                }
            }
        });

        this.detalles.push(detalle);

        // Fetch max price from history ONLY for NEW purchases to avoid network flooding on load
        if (producto?.id && !this.idCompra) {
            this.compraService.obtenerPrecioMaximoHistorico(producto.id).subscribe(res => {
                if (res.success) {
                    this.maxPreciosMap.set(producto.id, res.data);
                }
            });
        }
    }

    validarMaximo(detalle: FormGroup) {
        // Método eliminado: Ya no se fuerza el precio máximo
    }

    aplicarPrecioBonificacion(detalle: FormGroup) {
        const idCatalogo = detalle.get('idProducto')?.value;
        let maxFound = this.maxPreciosMap.get(idCatalogo) || 0;

        this.detalles.controls.forEach(ctrl => {
            const row = ctrl as FormGroup;
            if (row.get('idProducto')?.value === idCatalogo && !row.get('esBonificacion')?.value) {
                const pRow = Number(row.get('precioUnitario')?.value) || 0;
                if (pRow > maxFound) maxFound = pRow;
            }
        });

        detalle.get('precioUnitario')?.setValue(maxFound, { emitEvent: false });
    }

    // Calcular desglose de precios para Tooltip (Estimación y Catálogo) - SOLO VISTA
    getDesgloseTooltip(index: number): { estimacion: any[], catalogo: any[] } {
        const row = this.detalles.at(index);
        if (!row) return { estimacion: [], catalogo: [] };

        const productoId = Number(row.get('idProducto')?.value);
        if (!productoId) return { estimacion: [], catalogo: [] };

        const producto = this.productDataMap().get(productoId);
        if (!producto) return { estimacion: [], catalogo: [] };

        // PRECIO INGRESADO EN LA FILA
        const precioIngresado = Number(row.get('precioUnitario')?.value) || 0;
        const factorIngresado = Number(row.get('factorConversion')?.value) || 1;

        const niveles = [];

        // El factor de la caja es acumulativo: factorCaja (blisters) * factorBlister (unidades)
        let factorCajaReal = 1;
        if (producto.manejaCaja) {
            factorCajaReal = (producto.factorCaja || 1) * (producto.manejaBlister ? (producto.factorBlister || 1) : 1);
            niveles.push({
                nombre: producto.unidadMayor || 'Caja',
                factor: factorCajaReal
            });
        }

        if (producto.manejaBlister) {
            niveles.push({
                nombre: producto.unidadIntermedia || 'Blister',
                factor: Number(producto.factorBlister) || 1
            });
        }

        niveles.push({
            nombre: producto.unidadBase || 'Unidad',
            factor: 1
        });

        // Ordenar por factor (Jerarquía: Mayor -> Menor)
        const nivelesOrdenados = niveles.sort((a, b) => b.factor - a.factor);

        // Precio unidad base
        const precioUnidadBase = precioIngresado / factorIngresado;

        // 2. GENERAR ESTIMACIÓN (Nivel Superior toma el precio ingresado proporcional)
        const estimacion = nivelesOrdenados.map(n => ({
            nombre: n.nombre,
            precio: (precioUnidadBase * n.factor).toFixed(3)
        }));

        // 3. PRECIOS CATÁLOGO (Usar nombres literales del backend)
        const catalogo = [];
        if (producto.precioVentaCaja > 0) {
            catalogo.push({ nombre: producto.unidadMayor || 'Caja', precio: Number(producto.precioVentaCaja) });
        }
        if (producto.precioVentaBlister > 0) {
            catalogo.push({ nombre: producto.unidadIntermedia || 'Blister', precio: Number(producto.precioVentaBlister) });
        }
        if (producto.precioVentaUnitario > 0) {
            catalogo.push({ nombre: producto.unidadBase || 'Unidad', precio: Number(producto.precioVentaUnitario) });
        }

        const catalogoTop = catalogo
            .sort((a, b) => b.precio - a.precio)
            .slice(0, 2)
            .map(item => ({ ...item, precio: item.precio.toFixed(2) }));

        return { estimacion, catalogo: catalogoTop };
    }

    abrirCalculadora(index: number) {
        const row = this.detalles.at(index);
        this.calculadoraIndex.set(index);
        this.calculadoraTotal.set(row.get('totalItem')?.value || 0);
        this.calculadoraIncluyeIgv.set(true);
        this.showCalculadoraModal.set(true);
    }

    cerrarCalculadora() {
        this.showCalculadoraModal.set(false);
        this.calculadoraIndex.set(null);
    }

    aplicarCalculoInverso() {
        const index = this.calculadoraIndex();
        if (index === null) return;

        const row = this.detalles.at(index);
        const total = this.calculadoraTotal();
        const incluyeIgv = this.calculadoraIncluyeIgv();
        const cantidad = Number(row.get('cantidad')?.value) || 1;
        const d1 = Number(row.get('porcentajeDescuento')?.value) || 0;
        const d2 = Number(row.get('porcentajeDescuento2')?.value) || 0;
        const tipo = row.get('tipoAfectacion')?.value || 'GRAVADO';

        const nuevoPrecio = RowCalculationUtils.calcularPrecioUnitarioInverso(
            total, cantidad, d1, d2, tipo, incluyeIgv
        );

        row.get('precioUnitario')?.setValue(nuevoPrecio);
        this.cerrarCalculadora();
        this.alertService.toast('Precio calculado e ingresado', 'success');
    }

    getItemCalculationBreakdown(index: number): any {
        const row = this.detalles.at(index);
        if (!row) return null;

        const cantidad = Number(row.get('cantidad')?.value) || 0;
        let precio = Number(row.get('precioUnitario')?.value) || 0;
        const d1 = Number(row.get('porcentajeDescuento')?.value) || 0;
        const d2 = Number(row.get('porcentajeDescuento2')?.value) || 0;
        const tipo = row.get('tipoAfectacion')?.value || 'GRAVADO';
        const incluyeIgv = row.get('incluyeIgv')?.value;

        // El precio ingresado es SIEMPRE la base imponible
        return RowCalculationUtils.generarDesglosePrecios(cantidad, precio, d1, d2, tipo, incluyeIgv);
    }

    verHistorial(index: number) {
        const row = this.detalles.at(index);
        const idCatalogo = row.get('idProducto')?.value;
        if (!idCatalogo) return;

        this.loading.set(true);
        this.compraService.obtenerHistorialPorProducto(idCatalogo).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success) {
                    const results = (res.data as any).content ? (res.data as any).content : res.data;
                    this.historialProducto.set(results);
                    this.showHistorialModal.set(true);
                }
            },
            error: () => this.loading.set(false)
        });
    }

    cerrarHistorial() {
        this.showHistorialModal.set(false);
    }

    // Helper to get options by index (for HTML)
    getUnitOptions(index: number) {
        return this.rowUnitOptions()[index] || [];
    }

    onUnitChange(index: number, newId: number) {
        const opts = this.rowUnitOptions()[index];
        const opt = opts?.find(o => o.id === newId);
        if (opt) {
            this.detalles.at(index).get('factorConversion')?.setValue(opt.factor);
        }
    }

    eliminarDetalle(index: number) {
        this.detalles.removeAt(index);
        this.rowUnitOptions.update(opts => opts.filter((_, i) => i !== index));
    }

    tipoAfectacionOptions = [
        { value: 'GRAVADO', label: 'Gravado' },
        { value: 'EXONERADO', label: 'Exonerado' },
        { value: 'INAFECTO', label: 'Inafecto' }
    ];

    navegarTeclado(event: KeyboardEvent, i: number, col: number) {
        const key = event.key;
        const totalCols = 6; // Cantidad, Precio, Lote, Vencimiento, Dcto1, Dcto2
        let nextRow = i;
        let nextCol = col;

        if (key === 'ArrowUp') {
            nextRow = i - 1;
        } else if (key === 'ArrowDown') {
            nextRow = i + 1;
        } else if (key === 'ArrowLeft') {
            nextCol = col - 1;
        } else if (key === 'ArrowRight') {
            nextCol = col + 1;
        } else {
            return;
        }

        // Limites
        if (nextRow < 0 || nextRow >= this.detalles.length) return;
        if (nextCol < 0 || nextCol >= totalCols) return;

        event.preventDefault();

        // Buscar el input en la QueryList
        // Ocultar inputs de cabecera si los hay (asumimos que los primeros inputs son de la cabecera)
        // Necesitamos saber cuántos inputs hay antes de la tabla
        const headerInputsCount = 10; // Aproximado: Proveedor(1), Serie(1), Correlativo(1), Fechas(2), etc.
        // Es mejor filtrar por una clase específica o usar un identificador

        const tableInputs = this.inputs.toArray().filter(input => {
            // Podríamos añadir una propiedad 'isTableInput' o similar, pero por ahora buscaremos por el índice
            // Una mejor forma es usar un ID o atributo en el HTML y buscar en el DOM
            return true;
        });

        // Alternativa: usar document.querySelector con IDs dinámicos
        const targetId = `input-${nextRow}-${nextCol}`;
        const targetElement = document.getElementById(targetId);
        if (targetElement) {
            // Si el elemento es un app-form-input, necesitamos su instancia
            // Pero querySelector solo da el HTMLElement. 
            // Usaremos la estrategia de índices en la QueryList si es predecible, 
            // o simplemente buscaremos el input dentro del elemento.
            const input = targetElement.querySelector('input');
            input?.focus();
            input?.select();
        }
    }

    calculateTotals() {
        let gravada = 0;
        let exonerada = 0;
        let inafecta = 0;
        let igv = 0;
        let totalCalculado = 0;

        this.detalles.controls.forEach(ctrl => {
            const val = ctrl.value;
            if (val.esBonificacion) {
                ctrl.get('totalItem')?.setValue(0, { emitEvent: false });
                return;
            }

            const cantidad = Number(val.cantidad) || 0;
            const precio = Number(val.precioUnitario) || 0;

            const d1 = Number(val.porcentajeDescuento) || 0;
            const d2 = Number(val.porcentajeDescuento2) || 0;
            const incluyeIgv = val.incluyeIgv;
            const tipo = val.tipoAfectacion || '10';

            // Cálculo de valor neto paso a paso
            const valorBruto = Number((cantidad * precio).toFixed(2));
            const descuento1 = Number((valorBruto * (d1 / 100)).toFixed(2));
            const valorTrasD1 = Number((valorBruto - descuento1).toFixed(2));
            const descuento2 = Number((valorTrasD1 * (d2 / 100)).toFixed(2));
            const valorNeto = Number((valorTrasD1 - descuento2).toFixed(2));

            if (tipo === '10' || tipo === 'GRAVADO') {
                const igvItem = Number((valorNeto * 0.18).toFixed(2));
                const totalVisibleItem = incluyeIgv ? Number((valorNeto + igvItem).toFixed(2)) : valorNeto;

                gravada += valorNeto;
                igv += igvItem;
                ctrl.get('totalItem')?.setValue(totalVisibleItem, { emitEvent: false });
            } else if (tipo === '20' || tipo === 'EXONERADO') {
                exonerada += valorNeto;
                ctrl.get('totalItem')?.setValue(valorNeto, { emitEvent: false });
            } else if (tipo === '30' || tipo === 'INAFECTO') {
                inafecta += valorNeto;
                ctrl.get('totalItem')?.setValue(valorNeto, { emitEvent: false });
            } else {
                // Por seguridad, si no se reconoce, no aplicamos IGV pero sumamos a gravada
                gravada += valorNeto;
                ctrl.get('totalItem')?.setValue(valorNeto, { emitEvent: false });
            }
        });

        totalCalculado = Number((gravada + exonerada + inafecta + igv).toFixed(2));

        const subtotal = Number((gravada + exonerada + inafecta).toFixed(2));
        const percepcionValue = Number(this.form.get('percepcion')?.value) || 0;
        const percepcionMonto = Number((totalCalculado * (percepcionValue / 100)).toFixed(2));
        const ajuste = Number(this.form.get('ajusteRedondeo')?.value) || 0;
        const total = Number((totalCalculado + percepcionMonto + ajuste).toFixed(2));

        this.totals.set({ subtotal, gravada, exonerada, inafecta, igv, totalCalculado, total });

        this.form.patchValue({
            valorVentaGravado: gravada,
            valorVentaExonerado: exonerada,
            valorVentaInafecto: inafecta,
            igv: igv,
            total: totalCalculado
        }, { emitEvent: false });
    }

    cargarProveedores() {
        this.proveedorService.listarTodos(0, 1000).subscribe(res => {
            if (res.success && res.data) this.proveedores.set(res.data.content);
        });
    }

    cargarProductos() {
        this.catalogoService.listarTodos(0, 1000, this.selectedFilter()).subscribe(res => {
            console.log(res);
            if (res.success && res.data) this.productos.set(res.data.content);
        });
    }

    onProductoSelected(producto: any, cantidad: number = 1) {
        if (producto) {
            const prodId = Number(producto.id);
            this.productDataMap.update(map => {
                map.set(prodId, producto);
                return new Map(map);
            });
            this.agregarDetalle(producto, cantidad);
            this.searchTermProducto.set('');
            this.showDropdownProducto.set(false);
        }
    }

    onSolicitarPago() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();

            const faltantes: string[] = [];
            const controls = this.form.controls;

            if (controls['idProveedor'].invalid) faltantes.push('Seleccione un Proveedor');
            if (controls['tipoComprobante'].invalid) faltantes.push('Tipo de Comprobante es requerido');
            if (controls['serie'].invalid) {
                const errors = controls['serie'].errors;
                if (errors?.['required']) faltantes.push('La Serie es obligatoria');
                if (errors?.['maxlength']) faltantes.push('La Serie no puede exceder los 10 caracteres');
            }
            if (controls['correlativo'].invalid) {
                const errors = controls['correlativo'].errors;
                if (errors?.['required']) faltantes.push('El Correlativo es obligatorio');
                if (errors?.['maxlength']) faltantes.push('El Correlativo no puede exceder los 20 caracteres');
            }
            if (controls['fechaEmision'].invalid) faltantes.push('Fecha de Emisión es requerida');

            if (this.detalles.length === 0) {
                faltantes.push('Debe agregar al menos un producto');
            } else {
                this.detalles.controls.forEach((ctrl, index) => {
                    if (ctrl.invalid) {
                        const row = ctrl as FormGroup;
                        const prodName = row.get('nombreProducto')?.value || `Item ${index + 1}`;
                        const problemas: string[] = [];

                        if (row.get('cantidad')?.invalid) problemas.push('Cantidad');
                        if (row.get('precioUnitario')?.invalid) problemas.push('Precio');
                        if (row.get('tipoAfectacion')?.invalid) problemas.push('Afectación');
                        if (row.get('idUnidadMedida')?.invalid) problemas.push('Unidad');

                        if (problemas.length > 0) {
                            faltantes.push(`🔴 ${prodName}: Falta ${problemas.join(', ')}`);
                        } else {
                            faltantes.push(`🔴 ${prodName}: Datos inválidos`);
                        }
                    }
                });
            }

            if (faltantes.length > 0) {
                const htmlList = `<ul class="text-left mt-4 text-sm space-y-1 list-disc pl-5">${faltantes.map(f => `<li>${f}</li>`).join('')}</ul>`;

                this.alertService.custom({
                    title: 'Formulario Incompleto',
                    html: `Se encontraron los siguientes errores:${htmlList}`,
                    icon: 'warning',
                    confirmButtonText: 'Entendido'
                });
            }
            return;
        }

        const saldoFavor = (this.datosProveedor()?.saldo < 0 ? Math.abs(this.datosProveedor().saldo) : 0);
        const usarSaldo = (saldoFavor > 0 && this.opcionPagoSeleccionada() === 'SALDO');

        const processRequest = () => {
            if (usarSaldo) {
                this.ejecutarSolicitudPago(true, saldoFavor);
            } else {
                this.ejecutarSolicitudPago(false, 0);
            }
        };

        this.loading.set(true);
        const formValue = this.form.getRawValue();

        // Inject Sucursal ID from Token
        const idSucursal = this.authService.getSucursalIdFromToken();
        if (!idSucursal) {
            this.alertService.error('Error de Sesión', 'No se pudo identificar la sucursal actual. Por favor, inicie sesión nuevamente.');
            this.loading.set(false);
            return;
        }

        const req = {
            ...formValue,
            idSucursal
        };

        if (!this.idCompra) {
            this.compraService.registrar(req).subscribe({
                next: (res: any) => {
                    if (res.success && res.data?.id) {
                        this.idCompra = res.data.id;
                        processRequest();
                    } else {
                        this.loading.set(false);
                        this.alertService.error('Error', 'No se pudo guardar la compra para solicitar el pago.');
                    }
                },
                error: (err: any) => {
                    this.loading.set(false);
                    const msg = err.error?.message || 'Hubo un problema al guardar la compra.';
                    this.alertService.error('Error', msg);
                }
            });
        } else {
            this.compraService.actualizar(this.idCompra, req).subscribe({
                next: (res: any) => {
                    if (res.success) {
                        processRequest();
                    } else {
                        this.loading.set(false);
                        this.alertService.error('Error', 'No se pudo actualizar la compra.');
                    }
                },
                error: (err: any) => {
                    this.loading.set(false);
                    const msg = err.error?.message || 'Hubo un problema al actualizar la compra.';
                    this.alertService.error('Error', msg);
                }
            });
        }
    }

    private ejecutarSolicitudPago(usarSaldo: boolean, montoUsarSaldo: number) {
        this.loading.set(true);
        this.cronogramaPagoService.solicitarPagoCompra(this.idCompra!, { usarSaldoFavor: usarSaldo, montoUsarSaldo: montoUsarSaldo }).subscribe({
            next: (res) => {
                if (res.success) {
                    this.pagoSolicitado.set(true);
                    if (res.data && res.data.estado === 'PAGADO') {
                        this.pagoConfirmado.set(true);
                        // Cuando es pagado en su totalidad, dejamos que WS notifique (o mostramos la local, pero para ser consistentes, lo removemos también o lo dejamos si no hay WS para esto)
                    } else {
                        // Se omite la alerta local para dejar que el WebSocket (header) muestre la notificación
                    }
                    this.guardado.emit();
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    simularDatos() {
        // Datos de Cabecera Simuados
        this.form.patchValue({
            tipoComprobante: 'FACTURA',
            serie: 'F100',
            correlativo: '000001',
            fechaEmision: new Date().toISOString().split('T')[0],
            condicionPago: 'CONTADO',
            moneda: 'PEN',
            percepcion: 0
        });

        // Seleccionar primer proveedor si existe
        if (this.proveedores().length > 0) {
            this.form.patchValue({ idProveedor: this.proveedores()[0].id });
        }

        // Limpiar detalles actuales
        while (this.detalles.length !== 0) {
            this.detalles.removeAt(0);
        }
        this.rowUnitOptions.set([]);

        // Escenarios de Jerarquía
        const escenarios = [
            {
                // Escenario 1: Nivel Menor (Solo Unidad)
                producto: {
                    id: 7,
                    nombre: 'PARACETAMOL 500MG (NIVEL MENOR)',
                    codigo: 'MED-001',
                    manejaUnidad: true, idUnidadBase: 1, // Unidad
                    manejaBlister: false,
                    manejaCaja: false,
                    tipoAfectacion: 'GRAVADO'
                },
                cantidad: 100,
                precio: 10.00
            },
            {
                // Escenario 2: Nivel Intermedio (Unidad y Blister)
                producto: {
                    id: 8,
                    nombre: 'AMOXICILINA 500MG (NIVEL INTERMEDIO)',
                    codigo: 'MED-002',
                    manejaUnidad: true, idUnidadBase: 1, // Unidad
                    manejaBlister: true, idUnidadIntermedia: 2, factorBlister: 10, // Blister
                    manejaCaja: false,
                    tipoAfectacion: 'GRAVADO'
                },
                cantidad: 20, // 20 Blisters
                precio: 25.00
            },
            {
                // Escenario 3: Nivel Mayor (Completo: Unidad, Blister, Caja)
                producto: {
                    id: 9,
                    nombre: 'IBUPROFENO 400MG (NIVEL MAYOR)',
                    codigo: 'MED-003',
                    manejaUnidad: true, idUnidadBase: 1, // Unidad
                    manejaBlister: true, idUnidadIntermedia: 2, factorBlister: 10,
                    manejaCaja: true, idUnidadMayor: 3, factorCaja: 100, // Caja
                    tipoAfectacion: 'GRAVADO'
                },
                cantidad: 5, // 5 Cajas
                precio: 150.00
            }
        ];

        escenarios.forEach(item => {
            // Agregar al detalle simulando selección
            this.agregarDetalle(item.producto, item.cantidad);

            const lastIndex = this.detalles.length - 1;
            const detalleControl = this.detalles.at(lastIndex);

            // Patch valores adicionales
            detalleControl.patchValue({
                precioUnitario: item.precio,
                lote: 'LOTE-TEST',
                fechaVencimiento: new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0],
                esBonificacion: false
            });
        });

        this.calculateTotals();
        this.alertService.info('Simulación', 'Datos de prueba de jerarquías cargados');
    }

    simularDatosOtidol() {
        // Datos de Cabecera Diferente para distinguir
        this.form.patchValue({
            tipoComprobante: 'FACTURA',
            serie: 'F002', // Serie diferente
            correlativo: '001234',
            fechaEmision: new Date().toISOString().split('T')[0],
            condicionPago: 'CONTADO',
            moneda: 'PEN',
            percepcion: 0
        });

        // Limpiar detalles actuales
        while (this.detalles.length !== 0) {
            this.detalles.removeAt(0);
        }
        this.rowUnitOptions.set([]); // Limpiar opciones

        const items = [
            { nombre: 'OTIDOL NF', cantidad: 6, precio: 58.60, d2: 86.77, presentacion: 'FCO - CS' },
            { nombre: 'PATADINE PLUS', cantidad: 3, precio: 127.00, d2: 74.70, presentacion: 'FCO - CS' },
            { nombre: 'TERRACORSOL', cantidad: 6, precio: 26.60, d2: 48.93, presentacion: 'TBO - CS' },
            { nombre: 'TERRAMISOL-A', cantidad: 6, precio: 38.40, d2: 100.0, boni: true, presentacion: 'TBO - CS' }, // Bonificado
            { nombre: 'TERRAMISOL-A', cantidad: 21, precio: 38.40, d2: 70.38, presentacion: 'TBO - CS' },
            { nombre: 'TOBRAZOL DX', cantidad: 2, precio: 143.00, d2: 100.0, boni: true, presentacion: 'FCO - CS' }, // Bonificado
            { nombre: 'TOBRAZOL DX', cantidad: 2, precio: 143.00, d2: 77.63, presentacion: 'FCO - CS' },
            { nombre: 'TOBRAZOL DX', cantidad: 2, precio: 143.00, d2: 77.63, presentacion: 'FCO - CS' }, // Repetido en imagen
            { nombre: 'TOBRAZOL SOL', cantidad: 4, precio: 99.00, d2: 70.51, presentacion: 'FCO - CS' },
            { nombre: 'GENTAMICINA 0.3%', cantidad: 6, precio: 19.80, d2: 83.48, presentacion: 'CJA - BX' },
            { nombre: 'PREDNISOLONA 1%', cantidad: 4, precio: 58.00, d2: 63.53, presentacion: 'FCO - CS' },
            { nombre: 'HYALO COMFORT', cantidad: 5, precio: 152.90, d2: 84.69, presentacion: 'FCO - CS' },
            { nombre: 'BRINZOLAN T', cantidad: 2, precio: 209.00, d2: 62.09, presentacion: 'FCO - CS' },
            { nombre: 'XALOPTIC SR', cantidad: 3, precio: 180.91, d2: 68.07, presentacion: 'CJA - BX' },
            { nombre: 'MEGATOB NF', cantidad: 3, precio: 144.07, d2: 59.16, presentacion: 'CJA - BX' }
        ];

        items.forEach(item => {
            // Buscar producto por coincidencia de nombre
            const producto = this.productos().find(p => p.nombre.toUpperCase().includes(item.nombre));

            // Usar lógica existente: producto encontrado o fallback con datos básicos
            this.agregarDetalle(producto || { nombre: item.nombre, codigo: 'SIM-NVO', presentacion: item.presentacion }, item.cantidad);

            const lastIndex = this.detalles.length - 1;
            const detalleControl = this.detalles.at(lastIndex);

            detalleControl.patchValue({
                precioUnitario: item.precio,
                porcentajeDescuento2: item.d2,
                lote: '2025-001', // Lote genérico para esta simulación
                fechaVencimiento: '2026-12-31',
                esBonificacion: item.boni || false
            });
        });

        this.calculateTotals();
        this.alertService.info('Simulación', 'Datos de Compra OTIDOL cargados correctamente');
    }

    cancelar() {
        this.cerrar.emit();
    }
}
