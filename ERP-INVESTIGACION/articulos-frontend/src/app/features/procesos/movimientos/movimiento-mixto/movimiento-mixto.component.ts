import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormTextareaComponent } from '../../../../shared/components/forms/form-textarea/form-textarea.component';
import { FormSelectComponent, SelectOption } from '../../../../shared/components/forms/form-select/form-select.component';
import { InventarioService } from '../../service/movimiento.service';
import { CatalogoService } from '../../../configuraciones/services/catalogo.service';
import { AlmacenService } from '../../../almacen/service/almacen.service';
import { ProductoService } from '../../../almacen/service/producto.service';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PuntoDocumentoService } from '../../../documentos/services/punto-documento.service';
import { DocumentoImpresionService } from '../../../documentos/services/documento-impresion.service';
import { MovimientoLocal } from '../../models/movimiento.model';

@Component({
    selector: 'app-movimiento-mixto',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        SearchableSelectComponent,
        ModalComponent,
        FormInputComponent,
        FormTextareaComponent,
        FormSelectComponent
    ],
    templateUrl: './movimiento-mixto.component.html'
})
export class MovimientoMixtoComponent implements OnInit {
    private fb = inject(FormBuilder);
    private inventarioService = inject(InventarioService);
    private catalogoService = inject(CatalogoService);
    private almacenService = inject(AlmacenService);
    private productoService = inject(ProductoService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    public sidebarService = inject(SidebarService);
    private puntoDocumentoService = inject(PuntoDocumentoService);
    private documentoImpresionService = inject(DocumentoImpresionService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);

    // Estado local
    movimientos = signal<MovimientoLocal[]>([]);
    loading = signal(false);
    showModal = signal(false);
    tipoModal = signal<'INGRESO' | 'SALIDA'>('INGRESO');
    idEditando = signal<number | null>(null);
    idMovimientoEnEdicion = signal<number | null>(null);
    motivoGeneral = signal('');
    tipoFijo = signal<'INGRESO' | 'SALIDA' | null>(null);

    // Datos para selectores en modal
    catalogos = signal<any[]>([]);
    almacenes = signal<any[]>([]);
    almacenOptions = signal<SelectOption[]>([]);
    clasificaciones = signal<any[]>([]);
    clasificacionOptions = signal<SelectOption[]>([]);
    lotes = signal<any[]>([]);

    // Detalle de producto seleccionado
    selectedCatalogo = signal<any>(null);
    costoPrevio = signal<number | null>(null);
    modoLote = signal<'EXISTENTE' | 'NUEVO'>('NUEVO');
    stockDisponibleTotal = signal<number>(0);

    // Plantillas de Impresión
    plantillas = signal<any[]>([]);
    plantillaOptions = signal<SelectOption[]>([]);
    plantillaSeleccionadaId = signal<number | null>(null);
    puntoDocSeleccionado = signal<any>(null);

    // Formulario modal
    formItem!: FormGroup;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos', route: '/procesos' },
        { label: 'Movimientos', route: '/procesos/movimientos' },
        { label: 'Nuevo Movimiento Mixto' }
    ];

    ngOnInit() {
        this.route.queryParamMap.subscribe(params => {
            const tipo = params.get('tipo') as 'INGRESO' | 'SALIDA';
            if (tipo) {
                this.tipoFijo.set(tipo);
                this.tipoModal.set(tipo);
            }
        });
        this.initForm();
        this.cargarAlmacenes();
        this.cargarCatalogos();
        this.cargarPlantillas();

        this.route.queryParamMap.subscribe(params => {
            const idCatalogo = params.get('idCatalogo');
            const nroLote = params.get('nroLote');
            const idAlmacen = params.get('idAlmacen');
            const cantidad = params.get('cantidad');
            const idProducto = params.get('idProducto');
            const tipo = params.get('tipo');

            if (tipo === 'SALIDA' && idCatalogo && nroLote) {
                setTimeout(() => {
                    this.abrirSalidaPorVencimientoDirecta({
                        idCatalogo: Number(idCatalogo),
                        nroLote: nroLote,
                        idAlmacen: idAlmacen ? Number(idAlmacen) : null,
                        cantidad: cantidad ? Number(cantidad) : null,
                        idProducto: idProducto ? Number(idProducto) : null
                    });
                }, 800);
            }

            const idMov = params.get('idMovimiento');
            if (idMov) {
                this.idMovimientoEnEdicion.set(Number(idMov));
                this.cargarMovimientoEnEdicion(Number(idMov));
            }
        });
    }

    initForm() {
        this.formItem = this.fb.group({
            idCatalogo: [null, [Validators.required]],
            cantidad: [null, [Validators.required, Validators.min(0.0001)]],
            costoUnitario: [null, [Validators.required, Validators.min(0.000001)]],
            idAlmacen: [null, [Validators.required]],
            nroLote: ['', [Validators.required]],
            fechaVenc: [null], // Se manejará dinámicamente
            idClasificacion: [null, [Validators.required]],
            idProducto: [null],
            observacion: [''] // Opcional
        });

        // Al cambiar catálogo, cargar lotes
        this.formItem.get('idCatalogo')?.valueChanges.subscribe(id => {
            if (id) {
                this.cargarLotes(id);
                const cat = this.catalogos().find(c => c.id === id);
                if (cat) {
                    this.selectedCatalogo.set(cat);
                    this.configurarValidadores(this.tipoModal(), this.modoLote());
                    if (this.tipoModal() === 'INGRESO') {
                        if (this.modoLote() === 'NUEVO') {
                            this.sugerirUltimoCosto();
                        } else {
                            this.formItem.patchValue({ costoUnitario: cat.precioCompra || 0 });
                        }
                    }
                }
            } else {
                this.selectedCatalogo.set(null);
                this.stockDisponibleTotal.set(0);
            }
        });

        // Al cambiar nroLote manual en Ingreso, consultar costo previo
        this.formItem.get('nroLote')?.valueChanges.subscribe(nroLote => {
            const idCat = this.formItem.get('idCatalogo')?.value;
            if (this.tipoModal() === 'INGRESO' && nroLote && idCat) {
                this.inventarioService.obtenerCostoPrevioLote(idCat, nroLote).subscribe(res => {
                    if (res.success && res.data > 0) {
                        this.costoPrevio.set(res.data);
                    } else {
                        this.costoPrevio.set(null);
                    }
                });
            }
        });

        // Al cambiar costo, validar vs costo previo
        this.formItem.get('costoUnitario')?.valueChanges.subscribe(nuevoCosto => {
            const previo = this.costoPrevio();
            if (this.tipoModal() === 'INGRESO' && previo !== null && nuevoCosto > 0) {
                if (nuevoCosto > previo) {
                    this.alertService.toast('El precio es mayor al precio anterior del lote (S/ ' + previo + ')', 'warning');
                } else if (nuevoCosto < previo) {
                    this.alertService.toast('El precio es menor al precio anterior del lote (S/ ' + previo + ')', 'info');
                }
            }
        });

        // Al seleccionar lote (en SALIDA o en INGRESO modo EXISTENTE)
        this.formItem.get('idProducto')?.valueChanges.subscribe(id => {
            if (id) {
                const lote = this.lotes().find(l => l.idProducto === id);
                if (lote) {
                    if (this.tipoModal() === 'SALIDA') {
                        this.formItem.patchValue({
                            nroLote: lote.nroLote,
                            fechaVenc: lote.fechaVencimiento,
                            costoUnitario: lote.precioCompra
                        });
                        // Solo en SALIDA restringimos por stock
                        this.formItem.get('cantidad')?.setValidators([
                            Validators.required,
                            Validators.min(0.0001),
                            Validators.max(lote.stockReal)
                        ]);
                    } else if (this.tipoModal() === 'INGRESO' && this.modoLote() === 'EXISTENTE') {
                        this.formItem.patchValue({
                            nroLote: lote.nroLote,
                            fechaVenc: lote.fechaVencimiento,
                            costoUnitario: lote.precioCompra
                        }, { emitEvent: false });
                        this.costoPrevio.set(lote.precioCompra || 0);
                        // En INGRESO no hay máximo (estamos sumando stock)
                        this.formItem.get('cantidad')?.setValidators([
                            Validators.required,
                            Validators.min(0.0001)
                        ]);
                    }
                    this.formItem.get('cantidad')?.updateValueAndValidity();
                }
            }
        });
    }

    cargarMovimientoEnEdicion(id: number): void {
        this.loading.set(true);
        this.inventarioService.obtenerMovimientoDiverso(id).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const mov = res.data;
                    this.motivoGeneral.set(mov.motivo || '');
                    
                    if (mov.detalles && mov.detalles.length > 0) {
                        const locales: MovimientoLocal[] = mov.detalles.map((d: any, index: number) => ({
                            idInterno: Date.now() + index,
                            tipo: d.tipo === 'INGRESO' || d.tipo === '+' ? 'INGRESO' : 'SALIDA',
                            idCatalogo: d.idCatalogo,
                            nombreProducto: d.productoNombre,
                            cantidad: d.cantidad,
                            costoUnitario: d.costoUnitario,
                            nroLote: d.nroLote,
                            fechaVenc: d.fechaVencimiento,
                            idClasificacion: d.idClasificacion,
                            nombreClasificacion: d.clasificacionNombre,
                            idAlmacen: d.idAlmacen,
                            observacion: d.observacion
                        }));
                        this.movimientos.set(locales);
                    }
                }
                this.loading.set(false);
            },
            error: () => {
                this.alertService.error('Error', 'No se pudo cargar la cotización');
                this.loading.set(false);
            }
        });
    }

    sugerirUltimoCosto() {
        const idCat = this.formItem.get('idCatalogo')?.value;
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (idCat && sucursalId) {
            this.inventarioService.obtenerUltimoCosto(idCat, sucursalId).subscribe(res => {
                if (res.success && res.data > 0) {
                    this.formItem.patchValue({ costoUnitario: res.data }, { emitEvent: false });
                    this.costoPrevio.set(res.data);
                } else {
                    this.costoPrevio.set(null);
                }
            });
        }
    }

    getErrorMessage(controlName: string): string | null {
        const control = this.formItem.get(controlName);
        if (!control || !control.touched || control.valid) return null;

        if (control.hasError('required')) return 'Este campo es obligatorio';
        if (control.hasError('min')) return 'El valor debe ser mayor a 0';
        if (control.hasError('max')) {
            return `Máximo disponible: ${control.errors?.['max'].max}`;
        }
        if (control.hasError('minlength')) return `Mínimo ${control.errors?.['minlength'].requiredLength} caracteres`;

        return 'Campo inválido';
    }

    cargarAlmacenes() {
        const sucursalId = this.authService.getSucursalIdFromToken();
        if (sucursalId) {
            this.almacenService.getBySucursal(sucursalId).subscribe((res: any) => {
                const resData = res.data || res;
                const data = Array.isArray(resData) ? resData : (resData?.content ? resData.content : []);
                this.almacenes.set(data);
                this.almacenOptions.set(data.map((a: any) => ({ label: a.nombre, value: a.id })));
                if (data.length > 0) {
                    this.formItem.patchValue({ idAlmacen: data[0].id });
                }
            });
        }
    }

    cargarPlantillas() {
        const puntoId = this.authService.getPuntoIdFromToken();
        if (!puntoId) return;

        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Determinar el módulo a buscar basado en el tipo de movimiento
                    // Si no hay tipo seleccionado aún (tipoFijo es null), cargamos ambos o priorizamos ingresos
                    const tipoMov = this.tipoModal();
                    const moduloBuscado = tipoMov === 'INGRESO' ? 'INGRESOS_DIVERSOS' : 'SALIDAS_DIVERSAS';

                    // Filtrar plantillas del módulo correspondiente (Ingresos, Salidas, Diversos o Todos)
                    const docsDiversos = res.data.filter((pd: any) => {
                        // El estado y módulo pueden venir como objetos debido a la serialización del enum
                        const estado = typeof pd.estado === 'object' ? pd.estado?.name : pd.estado;
                        const modulo = typeof pd.modulo === 'object' ? pd.modulo?.name : pd.modulo;

                        const esFiltroValido =
                            modulo === moduloBuscado ||
                            modulo === 'DIVERSOS' ||
                            modulo === 'TODOS';

                        return esFiltroValido && estado === 'ACTIVO' && pd.idPlantilla;
                    });

                    const plantillasMapped = docsDiversos.map((pd: any) => {
                        const nombrePlantilla = pd.plantillaNombre || `Plantilla ${pd.idPlantilla}`;
                        const moduloStr = typeof pd.modulo === 'object' ? pd.modulo?.name : pd.modulo;

                        return {
                            id: pd.id,
                            idPlantilla: pd.idPlantilla,
                            nombre: `${nombrePlantilla} - ${pd.serie}-${pd.numero || 0} (${moduloStr})`,
                            tipoDocumento: { nombre: pd.tipoDocumentoNombre || pd.tipoDoc },
                            tipoDoc: pd.tipoDoc,
                            serie: pd.serie,
                            numero: pd.numero,
                            puntoDocCompleto: pd,
                            modulo: moduloStr
                        };
                    });

                    this.plantillas.set(plantillasMapped);
                    this.plantillaOptions.set(plantillasMapped.map(p => ({
                        label: p.nombre,
                        value: p.id
                    })));

                    // Seleccionar "Ticket Movimientos" o el primero por defecto
                    const ticket = plantillasMapped.find((p: any) => p.nombre.toLowerCase().includes('movimiento'));
                    if (ticket) {
                        this.plantillaSeleccionadaId.set(ticket.id);
                    } else if (plantillasMapped.length > 0) {
                        this.plantillaSeleccionadaId.set(plantillasMapped[0].id);
                    }
                    this.onPlantillaChange();
                }
            }
        });
    }

    onPlantillaChange() {
        const id = this.plantillaSeleccionadaId();
        const p = this.plantillas().find(x => x.id == id);
        this.puntoDocSeleccionado.set(p || null);
    }

    cargarCatalogos() {
        this.catalogoService.listarTodos(0, 1000, 'PRODUCTO').subscribe(res => {
            if (res.success) this.catalogos.set(res.data.content);
        });
    }

    cargarClasificaciones(tipo: string) {
        this.inventarioService.obtenerClasificaciones(tipo).subscribe(res => {
            if (res.success && res.data) {
                const data = Array.isArray(res.data.content) ? res.data.content :
                    (Array.isArray(res.data) ? res.data : []);

                this.clasificaciones.set(data);
                this.clasificacionOptions.set(data.map((c: any) => ({ label: c.nombre, value: c.id })));
            }
        });
    }

    cargarLotes(idCatalogo: number) {
        this.productoService.listarPorCatalogo(idCatalogo).subscribe(res => {
            if (res.success && res.data) {
                const sucursalId = this.authService.getSucursalIdFromToken();
                const data = res.data;
                let filtered = data.filter((p: any) => p.idSucursal === sucursalId);

                if (this.tipoModal() === 'SALIDA') {
                    filtered = filtered.filter((p: any) => (p.stockKardex ?? p.stock) > 0);
                }

                const mapped = filtered.map((p: any) => ({
                    ...p,
                    displayLabel: `${p.nroLote} (Stock: ${p.stockKardex ?? p.stock} | Venc: ${p.fechaVencimiento || 'S/V'})`,
                    stockReal: p.stockKardex ?? p.stock
                }));
                this.lotes.set(mapped);

                // Calcular stock total
                const totalStock = mapped.reduce((acc: number, val: any) => acc + val.stockReal, 0);
                this.stockDisponibleTotal.set(totalStock);

                // Auto-selección para productos que no manejan lotes
                if (this.selectedCatalogo()?.manejaLotes === false && mapped.length > 0) {
                    const primero = mapped[0];

                    if (this.tipoModal() === 'SALIDA') {
                        this.formItem.patchValue({
                            idProducto: primero.idProducto,
                            nroLote: primero.nroLote,
                            fechaVenc: primero.fechaVencimiento,
                            costoUnitario: primero.precioCompra
                        }, { emitEvent: false });

                        this.formItem.get('cantidad')?.setValidators([
                            Validators.required,
                            Validators.min(0.0001),
                            Validators.max(totalStock)
                        ]);
                        this.formItem.get('cantidad')?.updateValueAndValidity();
                    }
                }
            }
        });
    }

    abrirModalAdd(tipo: 'INGRESO' | 'SALIDA') {
        this.idEditando.set(null);
        this.tipoModal.set(tipo);
        this.modoLote.set(tipo === 'SALIDA' ? 'EXISTENTE' : 'NUEVO');
        this.formItem.reset({
            idAlmacen: this.almacenes().length > 0 ? this.almacenes()[0].id : null,
            idCatalogo: null,
            cantidad: null,
            costoUnitario: null,
            nroLote: '',
            idClasificacion: null,
            observacion: ''
        });
        this.stockDisponibleTotal.set(0);

        this.formItem.get('cantidad')?.setValidators([
            Validators.required,
            Validators.min(0.0001)
        ]);
        this.formItem.get('cantidad')?.updateValueAndValidity();

        this.configurarValidadores(tipo, this.modoLote());
        this.cargarClasificaciones(tipo);
        this.cargarPlantillas(); // Recargar plantillas para filtrar por el nuevo tipo seleccionado
        if (this.modoLote() === 'NUEVO') {
            this.sugerirUltimoCosto();
        }
        this.showModal.set(true);
    }

    abrirSalidaPorVencimientoDirecta(data: { idCatalogo: number, nroLote: string, idAlmacen: number | null, cantidad: number | null, idProducto: number | null }) {
        this.idEditando.set(null);
        this.tipoModal.set('SALIDA');
        this.modoLote.set('EXISTENTE');
        this.cargarClasificaciones('SALIDA');
        this.cargarPlantillas();

        setTimeout(() => {
            let idClasif = null;
            const vencClasif = this.clasificaciones().find(c => 
                c.nombre?.toUpperCase().includes('VENC') || 
                c.nombre?.toUpperCase().includes('MERMA') || 
                c.nombre?.toUpperCase().includes('BAJA')
            );
            if (vencClasif) {
                idClasif = vencClasif.id;
            } else if (this.clasificaciones().length > 0) {
                idClasif = this.clasificaciones()[0].id;
            }

            this.formItem.reset({
                idAlmacen: data.idAlmacen || (this.almacenes().length > 0 ? this.almacenes()[0].id : null),
                idCatalogo: data.idCatalogo,
                cantidad: data.cantidad,
                costoUnitario: null,
                nroLote: data.nroLote,
                idClasificacion: idClasif,
                idProducto: data.idProducto,
                observacion: 'SALIDA POR VENCIMIENTO DE LOTE'
            });

            this.cargarLotes(data.idCatalogo);

            if (data.idProducto) {
                setTimeout(() => {
                    const loteEncontrado = this.lotes().find(l => l.idProducto === data.idProducto || l.id === data.idProducto || l.nroLote === data.nroLote);
                    if (loteEncontrado) {
                        this.formItem.patchValue({
                            idProducto: loteEncontrado.idProducto || loteEncontrado.id,
                            costoUnitario: loteEncontrado.precioCompra || 0
                        });
                        this.stockDisponibleTotal.set(loteEncontrado.stockReal || data.cantidad || 0);

                        this.formItem.get('cantidad')?.setValidators([
                            Validators.required,
                            Validators.min(0.0001),
                            Validators.max(loteEncontrado.stockReal)
                        ]);
                        this.formItem.get('cantidad')?.updateValueAndValidity();
                    }
                }, 500);
            }

            this.showModal.set(true);
        }, 300);
    }

    cambiarModoLote(modo: 'EXISTENTE' | 'NUEVO') {
        this.modoLote.set(modo);
        this.costoPrevio.set(null);
        this.formItem.patchValue({
            idProducto: null,
            nroLote: '',
            fechaVenc: null,
            costoUnitario: null
        });

        // Al cambiar de modo, reseteamos validadores de cantidad por si acaso
        this.formItem.get('cantidad')?.setValidators([
            Validators.required,
            Validators.min(0.0001)
        ]);
        this.formItem.get('cantidad')?.updateValueAndValidity();

        this.configurarValidadores(this.tipoModal(), modo);
        if (modo === 'NUEVO') {
            this.sugerirUltimoCosto();
        }
    }

    private configurarValidadores(tipo: 'INGRESO' | 'SALIDA', modo: 'EXISTENTE' | 'NUEVO') {
        const idProductoCtrl = this.formItem.get('idProducto');
        const nroLoteCtrl = this.formItem.get('nroLote');
        const fechaVencCtrl = this.formItem.get('fechaVenc');

        const manejaLotes = this.selectedCatalogo()?.manejaLotes !== false;

        // Fecha de vencimiento obligatoria solo en INGRESO y si el catálogo maneja lotes
        if (tipo === 'INGRESO' && manejaLotes) {
            fechaVencCtrl?.setValidators([Validators.required]);
        } else {
            fechaVencCtrl?.clearValidators();
        }

        if (tipo === 'SALIDA' || (tipo === 'INGRESO' && modo === 'EXISTENTE')) {
            idProductoCtrl?.setValidators([Validators.required]);
            nroLoteCtrl?.clearValidators();
        } else {
            idProductoCtrl?.clearValidators();
            if (manejaLotes) {
                nroLoteCtrl?.setValidators([Validators.required]);
            } else {
                nroLoteCtrl?.clearValidators();
            }
        }

        idProductoCtrl?.updateValueAndValidity();
        nroLoteCtrl?.updateValueAndValidity();
        fechaVencCtrl?.updateValueAndValidity();
    }

    editarItem(item: MovimientoLocal) {
        this.idEditando.set(item.idInterno);
        this.tipoModal.set(item.tipo);

        // Determinar modo lote
        if (item.tipo === 'SALIDA') {
            this.modoLote.set('EXISTENTE');
        } else {
            this.modoLote.set(item.idProducto ? 'EXISTENTE' : 'NUEVO');
        }

        this.cargarClasificaciones(item.tipo);
        this.cargarLotes(item.idCatalogo);

        this.formItem.patchValue({
            idCatalogo: item.idCatalogo,
            cantidad: item.cantidad,
            costoUnitario: item.costoUnitario,
            idAlmacen: item.idAlmacen,
            nroLote: item.nroLote,
            fechaVenc: item.fechaVenc,
            idClasificacion: item.idClasificacion,
            idProducto: item.idProducto,
            observacion: item.observacion
        });

        this.showModal.set(true);
    }

    agregarALista() {
        if (this.formItem.invalid) {
            this.formItem.markAllAsTouched();
            return;
        }

        const val = this.formItem.value;
        const cat = this.catalogos().find(c => c.id == val.idCatalogo);
        const clas = this.clasificaciones().find(c => c.id == val.idClasificacion);

        const idEdit = this.idEditando();

        if (idEdit) {
            this.movimientos.update(list => list.map(m =>
                m.idInterno === idEdit ? {
                    ...m,
                    idCatalogo: val.idCatalogo,
                    nombreProducto: cat ? `${cat.nombre} - ${cat.presentacion || 'UNIDAD'}` : m.nombreProducto,
                    cantidad: val.cantidad,
                    costoUnitario: val.costoUnitario,
                    nroLote: val.nroLote,
                    fechaVenc: val.fechaVenc,
                    idClasificacion: val.idClasificacion,
                    nombreClasificacion: clas?.nombre || m.nombreClasificacion,
                    idAlmacen: val.idAlmacen,
                    idProducto: val.idProducto,
                    observacion: val.observacion
                } : m
            ));
            this.alertService.toast('Item actualizado', 'success');
        } else {
            const nuevo: MovimientoLocal = {
                idInterno: Date.now(),
                tipo: this.tipoModal(),
                idCatalogo: val.idCatalogo,
                nombreProducto: cat ? `${cat.nombre} - ${cat.presentacion || 'UNIDAD'}` : 'Desconocido',
                cantidad: val.cantidad,
                costoUnitario: val.costoUnitario,
                nroLote: val.nroLote,
                fechaVenc: val.fechaVenc,
                idClasificacion: val.idClasificacion,
                nombreClasificacion: clas?.nombre,
                idAlmacen: val.idAlmacen,
                idProducto: val.idProducto,
                observacion: val.observacion
            };

            this.movimientos.update(list => [...list, nuevo]);
            this.alertService.toast('Item añadido', 'success');
        }

        this.showModal.set(false);
    }

    eliminarDeLista(idInterno: number) {
        this.movimientos.update(list => list.filter(m => m.idInterno !== idInterno));
    }

    guardarGeneral(esCotizacion: boolean = false) {
        if (this.movimientos().length === 0) {
            this.alertService.error('Error', 'Debe añadir al menos un movimiento a la lista');
            return;
        }

        if (!this.motivoGeneral().trim()) {
            this.alertService.error('Error', 'Debe ingresar un motivo general para el movimiento');
            return;
        }

        this.loading.set(true);
        const idSucursal = this.authService.getSucursalIdFromToken();
        const idUsuario = this.authService.getUserIdFromToken();

        if (!idSucursal || !idUsuario) return;

        let serie: string | undefined;
        let numero: number | undefined;

        if (!esCotizacion && this.puntoDocSeleccionado()) {
            serie = this.puntoDocSeleccionado().serie;
            numero = this.puntoDocSeleccionado().numero;
        }

        const request = {
            idSucursal,
            idUsuario,
            motivo: this.motivoGeneral(),
            estado: esCotizacion ? 'COTIZACION' : 'ACTIVO',
            serie,
            numero,
            idMovimiento: this.idMovimientoEnEdicion(),
            idPlantilla: this.plantillaSeleccionadaId(),
            detalles: this.movimientos().map(m => ({
                tipo: m.tipo,
                idCatalogo: m.idCatalogo,
                idProducto: m.idProducto,
                idAlmacen: m.idAlmacen,
                cantidad: m.cantidad,
                costoUnitario: m.costoUnitario,
                nroLote: m.nroLote,
                fechaVenc: m.fechaVenc,
                idClasificacion: m.idClasificacion,
                observacion: m.observacion
            }))
        };

        this.inventarioService.registrarMovimientoMixtoBatch(request).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success) {
                    console.log('✅ Registro batch exitoso:', res.data);
                    this.alertService.toast('Movimientos registrados con éxito', 'success');

                    const idPlantilla = this.plantillaSeleccionadaId();
                    console.log('📄 ID Plantilla seleccionada:', idPlantilla);

                    // Si hay plantilla seleccionada, imprimir (solo si NO es cotización)
                    if (idPlantilla && res.data && !esCotizacion) {
                        console.log('🚀 Iniciando proceso de impresión para el movimiento...');
                        this.documentoImpresionService.imprimirMovimiento(idPlantilla, res.data)
                            .then(() => console.log('✅ Proceso de impresión enviado al service'))
                            .catch(err => {
                                console.error('❌ Error en el proceso de impresión:', err);
                                this.alertService.error('Error al intentar abrir el diálogo de impresión');
                            });
                    } else if (esCotizacion) {
                        console.log('⚠️ No se imprime porque es un borrador (cotización).');
                    } else {
                        console.warn('⚠️ No se disparó la impresión. idPlantilla:', idPlantilla, 'res.data:');
                    }

                    this.router.navigate(['/procesos/historial-movimientos']);
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error', err.error?.message || 'Error al procesar el batch');
            }
        });
    }

    cancelar() {
        if (this.movimientos().length > 0) {
            // Confirmar si desea salir
            this.router.navigate(['/procesos/historial-movimientos']);
        } else {
            this.router.navigate(['/procesos/historial-movimientos']);
        }
    }
}
