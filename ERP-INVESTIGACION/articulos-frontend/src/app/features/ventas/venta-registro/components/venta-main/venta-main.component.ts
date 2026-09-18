import { Component, signal, Input, Output, EventEmitter, inject, OnInit, ChangeDetectorRef, NgZone, OnChanges, SimpleChanges, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { debounceTime, Subject, switchMap, of, catchError, forkJoin } from 'rxjs';
import { UserResponse, UserService } from '../../../../../core/services/user.service';
import { Producto } from '../../../../almacen/models/producto.model';
import { ProductoService } from '../../../../almacen/service/producto.service';
import { AuthService } from '../../../../auth/services/auth.service';
import { AlertService } from '../../../../../core/services/alert.service';
import { VentaRegistroService } from '../../../services/venta-registro.service';
import { DocumentoService } from '../../../../documentos/services/documento.service';
import { DocumentoImpresionService } from '../../../../documentos/services/documento-impresion.service';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CajaChicaService } from '../../../../../core/services/caja-chica.service';
import { MetodoPagoService } from '../../../../almacen/service/atributo.service';
import { CajaAperturaModalComponent } from '../caja-apertura-modal/caja-apertura-modal.component';
import { PuntoDocumentoService } from '../../../../documentos/services/punto-documento.service';
import { DescuentoService } from '../../../descuentos/services/descuento.service';

interface ItemVenta {
    producto: Producto;
    cantidad: number;
    unidadSeleccionada: 'UNIDAD' | 'BLISTER' | 'CAJA';
    precioUnitario: number; // Base neta
    baseImp: number;
    igv: number;
    valorExo: number;
    valorInaf: number;
    total: number;
    descuentosSugeridos: any[];
    descuentoSeleccionado: any | null;
}

@Component({
    selector: 'app-venta-main',
    standalone: true,
    imports: [CommonModule, FormsModule, CajaAperturaModalComponent, ModalComponent],
    templateUrl: './venta-main.component.html',
    styles: [`
        .custom-select {
            appearance: none;
            -webkit-appearance: none;
            -moz-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 0.5rem center;
            background-size: 0.75rem;
            padding-right: 1.5rem !important;
        }
        
        :host-context(.dark) .custom-select {
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
        }

        .quantity-input::-webkit-outer-spin-button,
        .quantity-input::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }
        .quantity-input {
            -moz-appearance: textfield;
        }
    `]
})
export class VentaMainComponent implements OnInit, OnChanges {
    @Input() selectedPatient: UserResponse | null = null;
    @Output() onPatientLoaded = new EventEmitter<UserResponse>();

    private productoService = inject(ProductoService);
    private authService = inject(AuthService);
    private ventaRegistroService = inject(VentaRegistroService);
    private generalDescuentoService = inject(DescuentoService);
    private alertService = inject(AlertService);
    private documentoService = inject(DocumentoService);
    private documentoImpresionService = inject(DocumentoImpresionService);
    private cajaChicaService = inject(CajaChicaService);
    private metodoPagoService = inject(MetodoPagoService);
    private sanitizer = inject(DomSanitizer);
    private cdr = inject(ChangeDetectorRef);
    private zone = inject(NgZone);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    private route = inject(ActivatedRoute);
    private userService = inject(UserService); // Asegurarse de importar UserService

    searchTerm = '';
    private searchSubject = new Subject<string>();
    searchResults: Producto[] = [];
    cargandoSearch = false;
    busquedaRealizada = false;
    selectedIndex = -1;

    carrito: ItemVenta[] = [];
    descuentosActivos: any[] = [];

    showModalDescuento = false;
    showModalCaja = false;
    private estadoRegistroPendiente: 'VENTA' | 'COTIZACION' | null = null;

    // Impresión Post-Venta
    showModalImpresion = false;
    pdfUrl: SafeResourceUrl | null = null;
    cargandoPdf = false;

    subTotal = 0; // Se mostrará como Base Imponible
    igv = 0;
    valorExo = 0;
    valorInaf = 0;
    total = 0;
    descuentoTotal = 0;
    descuentosDetalle: { nombre: string, ahorro: number, colorClass?: string }[] = [];
    pagoCon: number | null = null;

    get vuelto(): number {
        if (!this.pagoCon || this.pagoCon <= this.total) return 0;
        return this.pagoCon - this.total;
    }

    get falta(): number {
        if (!this.pagoCon || this.pagoCon >= this.total) return 0;
        return this.total - this.pagoCon;
    }

    itemActivo: ItemVenta | null = null;
    tipoDocumentoSeleccionado = '03'; // 03=Boleta por defecto

    // Nuevos campos
    metodosPago: string[] = [];
    metodoPagoSeleccionado = 'EFECTIVO';
    plantillas: any[] = [];
    plantillaSeleccionadaId: number | null = null;
    tienePuntoVentaAsignado = false;

    // Post-Venta
    showModalExito = false;
    ventaReciente: any = null;
    todasLasPlantillasVenta: any[] = [];
    today = new Date();

    // Campos para compartir
    emailDestino = '';
    telefonoDestino = '';

    canjeNotaVentaId: number | null = null;

    ngOnInit() {
        this.setupSearch();
        this.cargarPlantillas();
        this.cargarMetodosPago();
        this.verificarPuntoDocumento();

        this.route.queryParams.subscribe(params => {
            const cotizacionId = params['cotizacionId'];
            const canjearId = params['canjearId'];
            if (cotizacionId) {
                this.cargarCotizacion(Number(cotizacionId));
            } else if (canjearId) {
                this.canjeNotaVentaId = Number(canjearId);
                this.cargarCotizacion(this.canjeNotaVentaId); // Reuse the same loading logic
            }
        });
    }

    cargarCotizacion(id: number) {
        this.ventaRegistroService.obtenerPorId(id).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const cotizacion = res.data;

                    // Cargar el paciente
                    if (cotizacion.idPersonal) {
                        this.ventaRegistroService.obtenerPaciente(cotizacion.idPersonal).subscribe(uRes => {
                            if (uRes && uRes.success && uRes.data) {
                                this.onPatientLoaded.emit(uRes.data);
                            }
                        });
                    }

                    if (cotizacion.detalles && cotizacion.detalles.length > 0) {
                        const observables = cotizacion.detalles.map((det: any) => {
                            return this.productoService.obtenerPorId(det.idArticulo || det.idCatalogo);
                        });

                        forkJoin(observables).subscribe((results: any) => {
                            this.carrito = [];
                            results.forEach((prodRes: any, index: number) => {
                                if (prodRes.success && prodRes.data) {
                                    const producto = prodRes.data;
                                    const det = cotizacion.detalles[index];
                                    const nuevoItem: ItemVenta = {
                                        producto: producto,
                                        cantidad: det.cantidad,
                                        unidadSeleccionada: det.unidadMedida || 'UNIDAD',
                                        precioUnitario: det.precioUnitario,
                                        total: det.total,
                                        baseImp: det.baseImp || 0,
                                        igv: det.igv || 0,
                                        valorExo: det.valorExo || 0,
                                        valorInaf: det.valorInaf || 0,
                                        descuentosSugeridos: [],
                                        descuentoSeleccionado: null
                                    };
                                    this.carrito.push(nuevoItem);
                                    this.recalcularPrecioItem(nuevoItem);
                                }
                            });
                            this.calcularTotales();
                            this.alertService.toast('Cotización cargada exitosamente', 'success');
                        });
                    }
                }
            }
        });
    }

    verificarPuntoDocumento() {
        const puntoId = this.authService.getPuntoIdFromToken();
        if (puntoId) {
            this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
                next: (res) => {
                    if (res.success && res.data) {
                        const tieneVenta = res.data.some((pd: any) => {
                            const isVenta = pd.modulo === 'VENTA';
                            // El estado puede venir como string, number u objeto {valor, name}
                            const isActive = pd.estado === 'ACTIVO' || pd.estado === 1 || pd.estado === 'A' ||
                                (pd.estado && (pd.estado.valor === 1 || pd.estado.name === 'ACTIVO'));
                            return isVenta && isActive;
                        });
                        if (!tieneVenta) {
                            this.alertService.warning('Este punto de emisión no tiene configurado documentos de VENTA (Boleta/Factura). Por favor asigne uno en Configuración.');
                            this.tienePuntoVentaAsignado = false;
                        } else {
                            this.tienePuntoVentaAsignado = true;
                        }
                    } else {
                        this.tienePuntoVentaAsignado = false;
                    }
                },
                error: () => this.tienePuntoVentaAsignado = false
            });
        }
    }

    cargarMetodosPago() {
        this.metodoPagoService.listarActivos().subscribe({
            next: (res) => {
                if (res.success && res.data?.content) {
                    this.metodosPago = res.data.content.map(m => m.descripcion.toUpperCase());
                    // Si el seleccionado no está en la lista nueva, volver a EFECTIVO o al primero
                    if (this.metodosPago.length > 0 && !this.metodosPago.includes(this.metodoPagoSeleccionado)) {
                        this.metodoPagoSeleccionado = this.metodosPago[0];
                    }
                }
            }
        });
    }

    cargarPlantillas() {
        const puntoId = this.authService.getPuntoIdFromToken();
        if (!puntoId) return;
        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Filtrar solo los documentos activos y del módulo VENTA (excluyendo notas de crédito '07' y débito '08')
                    const docsVenta = res.data.filter((pd: any) => {
                        const isVenta = pd.modulo === 'VENTA';
                        const isActive = pd.estado === 'ACTIVO' || pd.estado === 1 || pd.estado === 'A' ||
                            (pd.estado && (pd.estado.valor === 1 || pd.estado.name === 'ACTIVO'));
                        const isNotAdjustment = pd.tipoDoc !== '07' && pd.tipoDoc !== '08';
                        return isVenta && isActive && pd.idPlantilla && isNotAdjustment;
                    });

                    this.plantillas = docsVenta.map((pd: any) => ({
                        id: pd.idPlantilla,
                        nombre: pd.plantillaNombre || `Plantilla ${pd.idPlantilla}`,
                        tipoDocumento: { nombre: pd.tipoDocumentoNombre || pd.tipoDoc },
                        tipoDoc: pd.tipoDoc
                    }));

                    this.detectarDocumentoBasePaciente();
                }
            }
        });

        // Cargar TODAS las plantillas de VENTA para el modal post-venta
        this.documentoService.listarPlantillas('VENTA').subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.todasLasPlantillasVenta = res.data;
                }
            }
        });
    }

    onPlantillaChange() {
        if (this.plantillaSeleccionadaId) {
            const id = Number(this.plantillaSeleccionadaId);
            const plantilla = this.plantillas.find(p => p.id === id);
            if (plantilla && plantilla.tipoDoc) {
                this.tipoDocumentoSeleccionado = plantilla.tipoDoc;
            }
        }
    }

    getNombreComprobante(): string {
        if (!this.plantillaSeleccionadaId) return 'Vender';
        const id = Number(this.plantillaSeleccionadaId);
        const plantilla = this.plantillas.find(p => p.id === id);
        if (plantilla) {
            let nombre = plantilla.tipoDocumento?.nombre || plantilla.nombre || 'Venta';
            // Capitalize first letter of each word
            nombre = nombre.toLowerCase().replace(/\b\w/g, (c: string) => c.toUpperCase());
            return `Generar ${nombre}`;
        }
        return 'Vender';
    }

    private detectarDocumentoBasePaciente() {
        if (this.plantillas.length === 0) return;

        let plantilla;

        if (this.selectedPatient) {
            const numdoc = this.selectedPatient.numdoc || '';
            const ruc = this.selectedPatient.ruc || '';

            // Si tiene RUC (explícito) o numdoc de 11 dígitos, es Factura ('01')
            const esRuc = ruc.length === 11 || numdoc.length === 11;
            
            if (esRuc) {
                plantilla = this.plantillas.find(p => p.tipoDoc === '01');
            }
        }

        // Por defecto, intentar buscar Ticket siempre que no haya exigido Factura
        if (!plantilla) {
            plantilla = this.plantillas.find(p => p.nombre.toLowerCase().includes('ticket') || (p.tipoDocumento && p.tipoDocumento.nombre.toLowerCase().includes('ticket')));
        }
        
        // Si no hay ticket, buscar Boleta ('03')
        if (!plantilla) {
            plantilla = this.plantillas.find(p => p.tipoDoc === '03');
        }

        // Si no hay boleta, buscar Nota de Venta
        if (!plantilla) {
            plantilla = this.plantillas.find(p => p.nombre.toLowerCase().includes('nota') || p.tipoDoc === '12');
        }

        // Si todavía no hay nada, el primero disponible
        if (!plantilla && this.plantillas.length > 0) {
            plantilla = this.plantillas[0];
        }

        if (plantilla) {
            this.plantillaSeleccionadaId = plantilla.id;
            this.onPlantillaChange();
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['selectedPatient'] && !changes['selectedPatient'].firstChange) {
            if (this.selectedPatient) {
                this.generalDescuentoService.listarAplicables(this.selectedPatient.id).subscribe({
                    next: (res) => {
                        if (res.success && res.data) {
                            this.descuentosActivos = res.data.content || res.data;
                            console.log('✅ Descuentos aplicables devueltos por el backend:', this.descuentosActivos);
                            this.verificarDescuentosCarrito();
                        }
                    },
                    error: (err) => {
                        console.error('Error cargando descuentos aplicables:', err);
                        this.descuentosActivos = [];
                    }
                });
            } else {
                this.descuentosActivos = [];
                this.verificarDescuentosCarrito();
            }
            this.detectarDocumentoBasePaciente();
        }
    }

    private setupSearch() {
        this.searchSubject.pipe(
            debounceTime(10),
            switchMap(term => {
                const cleanTerm = term.trim();
                // Si el término es vacío, igual buscamos para mostrar los primeros productos
                this.cargandoSearch = true;
                const idSucursal = this.authService.getSucursalIdFromToken() || 1;
                return this.productoService.buscar(term, 0, 10, idSucursal, undefined, true).pipe(
                    catchError(err => {
                        console.error('Error en búsqueda:', err);
                        this.zone.run(() => {
                            this.cargandoSearch = false;
                            this.searchResults = [];
                        });
                        return of({ data: { content: [] } as any });
                    })
                );
            })
        ).subscribe(res => {
            this.zone.run(() => {
                const content = res.data?.content || [];

                // Filtrar productos con stock > 0 y ordenar por vencimiento
                this.searchResults = content
                    .filter((p: any) => (p.stockKardex ?? p.stock ?? 0) > 0)
                    .sort((a: any, b: any) => {
                        const dateA = a.fechaVencimiento ? new Date(a.fechaVencimiento).getTime() : Infinity;
                        const dateB = b.fechaVencimiento ? new Date(b.fechaVencimiento).getTime() : Infinity;
                        return dateA - dateB;
                    });

                this.cargandoSearch = false;
                this.selectedIndex = -1;
                this.cdr.detectChanges();
            });
        });
    }

    onSearchFocus() {
        this.busquedaRealizada = true;
        // Disparar búsqueda al ganar el foco si no hay resultados mostrados
        if (this.searchResults.length === 0 || !this.searchTerm) {
            this.cargandoSearch = true; // Forzar estado de carga visual
            this.searchSubject.next(this.searchTerm || '');
        }
    }

    onSearchBlur() {
        // Retrasamos el cierre para permitir que el click en el resultado se registre
        setTimeout(() => {
            // Solo cerramos si no hay un término que el usuario esté editando o si ya seleccionó
            if (!this.searchTerm) {
                this.busquedaRealizada = false;
            }
        }, 300);
    }

    @HostListener('document:mousedown', ['$event'])
    onDocumentClick(event: MouseEvent) {
        const target = event.target as HTMLElement;

        // Cerrar resultados de búsqueda si se hace clic fuera
        const isOutsideSearch = !target.closest('.search-area-container');
        if (isOutsideSearch && this.busquedaRealizada) {
            this.busquedaRealizada = false;
        }

        if (!this.itemActivo) return;

        const isOutsidePanel = !target.closest('.panel-descuentos');
        const isOutsideTable = !target.closest('.tabla-articulos');

        // Si el clic es fuera de ambos, cerramos panel
        if (isOutsidePanel && isOutsideTable) {
            this.cerrarDescuentos();
        }
    }

    cerrarDescuentos() {
        this.itemActivo = null;
    }

    onSearchChange() {
        this.busquedaRealizada = true;
        this.cargandoSearch = true; // Activar feedback instantáneo
        this.searchSubject.next(this.searchTerm);
    }

    onSearchKeyDown(event: KeyboardEvent) {
        if (!this.busquedaRealizada || this.searchResults.length === 0) return;

        if (event.key === 'ArrowDown') {
            event.preventDefault();
            if (this.selectedIndex < this.searchResults.length - 1) {
                this.selectedIndex++;
                this.scrollToSelected();
            }
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            if (this.selectedIndex > 0) {
                this.selectedIndex--;
                this.scrollToSelected();
            }
        } else if (event.key === 'Enter') {
            event.preventDefault();
            if (this.selectedIndex >= 0 && this.selectedIndex < this.searchResults.length) {
                this.agregarAlCarrito(this.searchResults[this.selectedIndex]);
            } else if (this.searchResults.length === 1) {
                this.agregarAlCarrito(this.searchResults[0]);
            }
        }
    }

    scrollToSelected() {
        setTimeout(() => {
            const selectedItem = document.querySelector('.search-item-selected');
            if (selectedItem) {
                (selectedItem as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        });
    }

    seleccionarItem(item: ItemVenta | null) {
        this.itemActivo = (this.itemActivo === item) ? null : item;
    }

    ajustarCantidad(item: ItemVenta, delta: number) {
        const nuevaCantidad = item.cantidad + delta;
        if (nuevaCantidad >= 1) {
            item.cantidad = nuevaCantidad;
            this.actualizarFila(item);
        }
    }



    seleccionarDescuento(item: ItemVenta, descuento: any) {
        if (item.descuentoSeleccionado?.id === descuento.id) {
            item.descuentoSeleccionado = null;
        } else {
            const det = descuento.matchingDetail;
            if (det.tipoDescuento === 'CANTIDAD' && item.cantidad < det.cantidad) {
                this.alertService.warning(`Este descuento requiere al menos ${det.cantidad} unidades.`);
                return;
            }
            item.descuentoSeleccionado = descuento;
        }
        this.actualizarFila(item);
    }


    agregarAlCarrito(producto: Producto) {
        const stockActual = producto.stockKardex ?? producto.stock ?? 0;
        if (stockActual <= 0) {
            this.alertService.warning('Este producto no tiene stock disponible.');
            return;
        }
        const idProd = producto.idProducto || producto.id;

        // Validar si queda stock acumulado antes de permitir agregar otra fila
        let unidadesAcumuladas = 0;
        for (const fila of this.carrito) {
            if ((fila.producto.idProducto || fila.producto.id) === idProd) {
                let factor = 1;
                if (fila.unidadSeleccionada === 'BLISTER' && fila.producto.factorBlister) {
                    factor = fila.producto.factorBlister;
                } else if (fila.unidadSeleccionada === 'CAJA' && fila.producto.factorCaja) {
                    factor = fila.producto.factorCaja;
                    if (fila.producto.manejaBlister && fila.producto.factorBlister) {
                        factor *= fila.producto.factorBlister;
                    }
                }
                unidadesAcumuladas += fila.cantidad * factor;
            }
        }

        if (stockActual - unidadesAcumuladas <= 0) {
            this.alertService.warning('No hay más stock disponible para seguir agregando este producto.');
            return;
        }

        const itemExistente = this.carrito.find(item => (item.producto.idProducto || item.producto.id) === idProd && item.unidadSeleccionada === 'UNIDAD');

        if (itemExistente) {
            itemExistente.cantidad++;
            this.actualizarFila(itemExistente);
        } else {
            const nuevoItem: ItemVenta = {
                producto: producto,
                cantidad: 1,
                unidadSeleccionada: 'UNIDAD',
                precioUnitario: 0,
                total: 0,
                baseImp: 0,
                igv: 0,
                valorExo: 0,
                valorInaf: 0,
                descuentosSugeridos: [],
                descuentoSeleccionado: null
            };
            this.carrito.push(nuevoItem);
            this.recalcularPrecioItem(nuevoItem);
            this.actualizarFila(nuevoItem);
            this.itemActivo = nuevoItem;
        }

        this.searchTerm = '';
        this.searchResults = [];
        this.busquedaRealizada = false;
        this.selectedIndex = -1;
        this.calcularTotales();
    }

    recalcularPrecioItem(item: ItemVenta) {
        const p = item.producto;
        let precioBase = p.precioVentaUnitario || 0;

        if (item.unidadSeleccionada === 'BLISTER') {
            precioBase = p.precioVentaBlister || (precioBase * (p.factorBlister || 1));
        } else if (item.unidadSeleccionada === 'CAJA') {
            let factorRealCaja = p.factorCaja || 1;
            if (p.manejaBlister && p.factorBlister && p.factorBlister > 0) {
                factorRealCaja = factorRealCaja * p.factorBlister;
            }
            precioBase = p.precioVentaCaja || (precioBase * factorRealCaja);
        }

        item.precioUnitario = precioBase;

        this.verificarDescuentoItem(item);
        this.calcularTotales();
    }

    verificarDescuentosCarrito() {
        this.carrito.forEach(item => this.verificarDescuentoItem(item, item.producto.idCatalogo || item.producto.id, false));
        this.calcularTotales();
    }

    private verificarDescuentoItem(item: ItemVenta, idCat?: number, calcTotales: boolean = true) {
        if (!idCat) idCat = item.producto.idCatalogo || item.producto.id;
        if (!idCat) return;

        // 1. Buscamos descuentos generales (promociones)
        const generales = this.buscarDescuentosGenerales(idCat, item.producto.precioVentaUnitario || 0);

        item.descuentosSugeridos = [...generales];

        // Validar si el descuento seleccionado todavía aplica
        if (item.descuentoSeleccionado) {
            const todaviaAplica = item.descuentosSugeridos.find(d => d.id === item.descuentoSeleccionado.id);
            if (!todaviaAplica) {
                item.descuentoSeleccionado = null;
            } else {
                // Mantener el objeto actualizado con su matchingDetail
                item.descuentoSeleccionado = todaviaAplica;
            }
        }

        this.actualizarFila(item, calcTotales);
    }

    buscarDescuentosGenerales(idCatalogo: number, precioProducto: number = 0) {
        const sugeridos: any[] = [];

        this.descuentosActivos.forEach(d => {
            // 1. Verificar Alcance en Detalles (Maestro-Detalle)
            if (!d.detalles || d.detalles.length === 0) return;

            const detalleCoincidente = d.detalles.find((det: any) =>
                d.tipoAlcance === 'GLOBAL' ||
                det.idCatalogo === idCatalogo ||
                det.idProducto === idCatalogo
            );

            if (detalleCoincidente) {
                // IMPORTANTE: Retornamos un clon para que el matchingDetail sea único por ÍTEM del carrito
                sugeridos.push({
                    ...d,
                    matchingDetail: { ...detalleCoincidente }
                });
            }
        });

        return sugeridos;
    }

    getDescuentoColorClass(d: any): string {
        if (!d.usuariosAfectados || d.usuariosAfectados === 'ALL') {
            return 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300 border-blue-200 dark:border-blue-800';
        }

        // Si tiene usuarios específicos, es un precio especial/convenio
        return 'bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-300 border-purple-200 dark:border-purple-800';
    }

    formatearStock(item: ItemVenta): string {
        return this.formatearStockProducto(item.producto, item.unidadSeleccionada);
    }

    getAbrevUnidad(p: Producto, tipo: 'UNIDAD'|'BLISTER'|'CAJA'): string {
        const cat: any = p.catalogo;
        let nombre = '';
        if (tipo === 'UNIDAD') nombre = cat?.unidadBase || 'UNIDAD';
        else if (tipo === 'BLISTER') nombre = cat?.unidadIntermedia || 'BLISTER';
        else if (tipo === 'CAJA') nombre = cat?.unidadMayor || 'CAJA';

        const upper = (nombre || '').toUpperCase();
        if (upper === 'UNIDAD') return 'UND';
        if (upper === 'BLISTER') return 'BLI';
        if (upper === 'CAJA') return 'CAJ';
        
        return upper ? upper.substring(0, 3) : '';
    }

    formatearStockProducto(p: Producto, unidad?: string): string {
        const stockActual = p.stockKardex !== undefined && p.stockKardex !== null ? p.stockKardex : (p.stock || 0);
        const abrevUnd = this.getAbrevUnidad(p, 'UNIDAD');
        if (stockActual <= 0) return '0 ' + abrevUnd;

        if (unidad === 'BLISTER' && p.factorBlister && p.factorBlister > 0) {
            return `${(stockActual / p.factorBlister).toFixed(1)} ${this.getAbrevUnidad(p, 'BLISTER')}`;
        }

        if (unidad === 'CAJA' && p.factorCaja && p.factorCaja > 0) {
            let factorRealCaja = p.factorCaja;
            if (p.manejaBlister && p.factorBlister && p.factorBlister > 0) {
                factorRealCaja = factorRealCaja * p.factorBlister;
            }
            return `${(stockActual / factorRealCaja).toFixed(1)} ${this.getAbrevUnidad(p, 'CAJA')}`;
        }

        // Si no se especifica unidad, mostrar resumen (para la búsqueda)
        if (!unidad) {
            const parts: string[] = [`${stockActual} ${abrevUnd}`];
            if (p.manejaBlister && p.factorBlister) parts.push(`${(stockActual / p.factorBlister).toFixed(1)} ${this.getAbrevUnidad(p, 'BLISTER')}`);
            if (p.manejaCaja && p.factorCaja) {
                let factorRealCaja = p.factorCaja;
                if (p.manejaBlister && p.factorBlister && p.factorBlister > 0) {
                    factorRealCaja = factorRealCaja * p.factorBlister;
                }
                parts.push(`${(stockActual / factorRealCaja).toFixed(1)} ${this.getAbrevUnidad(p, 'CAJA')}`);
            }
            return parts.join(' • ');
        }

        return `${stockActual} ${abrevUnd}`;
    }

    getProfitLabel(p: Producto): string {
        const cost = p.precioCompra || 0;
        const sale = p.precioVentaUnitario || 0;

        if (cost > 0 && sale > 0) {
            // Margen sobre el Precio de Venta: (Venta - Costo) / Venta
            const margin = ((sale - cost) / sale) * 100;
            return `${Math.round(margin)}%`;
        }

        if (p.gananciaUnidad === undefined || p.gananciaUnidad === null) return '';
        if (p.tipoGananciaUnidad === 'PORCENTAJE') {
            return `${p.gananciaUnidad}%`;
        }
        return `S/ ${Number(p.gananciaUnidad).toFixed(2)}`;
    }

    obtenerStockMaximo(item: ItemVenta): number {
        const p = item.producto;
        const stockActual = p.stockKardex !== undefined && p.stockKardex !== null ? p.stockKardex : (p.stock || 0);

        // Calcular cuántas unidades base ya están en el carrito PARA ESTE MISMO PRODUCTO,
        // excluyendo la fila actual.
        let unidadesAcumuladas = 0;
        const idProdActual = p.idProducto || p.id;
        for (const fila of this.carrito) {
            if (fila !== item && (fila.producto.idProducto || fila.producto.id) === idProdActual) {
                let factor = 1;
                if (fila.unidadSeleccionada === 'BLISTER' && fila.producto.factorBlister) {
                    factor = fila.producto.factorBlister;
                } else if (fila.unidadSeleccionada === 'CAJA' && fila.producto.factorCaja) {
                    factor = fila.producto.factorCaja;
                    if (fila.producto.manejaBlister && fila.producto.factorBlister) {
                        factor *= fila.producto.factorBlister;
                    }
                }
                unidadesAcumuladas += fila.cantidad * factor;
            }
        }

        const stockDisponible = Math.max(0, stockActual - unidadesAcumuladas);

        if (item.unidadSeleccionada === 'BLISTER' && p.factorBlister && p.factorBlister > 0) {
            return Math.floor(stockDisponible / p.factorBlister);
        }

        if (item.unidadSeleccionada === 'CAJA' && p.factorCaja && p.factorCaja > 0) {
            let factorRealCaja = p.factorCaja;
            if (p.manejaBlister && p.factorBlister && p.factorBlister > 0) {
                factorRealCaja = factorRealCaja * p.factorBlister;
            }
            return Math.floor(stockDisponible / factorRealCaja);
        }

        return stockDisponible;
    }

    getRemainingStockSummary(item: ItemVenta): string {
        const p = item.producto;
        const stockActual = p.stockKardex !== undefined && p.stockKardex !== null ? p.stockKardex : (p.stock || 0);

        let unidadesEnCarrito = 0;
        const idProdActual = p.idProducto || p.id;
        for (const fila of this.carrito) {
            if ((fila.producto.idProducto || fila.producto.id) === idProdActual) {
                let factor = 1;
                if (fila.unidadSeleccionada === 'BLISTER' && fila.producto.factorBlister) {
                    factor = fila.producto.factorBlister;
                } else if (fila.unidadSeleccionada === 'CAJA' && fila.producto.factorCaja) {
                    factor = fila.producto.factorCaja;
                    if (fila.producto.manejaBlister && fila.producto.factorBlister) {
                        factor *= fila.producto.factorBlister;
                    }
                }
                unidadesEnCarrito += fila.cantidad * factor;
            }
        }

        const stockDisponible = Math.max(0, stockActual - unidadesEnCarrito);

        if (item.unidadSeleccionada === 'CAJA' && p.factorCaja) {
            let factorRealCaja = p.factorCaja;
            if (p.manejaBlister && p.factorBlister) {
                factorRealCaja *= p.factorBlister;
            }
            const cajas = stockDisponible / factorRealCaja;
            return `${Number.isInteger(cajas) ? cajas : cajas.toFixed(1)} ${this.getAbrevUnidad(p, 'CAJA')}`;
        }

        if (item.unidadSeleccionada === 'BLISTER' && p.factorBlister) {
            const blisters = stockDisponible / p.factorBlister;
            return `${Number.isInteger(blisters) ? blisters : blisters.toFixed(1)} ${this.getAbrevUnidad(p, 'BLISTER')}`;
        }

        return `${stockDisponible} ${this.getAbrevUnidad(p, 'UNIDAD')}`;
    }

    getPrecioConDescuento(item: ItemVenta): number {
        let ahorroUnitario = 0;
        const d = item.descuentoSeleccionado;
        const det = d?.matchingDetail;

        if (det) {
            const tipo = det.tipoDescuento;
            const valor = det.valorDescuento;

            if (tipo === 'PORCENTAJE') {
                ahorroUnitario = item.precioUnitario * (valor / 100);
            } else if (tipo === 'MONTO_FIJO') {
                ahorroUnitario = valor;
            } else if (tipo === 'CANTIDAD' && det.cantidad > 0) {
                // "Lleva X Regala Y" -> Ahorro unitario promedio = (Base * Regala) / Lleva
                const base = item.precioUnitario;
                const regala = valor || 0;
                const lleva = det.cantidad;
                ahorroUnitario = (base * regala) / lleva;
            }
        }
        return Math.max(0, item.precioUnitario - ahorroUnitario);
    }

    cambiarUnidad(item: ItemVenta, unidad: 'UNIDAD' | 'BLISTER' | 'CAJA') {
        item.unidadSeleccionada = unidad;
        this.recalcularPrecioItem(item);
        this.actualizarFila(item);
    }

    actualizarFila(item: ItemVenta, calcTotales: boolean = true) {
        const multiplicadorIGV = 1.18;
        const maxStock = this.obtenerStockMaximo(item);
        if (item.cantidad > maxStock) {
            this.alertService.warning(`La cantidad no puede superar el stock disponible (${maxStock})`);
            item.cantidad = maxStock;
        }
        if (item.cantidad < 1) item.cantidad = 1;

        const afMain = item.producto.catalogo?.tipoAfectacion;
        const isExonerado = afMain === 'EXONERADO';
        const isInafecto = afMain === 'INAFECTO';
        const isGravado = !isExonerado && !isInafecto;
        const baseBruta = item.cantidad * item.precioUnitario;

        let ahorroConIgv = 0;
        let d = item.descuentoSeleccionado;
        let det = d?.matchingDetail;

        if (det && det.tipoDescuento === 'CANTIDAD' && item.cantidad < det.cantidad) {
            item.descuentoSeleccionado = null;
            d = null;
            det = null;
            this.alertService.info('Descuento removido por cantidad insuficiente.');
        }

        if (det) {
            const tipo = det.tipoDescuento;
            const valor = det.valorDescuento;
            let ahorroBase = 0;

            if (tipo === 'PORCENTAJE') {
                ahorroBase = baseBruta * (valor / 100);
            } else if (tipo === 'MONTO_FIJO') {
                ahorroBase = (item.cantidad * valor);
            } else if (tipo === 'CANTIDAD' && det.cantidad > 0) {
                const numPromociones = Math.floor(item.cantidad / det.cantidad);
                ahorroBase = numPromociones * (valor || 0) * item.precioUnitario;
            }

            ahorroConIgv = isGravado ? (ahorroBase * multiplicadorIGV) : ahorroBase;
        }

        if (isExonerado) {
            item.total = baseBruta - ahorroConIgv;
            item.baseImp = 0;
            item.igv = 0;
            item.valorExo = item.total;
            item.valorInaf = 0;
        } else if (isInafecto) {
            item.total = baseBruta - ahorroConIgv;
            item.baseImp = 0;
            item.igv = 0;
            item.valorExo = 0;
            item.valorInaf = item.total;
        } else {
            const totalBruto = baseBruta * multiplicadorIGV;
            item.total = Number((totalBruto - ahorroConIgv).toFixed(2));
            item.baseImp = Number((item.total / multiplicadorIGV).toFixed(2));
            item.igv = Number((item.total - item.baseImp).toFixed(2));
            item.valorExo = 0;
            item.valorInaf = 0;
        }

        if (calcTotales) {
            this.calcularTotales();
        }
    }

    eliminarItem(index: number) {
        this.carrito.splice(index, 1);
        this.calcularTotales();
    }

    calcularTotales() {
        this.subTotal = this.carrito.reduce((acc, item) => acc + item.baseImp, 0);
        this.igv = this.carrito.reduce((acc, item) => acc + item.igv, 0);
        this.valorExo = this.carrito.reduce((acc, item) => acc + item.valorExo, 0);
        this.valorInaf = this.carrito.reduce((acc, item) => acc + item.valorInaf, 0);
        this.total = Number((this.subTotal + this.igv + this.valorExo + this.valorInaf).toFixed(2));

        const mapaDescuentos = new Map<string, { nombre: string, ahorro: number, colorClass: string }>();
        this.carrito.forEach(item => {
            const d = item.descuentoSeleccionado;
            const det = d?.matchingDetail;
            if (det) {
                const nombre = d.nombre || 'Descuento';
                const uniqueKey = d.id ? `${d.id}` : nombre;

                let ahorroItem = 0;
                const tipo = det.tipoDescuento;
                const valor = det.valorDescuento;
                const isInafecto = item.producto.catalogo?.tipoAfectacion === 'EXONERADO' || item.producto.catalogo?.tipoAfectacion === 'INAFECTO';

                if (tipo === 'PORCENTAJE') {
                    ahorroItem = (item.cantidad * item.precioUnitario) * (valor / 100);
                } else if (tipo === 'MONTO_FIJO') {
                    ahorroItem = (item.cantidad * valor);
                } else if (tipo === 'CANTIDAD' && det.cantidad > 0) {
                    const numPromociones = Math.floor(item.cantidad / det.cantidad);
                    ahorroItem = numPromociones * (valor || 0) * item.precioUnitario;
                }

                if (!isInafecto) ahorroItem *= 1.18;

                const actual = mapaDescuentos.get(uniqueKey) || {
                    nombre: nombre,
                    ahorro: 0,
                    colorClass: this.getDescuentoColorClass(d)
                };
                actual.ahorro += ahorroItem;
                mapaDescuentos.set(uniqueKey, actual);
            }
        });

        this.descuentoTotal = this.descuentosDetalle.reduce((acc, d) => acc + d.ahorro, 0);
        this.descuentosDetalle = Array.from(mapaDescuentos.values());

        this.cdr.detectChanges();
    }

    registrarVenta(estado: 'VENTA' | 'COTIZACION') {
        if (this.carrito.length === 0) {
            this.alertService.error('Debe agregar al menos un producto');
            return;
        }

        // Si es COTIZACION, no validamos caja
        if (estado === 'COTIZACION') {
            this.procederConRegistro(estado);
            return;
        }

        if (estado === 'VENTA' && !this.tienePuntoVentaAsignado) {
            this.alertService.error('No se puede realizar la nota de venta. El punto actual no tiene un correlativo de VENTA (Boleta, Factura o Ticket) asignado.');
            return;
        }

        // Verificar Caja Chica antes de VENTA
        this.cajaChicaService.obtenerCajaAbierta().subscribe({
            next: (caja) => {
                this.zone.run(() => {
                    if (caja) {
                        this.procederConRegistro(estado);
                    } else {
                        this.estadoRegistroPendiente = estado;
                        this.showModalCaja = true;
                    }
                    this.cdr.detectChanges();
                });
            },
            error: (err) => {
                this.zone.run(() => {
                    this.alertService.error('Error al verificar caja chica');
                    this.cdr.detectChanges();
                });
            }
        });
    }

    onCajaCerrada(success: boolean) {
        this.showModalCaja = false;
        if (success && this.estadoRegistroPendiente) {
            this.procederConRegistro(this.estadoRegistroPendiente);
        }
        this.estadoRegistroPendiente = null;
    }

    private procederConRegistro(estado: 'VENTA' | 'COTIZACION') {
        const venta = {
            tipoDoc: this.tipoDocumentoSeleccionado || '03',
            estado: estado === 'VENTA' ? 'V' : 'C',
            idPersonal: this.selectedPatient?.id?.toString(),

            // Datos del Paciente (Auditoría/SIRE)
            nroDni: this.selectedPatient?.numdoc,
            nombrePac: this.selectedPatient?.nombreCompleto,
            tipoDni: '1', // DNI por defecto

            // Montos de Pago (Auditoría de Caja)
            importePago: this.pagoCon,
            vuelto: this.vuelto,

            canjeNotaVentaId: this.canjeNotaVentaId,

            fecha: new Date(),
            moneda: 'PEN',
            tipoPac: 'PA', // Valor por defecto
            idConvenio: '',
            observacion: '',
            metodoPago: this.metodoPagoSeleccionado,
            idPlantilla: this.plantillaSeleccionadaId,
            descuento: this.descuentoTotal,
            detalles: this.carrito.map(item => {
                const isInafecto = item.producto.catalogo?.tipoAfectacion === 'EXONERADO' || item.producto.catalogo?.tipoAfectacion === 'INAFECTO';
                const baseBruta = item.precioUnitario * item.cantidad;
                const totalBrutoEsperado = isInafecto ? baseBruta : (baseBruta * 1.18);
                const ahorroTotal = Number((totalBrutoEsperado - item.total).toFixed(2));

                const detalle: any = {
                    idCatalogo: item.producto.idCatalogo?.toString() || item.producto.id?.toString(),
                    idArticulo: item.producto.idProducto?.toString(),
                    glosa: item.producto.catalogo?.nombre || 'Producto',
                    cantidad: item.cantidad,
                    unidadMedida: item.unidadSeleccionada,
                    precioUnitario: item.precioUnitario,
                    total: item.total,

                    // Persistencia de Descuentos Detallados
                    descuento: ahorroTotal > 0 ? ahorroTotal : 0,
                    montoDescuento: ahorroTotal > 0 ? ahorroTotal : 0,
                    porcentajeDescuento: 0,
                    porcDsc: 0
                };

                // Si hay descuento seleccionado, extraer porcentaje si aplica
                if (item.descuentoSeleccionado?.matchingDetail?.tipoDescuento === 'PORCENTAJE') {
                    const pct = item.descuentoSeleccionado.matchingDetail.valorDescuento;
                    detalle.porcentajeDescuento = pct;
                    detalle.porcDsc = pct;
                }

                return detalle;
            })
        };

        this.ventaRegistroService.registrar(venta).subscribe({
            next: (res: any) => {
                this.ventaReciente = res.data;
                this.showModalExito = true;

                // Dejar inputs vacíos
                this.emailDestino = '';
                this.telefonoDestino = '';

                // Si hay plantilla por defecto, imprimir automáticamente si se desea
                if (this.plantillaSeleccionadaId && res.data) {
                    // Opcional: imprimirVenta(this.plantillaSeleccionadaId, res.data);
                }

                this.limpiarFormulario();
            },
            error: (err) => {
          const errorMsg = err.error?.message || 'No se pudo completar el registro';
          this.alertService.error(errorMsg);
        }
        });
    }

    // El método generarPdfPostVenta ya no es necesario si usamos impresión directa, 
    // pero lo mantenemos vacío o lo eliminamos para evitar errores de referencia si existiera en el HTML.
    generarPdfPostVenta(idVenta: number, idPlantilla: number, datosVenta: any) {
        this.documentoImpresionService.imprimirVenta(idPlantilla, datosVenta);
    }

    cerrarModalImpresion() {
        if (this.pdfUrl) {
            // Liberar memoria
            const urlString = (this.pdfUrl as any).changingThisBreaksApplicationSecurity;
            if (urlString) URL.revokeObjectURL(urlString);
        }
        this.showModalImpresion = false;
        this.pdfUrl = null;
    }

    cerrarModalDescuento() {
        this.showModalDescuento = false;
    }

    onDescuentoGuardado() {
        this.cerrarModalDescuento();
    }

    // --- ACCIONES POST-VENTA ---

    limpiarFormulario() {
        this.carrito = [];
        this.selectedPatient = null;
        this.pagoCon = null;
        this.calcularTotales();
        this.searchTerm = '';
        this.searchResults = [];
        this.itemActivo = null;
    }

    nuevaEmision() {
        this.showModalExito = false;
        this.ventaReciente = null;
        this.limpiarFormulario();
    }

    imprimirEnFormato(formato: string) {
        if (!this.ventaReciente) return;

        let idPlantilla = this.plantillaSeleccionadaId;

        // Si se pide un formato específico (A4, A5, etc.) lo buscamos, 
        // de lo contrario usamos el seleccionado en el dropdown
        if (formato !== 'SELECCIONADO') {
            const p = this.todasLasPlantillasVenta.find(tpl => {
                const nom = tpl.nombre.toUpperCase();
                if (formato === 'A4') return nom.includes('A4');
                if (formato === 'A5') return nom.includes('A5');
                if (formato === '80mm') return nom.includes('80') || nom.includes('TICKET');
                if (formato === '58mm') return nom.includes('58');
                return false;
            });
            if (p) idPlantilla = p.id;
        }

        if (idPlantilla) {
            this.documentoImpresionService.imprimirVenta(idPlantilla, this.ventaReciente)
                .catch(() => this.alertService.error('Error al imprimir comprobante'));
        } else {
            this.alertService.warning(`Seleccione una plantilla válida.`);
        }
    }

    descargarArchivo(tipo: 'XML' | 'HTML' | 'PDF') {
        if (!this.ventaReciente) return;

        if (tipo === 'PDF') {
            const idPlantilla = this.plantillaSeleccionadaId;
            if (!idPlantilla) {
                this.alertService.error('Seleccione una plantilla válida para generar el PDF');
                return;
            }
            this.alertService.info('Generando PDF...');
            this.ventaRegistroService.descargarPdf(this.ventaReciente.idVenta, idPlantilla).subscribe({
                next: (response: any) => {
                    this.procesarBlob(response.body, response.headers, 'pdf');
                },
                error: (err) => {
                    console.error('Error al descargar PDF:', err);
                    this.alertService.error('No se pudo generar el PDF');
                }
            });
            return;
        }

        const id = this.ventaReciente.idVenta;
        const downloadObs = tipo === 'XML'
            ? this.ventaRegistroService.descargarXml(id)
            : this.ventaRegistroService.descargarHtml(id);

        this.alertService.info(`Generando archivo ${tipo}...`);

        downloadObs.subscribe({
            next: (response: any) => {
                this.procesarBlob(response.body, response.headers, tipo.toLowerCase());
                this.alertService.success(`${tipo} descargado correctamente`);
            },
            error: (err) => {
                console.error(`Error descargando ${tipo}:`, err);
                this.alertService.error(`No se pudo generar el archivo ${tipo}. Verifique la conexión.`);
            }
        });
    }

    private procesarBlob(blob: Blob, headers: any, extension: string) {
        const contentDisposition = headers.get('Content-Disposition');
        let fileName = `comprobante-${this.ventaReciente.serie}-${this.ventaReciente.numero}.${extension}`;

        if (contentDisposition) {
            const fileNameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameRegex.exec(contentDisposition);
            if (matches != null && matches[1]) {
                fileName = matches[1].replace(/['"]/g, '');
            }
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    }

    compartir(medio: 'WHATSAPP' | 'CORREO' | 'LINK') {
        if (!this.ventaReciente) return;
        const id = this.ventaReciente.idVenta;

        if (medio === 'CORREO') {
            if (!this.emailDestino || !this.emailDestino.includes('@')) {
                this.alertService.error('Por favor ingrese un correo electrónico válido');
                return;
            }

            const idPlantilla = this.plantillaSeleccionadaId;
            if (!idPlantilla) {
                this.alertService.error('Seleccione una plantilla válida');
                return;
            }

            this.alertService.toast(`Enviando comprobante por correo a ${this.emailDestino}...`, 'info');
            this.ventaRegistroService.enviarPorEmail(id, this.emailDestino, idPlantilla).subscribe({
                next: (res) => {
                    this.alertService.toast(res.message || 'Correo enviado con éxito', 'success');
                },
                error: (err) => {
                    this.alertService.error('Error al enviar el correo');
                }
            });
        } else if (medio === 'WHATSAPP') {
            if (!this.telefonoDestino || this.telefonoDestino.length < 9) {
                this.alertService.error('Por favor ingrese un número de teléfono válido');
                return;
            }

            const idPlantilla = this.plantillaSeleccionadaId;
            if (!idPlantilla) {
                this.alertService.error('Seleccione una plantilla válida');
                return;
            }

            // Envío vía API (Meta)
            this.alertService.toast(`Enviando comprobante por WhatsApp a ${this.telefonoDestino}...`, 'info');
            this.ventaRegistroService.enviarPorWhatsApp(id, this.telefonoDestino, idPlantilla).subscribe({
                next: (res) => {
                    this.alertService.toast(res.message || 'Mensaje de WhatsApp enviado', 'success');
                },
                error: (err) => {
                    this.alertService.error('Error al enviar WhatsApp. Revisa la conexión o el estado de tu plantilla en Meta.');
                }
            });
        }
    }
}
