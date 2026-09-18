import { Component, EventEmitter, Input, OnInit, Output, signal, HostListener, ElementRef, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Descuento, DescuentoRequest, TipoDescuento } from '../../models/descuento.model';
import { DescuentoService } from '../../services/descuento.service';
import { ProductoService } from '../../../../almacen/service/producto.service';
import { Producto } from '../../../../almacen/models/producto.model';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { AlertService } from '../../../../../core/services/alert.service';
import { UserService, UserResponse } from '../../../../../core/services/user.service';
import { SearchGenericComponent } from '../../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { SearchableSelectComponent } from '../../../../../shared/components/searchable-select/searchable-select.component';
import { PaginationComponent } from '../../../../../shared/components/pagination/pagination';
import { AuthService } from '../../../../auth/services/auth.service';
import { ConvenioService } from '../../services/convenio.service';
import * as XLSX from 'xlsx';
import { FormInputComponent } from '../../../../../shared/components/forms/form-input/form-input.component';
import { FormTextareaComponent } from '../../../../../shared/components/forms/form-textarea/form-textarea.component';
import { FormSelectComponent } from '../../../../../shared/components/forms/form-select/form-select.component';

@Component({
    selector: 'app-descuento-form',
    standalone: true,
    imports: [
        CommonModule, 
        NgIf, 
        NgFor, 
        ReactiveFormsModule, 
        FormsModule,
        SearchGenericComponent, 
        SearchableSelectComponent, 
        PaginationComponent,
        FormInputComponent,
        FormTextareaComponent,
        FormSelectComponent
    ],
    templateUrl: './descuento-form.component.html'
})
export class DescuentoFormComponent implements OnInit, OnChanges {
    @ViewChild('productSearch') productSearch!: SearchableSelectComponent;
    @Input() descuento: Descuento | null = null;
    @Input() idCompaniaPreset: number | null = null;
    @Output() cerrar = new EventEmitter<void>();
    @Output() guardado = new EventEmitter<void>();

    form!: FormGroup;
    loading = signal(false);
    isGlobalValidated = signal(false);

    // Enums for template
    TipoDescuento = TipoDescuento;

    globalModeDiscountOptions = [
        { label: 'Porcentaje (%)', value: TipoDescuento.PORCENTAJE },
        { label: 'Monto Fijo (S/)', value: TipoDescuento.MONTO_FIJO },
        { label: 'Paquete (Lleva X Paga S/ Y)', value: TipoDescuento.CANTIDAD }
    ];

    // Searchable Data
    productos = signal<any[]>([]);

    // Maestro-Detalle Selection
    selectedDetails = signal<{ producto: Producto, tipoDescuento: string, valorDescuento: number, cantidad: number, cantidadMinima?: number, cantidadRegalo?: number, valido: boolean, lotesValidados: { producto: Producto, precioFinal: number, ganancia: number, valido: boolean }[] }[]>([]);

    usuariosCargados = signal<{ id: string, dni: string, nombre: string, tipo: string }[]>([]);
    tiposBeneficiario = ['DIRECTO', 'FAMILIAR', 'TITULAR', 'HIJO', 'ESPOSO(A)', 'MADRE', 'PADRE', 'OTROS'];

    // Segmentación Avanzada de Usuarios
    usuariosPaginados = signal<UserResponse[]>([]);
    loadingUsers = signal(false);
    currentUsersPage = signal(0);
    usersPageSize = signal(5);
    totalUsers = signal(0);
    totalUsersPages = signal(0);
    selectedUserIds = signal<number[]>([]);
    searchQueryUsers = signal('');
    selectedSegmentMode = signal<'ALL' | 'MANUAL' | 'EXCEL' | 'CONVENIO'>('ALL');
    companias = signal<any[]>([]);
    loadingCompanias = signal(false);
    selectedCompaniaId = signal<number | null>(null);
    showExcelExample = signal(false);
    isValidatingExcel = signal(false);
    importReport = signal<{
        valid: { id: string, dni: string, tipo: string, nombre?: string }[],
        invalid: { id: string, tipo: string, motivo: string }[],
        summary: { total: number, validCount: number, errorCount: number }
    } | null>(null);

    // Search Options for Users
    searchOptionsUsers = [
        { label: 'Nombre/Usuario', value: 'nombre', icon: 'bi-person' },
        { label: 'DNI/RUC', value: 'documento', icon: 'bi-card-text' }
    ];

    // Global Mode Data
    globalModeDetail = signal<{ tipoDescuento: string, valorDescuento: number | null, cantidad: number | null, cantidadMinima?: number | null, cantidadRegalo?: number | null }>({
        tipoDescuento: TipoDescuento.PORCENTAJE,
        valorDescuento: null,
        cantidad: null,
        cantidadMinima: null,
        cantidadRegalo: null
    });

    // Vista Detallada de Lotes
    showLotesModal = signal(false);
    lotesModalTitle = signal('');
    lotesValidadosSeleccionados = signal<{ producto: Producto, precioFinal: number, ganancia: number, valido: boolean }[]>([]);

    constructor(
        private fb: FormBuilder,
        private descuentoService: DescuentoService,
        private productoService: ProductoService,
        private alertService: AlertService,
        private userService: UserService,
        private authService: AuthService,
        private convenioService: ConvenioService,
        private elementRef: ElementRef
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.loadProductos();
        this.loadUsers();

        if (this.idCompaniaPreset) {
            this.form.get('idCompania')?.setValue(this.idCompaniaPreset);
            this.selectedSegmentMode.set('CONVENIO');
            this.selectedCompaniaId.set(this.idCompaniaPreset);
            this.loadCompanias();
        }

        if (this.descuento) {
            this.cargarDatosDescuento();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['descuento'] && this.descuento) {
            if (!this.form) this.initForm();
            this.cargarDatosDescuento();
        }
    }

    private cargarDatosDescuento(): void {
        if (!this.descuento || !this.form) return;

        this.form.patchValue({
            ...this.descuento,
            fechaInicio: this.descuento.fechaInicio?.split('T')[0],
            fechaFin: this.descuento.fechaFin?.split('T')[0],
            activo: !!this.descuento.activo
        });

        // Manejo de usuarios
        if (this.descuento.usuariosAfectados) {
            if (this.descuento.usuariosAfectados === 'ALL') {
                this.selectedSegmentMode.set('ALL');
            } else {
                try {
                    const users = JSON.parse(this.descuento.usuariosAfectados);
                    if (Array.isArray(users)) {
                        this.usuariosCargados.set(users.map((u: any) => ({
                            id: String(u.id),
                            dni: u.dni || String(u.id),
                            nombre: u.nombre || 'Cargado...',
                            tipo: u.tipo || 'DIRECTO'
                        })));
                        const ids = users.map((u: any) => typeof u === 'object' ? Number(u.id) : Number(u)).filter(id => !isNaN(id));
                        this.selectedUserIds.set(ids);
                        this.selectedSegmentMode.set('MANUAL');
                    } else if (users && typeof users === 'object' && users.idCompania) {
                        this.selectedCompaniaId.set(Number(users.idCompania));
                        this.selectedSegmentMode.set('CONVENIO');
                        this.loadCompanias();
                    }
                } catch (e) {
                    this.selectedSegmentMode.set('EXCEL');
                }
            }
        }

        // Cargar detalles
        if (this.descuento.detalles && this.descuento.detalles.length > 0) {
            if (this.descuento.tipoAlcance === 'GLOBAL') {
                const first = this.descuento.detalles[0];
                this.globalModeDetail.set({
                    tipoDescuento: first.tipoDescuento,
                    valorDescuento: first.valorDescuento,
                    cantidad: first.cantidad || 1
                });
            } else {
                this.loadDetailsFromModel(this.descuento.detalles);
            }
        }
    }

    private isExpired(fechaVencimiento?: any): boolean {
        if (!fechaVencimiento) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        // Assumes format YYYY-MM-DD
        const [year, month, day] = fechaVencimiento.split('-').map(Number);
        const venc = new Date(year, month - 1, day);
        return venc < today;
    }

    private loadDetailsFromModel(detalles: any[]) {
        this.selectedDetails.set([]);
        const idSucursal = this.authService.getSucursalIdFromToken() || 1;
        const observables = detalles.map(d => this.productoService.listarPorCatalogoYSucursal(d.idCatalogo, idSucursal).pipe(
            map(res => {
                if (res.success && res.data && res.data.length > 0) {
                    const activos = res.data.filter(p => (p.precioVentaUnitario || 0) > 0 && !this.isExpired(p.fechaVencimiento));
                    if (activos.length === 0) activos.push(res.data[0]);
                    return { success: true, data: activos };
                }
                return { success: false, data: [] };
            })
        ));
        forkJoin(observables).subscribe(responses => {
            const mappedDetails = responses
                .filter(res => res.success && res.data.length > 0)
                .map((res, index) => {
                    const lotes = res.data;
                    const prod = lotes[0];
                    const modelDetalle = detalles[index];
                    
                    const lotesValidados = lotes.map(lote => {
                        const cal = this.calcularDescuentoProducto(lote, modelDetalle.tipoDescuento, modelDetalle.valorDescuento, modelDetalle.cantidad);
                        return {
                            producto: lote,
                            ...cal
                        };
                    });

                    return {
                        producto: prod,
                        tipoDescuento: modelDetalle.tipoDescuento,
                        valorDescuento: modelDetalle.valorDescuento,
                        cantidad: modelDetalle.cantidad || 1,
                        valido: lotesValidados.every(l => l.valido),
                        lotesValidados: lotesValidados
                    };
                });
            this.selectedDetails.set(mappedDetails);
        });
    }

    initForm(): void {
        const today = new Date().toISOString().split('T')[0];
        this.form = this.fb.group({
            nombre: ['', [Validators.required]],
            descripcion: [''],
            usuariosAfectados: ['ALL'],
            tipoAlcance: ['GLOBAL', [Validators.required]],
            fechaInicio: [today, [Validators.required]],
            fechaFin: ['', [Validators.required]],
            activo: [true]
        });

        // Al cambiar tipoAlcance, invalidar validación global si era necesario
        this.form.get('tipoAlcance')?.valueChanges.subscribe(() => {
            this.isGlobalValidated.set(false);
        });
    }

    loadProductos(): void {
        const idSucursal = this.authService.getSucursalIdFromToken() || 1;
        this.productoService.listarParaDescuentos(idSucursal, 0, 50, 'idCatalogo', 'ASC').subscribe(res => {
            if (res.success && res.data) {
                const mapped = res.data.content.map((p: any) => {
                    const nombreBase = p.catalogo?.nombre || 'Sin nombre';
                    return {
                        ...p,
                        nombre: nombreBase
                    };
                });
                this.productos.set(mapped);
            }
        });
    }

    loadUsers(): void {
        this.loadingUsers.set(true);
        const obs$ = this.searchQueryUsers()
            ? this.userService.searchUsers(this.searchQueryUsers(), this.currentUsersPage(), this.usersPageSize())
            : this.userService.getActiveUsers(this.currentUsersPage(), this.usersPageSize());

        obs$.subscribe({
            next: (res) => {
                this.usuariosPaginados.set(res.content);
                this.totalUsers.set(res.totalElements);
                this.totalUsersPages.set(res.totalPages);
                this.loadingUsers.set(false);
            },
            error: () => this.loadingUsers.set(false)
        });
    }

    onUserSearch(event: any): void {
        this.searchQueryUsers.set(event.q);
        this.currentUsersPage.set(0);
        this.loadUsers();
    }

    onUserPageChange(page: number): void {
        this.currentUsersPage.set(page);
        this.loadUsers();
    }

    toggleUserSelection(userId: number): void {
        const user = this.usuariosPaginados().find(u => u.id === userId);
        if (!user) return;

        this.selectedUserIds.update(ids => {
            const index = ids.indexOf(userId);
            if (index !== -1) {
                this.usuariosCargados.update(c => c.filter(u => Number(u.id) !== userId));
                return ids.filter(id => id !== userId);
            } else {
                this.usuariosCargados.update(c => [...c, {
                    id: String(user.id),
                    dni: user.numdoc || String(user.id),
                    nombre: user.nombreCompleto || 'Desconocido',
                    tipo: 'DIRECTO'
                }]);
                return [...ids, userId];
            }
        });
        this.syncUsuariosAfectados();
    }

    isUserSelected(userId: number): boolean {
        return this.selectedUserIds().includes(userId);
    }

    public syncUsuariosAfectados(): void {
        if (this.selectedSegmentMode() === 'CONVENIO') {
            if (this.selectedCompaniaId()) {
                const comp = this.companias().find(c => c.id === this.selectedCompaniaId());
                this.form.get('usuariosAfectados')?.setValue(JSON.stringify({
                    mode: 'CONVENIO',
                    idCompania: this.selectedCompaniaId(),
                    nombreCompania: comp?.nombre || 'Compañía'
                }));
            }
            return;
        }

        if (this.selectedSegmentMode() === 'ALL') {
            this.form.get('usuariosAfectados')?.setValue('ALL');
        } else {
            const beneficiarios = this.usuariosCargados();
            this.form.get('usuariosAfectados')?.setValue(JSON.stringify(beneficiarios));
        }
    }

    removeBeneficiario(index: number): void {
        this.usuariosCargados.update(list => {
            const removed = list[index];
            const newList = list.filter((_, i) => i !== index);
            if (removed) {
                this.selectedUserIds.update(ids => ids.filter(id => id !== Number(removed.id)));
            }
            return newList;
        });
        this.syncUsuariosAfectados();
    }

    updateBeneficiarioType(index: number, type: string): void {
        this.usuariosCargados.update(list => {
            const newList = [...list];
            newList[index] = { ...newList[index], tipo: type };
            return newList;
        });
        this.syncUsuariosAfectados();
    }

    setSegmentMode(mode: 'ALL' | 'MANUAL' | 'EXCEL' | 'CONVENIO'): void {
        this.selectedSegmentMode.set(mode);
        if (mode === 'ALL') {
            this.form.get('usuariosAfectados')?.setValue('ALL');
            this.usuariosCargados.set([]);
            this.selectedUserIds.set([]);
            this.selectedCompaniaId.set(null);
        } else if (mode === 'CONVENIO') {
            this.loadCompanias();
        } else {
            this.syncUsuariosAfectados();
        }
    }

    loadCompanias(): void {
        this.loadingCompanias.set(true);
        this.convenioService.listarCompanias().subscribe({
            next: (res) => {
                const data = res?.data || res || [];
                this.companias.set(Array.isArray(data) ? data : (data.content || []));
                this.loadingCompanias.set(false);
                if (this.selectedCompaniaId()) {
                    this.syncUsuariosAfectados();
                }
            },
            error: () => {
                this.loadingCompanias.set(false);
                this.alertService.error('Error al cargar compañías');
            }
        });
    }

    onCompaniaSelected(compania: any): void {
        if (!compania) return;
        this.selectedCompaniaId.set(compania.id);
        this.syncUsuariosAfectados();
    }

    onProductSelected(producto: any) {
        if (!producto) return;
        const exists = this.selectedDetails().some(p => p.producto.idCatalogo === producto.idCatalogo);
        if (!exists) {
            const idSucursal = this.authService.getSucursalIdFromToken() || 1;
            this.productoService.listarPorCatalogoYSucursal(producto.idCatalogo, idSucursal).subscribe(res => {
                let lotes: Producto[] = [];
                if (res.success && res.data && res.data.length > 0) {
                    lotes = res.data.filter(p => (p.precioVentaUnitario || 0) > 0 && !this.isExpired(p.fechaVencimiento));
                    if (lotes.length === 0) lotes.push(res.data[0]);
                } else {
                    lotes.push(producto as Producto); // fallback
                }

                const lotesValidados = lotes.map(lote => {
                    const cal = this.calcularDescuentoProducto(lote, TipoDescuento.PORCENTAJE, 0, 1);
                    return {
                        producto: lote,
                        ...cal
                    };
                });

                this.selectedDetails.update(list => [...list, {
                    producto: producto as Producto,
                    tipoDescuento: TipoDescuento.PORCENTAJE,
                    valorDescuento: null as any,
                    cantidad: 1,
                    valido: lotesValidados.every(l => l.valido),
                    lotesValidados: lotesValidados
                }]);

                // Limpiar selección del buscador
                if (this.productSearch) {
                    this.productSearch.clearSelection();
                }
            });
        }
    }

    removeProduct(index: number) {
        this.selectedDetails.update(list => list.filter((_, i) => i !== index));
    }

    openLotesModal(index: number) {
        const item = this.selectedDetails()[index];
        this.lotesModalTitle.set(item.producto.catalogo?.nombre || 'Producto');
        this.lotesValidadosSeleccionados.set(item.lotesValidados);
        this.showLotesModal.set(true);
    }

    trackByIndex(index: number, item: any): number {
        return index;
    }

    closeLotesModal() {
        this.showLotesModal.set(false);
    }

    onDetailChange(index: number, field: string, value: any) {
        this.selectedDetails.update(list => {
            const newList = [...list];
            const item = { ...newList[index], [field]: value };

            // Recalcular si cambió tipo o valor
            if (field === 'tipoDescuento' || field === 'valorDescuento' || field === 'cantidad') {
                const lotesValidados = item.lotesValidados.map(lote => {
                    const cal = this.calcularDescuentoProducto(lote.producto, item.tipoDescuento, item.valorDescuento, item.cantidad);
                    return {
                        producto: lote.producto,
                        ...cal
                    };
                });
                item.lotesValidados = lotesValidados;
                item.valido = lotesValidados.every(l => l.valido);
                newList[index] = item;
            } else {
                newList[index] = item;
            }
            return newList;
        });
    }

    recalcularTodo() {
        const isGlobal = this.form.get('tipoAlcance')?.value === 'GLOBAL';

        if (isGlobal) {
            if (this.validarGlobal()) {
                const detail = this.globalModeDetail();
                const invalidProducts = this.productos().filter(prod => {
                    const calc = this.calcularDescuentoProducto(prod, detail.tipoDescuento, detail.valorDescuento || 0, detail.cantidad || 0);
                    return !calc.valido;
                });

                if (invalidProducts.length > 0) {
                    console.group('Productos con Conflicto de Margen (Validación Global)');
                    invalidProducts.forEach(p => {
                        const calc = this.calcularDescuentoProducto(p, detail.tipoDescuento, detail.valorDescuento || 0, detail.cantidad || 0);
                        console.log(`Nombre: ${(p as any).nombre || p.catalogo?.nombre} | Lote: ${p.nroLote || 'N/A'} | Venc: ${p.fechaVencimiento || 'N/A'} | Precio Compra: S/ ${p.precioCompra} | Precio Venta Base: S/ ${p.precioVentaUnitario} | Tipo Descuento: ${detail.tipoDescuento} | Valor: ${detail.valorDescuento} | Precio Final con Descuento: S/ ${calc.precioFinal}`);
                    });
                    console.groupEnd();

                    this.isGlobalValidated.set(false);
                    const names = invalidProducts.slice(0, 3).map(p => (p as any).nombre || p.catalogo?.nombre).join(', ');
                    const suffix = invalidProducts.length > 3 ? ` y ${invalidProducts.length - 3} más` : '';
                    this.alertService.error('Error de Margen', `El descuento global no se puede aplicar porque generaría margen negativo en: ${names}${suffix}.`);
                } else {
                    this.isGlobalValidated.set(true);
                    this.alertService.success('Validación', 'Configuración de descuento global validada correctamente.');
                }
            } else {
                this.isGlobalValidated.set(false);
            }
            return;
        }

        this.selectedDetails.update(list => {
            return list.map(item => {
                const calculo = this.calcularDescuentoProducto(item.producto, item.tipoDescuento, item.valorDescuento, item.cantidad);
                return { ...item, ...calculo };
            });
        });
        this.alertService.success('Cálculo', 'Márgenes recalculados correctamente.');
    }

    private validarGlobal(): boolean {
        const detail = this.globalModeDetail();
        
        if (detail.tipoDescuento === TipoDescuento.PORCENTAJE) {
            if (!detail.valorDescuento || detail.valorDescuento <= 0 || detail.valorDescuento > 100) {
                this.alertService.error('Error de Validación', 'El porcentaje de descuento debe estar entre 1 y 100.');
                return false;
            }
        } else if (detail.tipoDescuento === TipoDescuento.MONTO_FIJO) {
            if (!detail.valorDescuento || detail.valorDescuento <= 0) {
                this.alertService.error('Error de Validación', 'El monto de descuento debe ser mayor a 0.');
                return false;
            }
        } else if (detail.tipoDescuento === TipoDescuento.CANTIDAD) {
            if (!detail.cantidad || detail.cantidad <= 0) {
                this.alertService.error('Error de Validación', 'La cantidad "Lleva" debe ser mayor a 0.');
                return false;
            }
            if (!detail.valorDescuento || detail.valorDescuento <= 0) {
                this.alertService.error('Error de Validación', 'La cantidad "Regala" debe ser mayor a 0.');
                return false;
            }
            if (detail.cantidad <= detail.valorDescuento) {
                this.alertService.error('Error de Validación', 'La cantidad "Lleva" debe ser mayor a la cantidad "Regala" para que exista un cobro.');
                return false;
            }
        }
        
        return true;
    }

    calcularDescuentoProducto(producto: Producto, tipo: string, valor: number, cantidad: number = 1) {
        const precioUnitario = producto.precioVentaUnitario || 0;
        const costo = producto.precioCompra || 0;
        let precioFinal = precioUnitario;

        if (tipo === TipoDescuento.PORCENTAJE) {
            precioFinal = precioUnitario - (precioUnitario * (valor / 100));
        } else if (tipo === TipoDescuento.MONTO_FIJO) {
            precioFinal = precioUnitario - valor;
        } else if (tipo === TipoDescuento.CANTIDAD) {
            // "Lleva X Regala Y" -> precioFinal por unidad = (PrecioBase * (X - Y)) / X
            if (cantidad > 0) {
                precioFinal = (precioUnitario * (cantidad - (valor || 0))) / cantidad;
            }
        }

        const ganancia = precioFinal - costo;
        return {
            precioFinal,
            ganancia,
            valido: precioFinal >= costo
        };
    }

    onGlobalDetailChange(field: string, value: any) {
        this.isGlobalValidated.set(false);
        this.globalModeDetail.update(current => ({
            ...current,
            [field]: value === '' ? null : (field === 'tipoDescuento' ? value : Number(value))
        }));
    }

    isSaveDisabled(): boolean {
        const isGlobal = this.form.get('tipoAlcance')?.value === 'GLOBAL';
        if (isGlobal) {
            return !this.isGlobalValidated();
        } else {
            return this.selectedDetails().length === 0 || this.selectedDetails().some(p => !p.valido);
        }
    }

    onSubmit(): void {
        const formValue = this.form.value;
        const isGlobal = formValue.tipoAlcance === 'GLOBAL';

        if (!isGlobal && this.selectedDetails().length === 0) {
            this.alertService.error('Error de Validación', 'Debe agregar al menos un producto al descuento personalizado.');
            return;
        }

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        if (isGlobal) {
            if (!this.validarGlobal()) {
                return;
            }
        }

        if (this.selectedSegmentMode() === 'EXCEL' && this.usuariosCargados().length === 0) {
            this.alertService.error('Error de Validación', 'Debe importar al menos un usuario mediante Excel si tiene seleccionada la segmentación por Excel.');
            return;
        }

        if (this.selectedSegmentMode() === 'MANUAL' && this.usuariosCargados().length === 0) {
            this.alertService.error('Error de Validación', 'Debe seleccionar al menos un usuario si tiene seleccionada la segmentación Manual.');
            return;
        }

        // Validación estricta de márgenes (Bloquea el guardado)
        let invalidProducts: any[] = [];
        if (!isGlobal) {
            invalidProducts = this.selectedDetails().filter(p => !p.valido).map(p => p.producto);
        } else {
            const detail = this.globalModeDetail();
            invalidProducts = this.productos().filter(prod => {
                const calc = this.calcularDescuentoProducto(prod, detail.tipoDescuento, detail.valorDescuento || 0, detail.cantidad || 0);
                return !calc.valido;
            });
        }

        if (invalidProducts.length > 0) {
            console.group('Productos con Conflicto de Margen (Intento de Guardado)');
            
            if (!isGlobal) {
                // Modo Personalizado
                const invalidDetails = this.selectedDetails().filter(d => !d.valido);
                invalidDetails.forEach(d => {
                    const p = d.producto;
                    console.log(`Nombre: ${(p as any).nombre || p.catalogo?.nombre} | Lote: ${p.nroLote || 'N/A'} | Venc: ${p.fechaVencimiento || 'N/A'} | Precio Compra: S/ ${p.precioCompra} | Precio Venta Base: S/ ${p.precioVentaUnitario} | Tipo Descuento: ${d.tipoDescuento} | Valor: ${d.valorDescuento}`);
                });
            } else {
                // Modo Global
                const detail = this.globalModeDetail();
                invalidProducts.forEach(p => {
                    const calc = this.calcularDescuentoProducto(p, detail.tipoDescuento, detail.valorDescuento || 0, detail.cantidad || 0);
                    console.log(`Nombre: ${(p as any).nombre || p.catalogo?.nombre} | Lote: ${p.nroLote || 'N/A'} | Venc: ${p.fechaVencimiento || 'N/A'} | Precio Compra: S/ ${p.precioCompra} | Precio Venta Base: S/ ${p.precioVentaUnitario} | Tipo Descuento: ${detail.tipoDescuento} | Valor: ${detail.valorDescuento} | Precio Final con Descuento: S/ ${calc.precioFinal}`);
                });
            }
            console.groupEnd();

            const names = invalidProducts.slice(0, 3).map(p => (p as any).nombre || p.catalogo?.nombre).join(', ');
            const suffix = invalidProducts.length > 3 ? ` y ${invalidProducts.length - 3} más` : '';
            this.alertService.error(
                'Guardado Bloqueado por Margen Negativo',
                isGlobal 
                    ? `El descuento global no se puede guardar porque generaría precios finales por debajo del costo en: ${names}${suffix}.`
                    : `No se puede guardar porque los siguientes productos tienen margen negativo: ${names}${suffix}.`
            );
            return;
        }

        this.processSave();
    }

    private processSave(): void {
        const formValue = this.form.value;
        const isGlobal = formValue.tipoAlcance === 'GLOBAL';

        this.loading.set(true);

        const detalles = isGlobal
            ? [{
                tipoDescuento: this.globalModeDetail().tipoDescuento,
                valorDescuento: this.globalModeDetail().valorDescuento || 0,
                cantidad: (this.globalModeDetail().tipoDescuento === TipoDescuento.CANTIDAD)
                    ? (this.globalModeDetail().cantidad || 0)
                    : 1
            }]
            : this.selectedDetails().map(d => ({
                    idCatalogo: d.producto.idCatalogo,
                    tipoDescuento: d.tipoDescuento,
                    valorDescuento: d.valorDescuento,
                    cantidad: d.cantidad || 1
                }));

        const isConvenio = this.selectedSegmentMode() === 'CONVENIO';
        const idCompania = isConvenio ? this.selectedCompaniaId() : (this.idCompaniaPreset || undefined);

        let usuariosAfectados = 'ALL';
        if (isConvenio) {
            const comp = this.companias().find(c => c.id === this.selectedCompaniaId());
            usuariosAfectados = JSON.stringify({
                mode: 'CONVENIO',
                idCompania: this.selectedCompaniaId(),
                nombreCompania: comp?.nombre || 'Compañía'
            });
        } else if (this.selectedSegmentMode() !== 'ALL') {
            usuariosAfectados = JSON.stringify(this.usuariosCargados());
        }

        const request: DescuentoRequest = {
            ...formValue,
            idCompania: idCompania as any,
            usuariosAfectados: usuariosAfectados,
            fechaInicio: formValue.fechaInicio + 'T00:00:00',
            fechaFin: formValue.fechaFin + 'T23:59:59',
            detalles: detalles
        };

        delete (request as any).usuariosAfectados;
        (request as any).usuariosAfectados = usuariosAfectados;

        const obs$ = this.descuento?.id
            ? this.descuentoService.actualizar(this.descuento.id, request)
            : this.descuentoService.crear(request);

        obs$.subscribe({
            next: (res) => {
                this.alertService.success('Éxito', 'Descuento guardado correctamente');
                this.guardado.emit();
                this.loading.set(false);
            },
            error: (err) => {
                this.alertService.error('Error', err.error?.message || 'Error al guardar');
                this.loading.set(false);
            }
        });
    }

    isFieldInvalid(field: string): boolean {
        const control = this.form.get(field);
        return !!(control && control.invalid && (control.dirty || control.touched));
    }

    onFileChange(evt: any) {
        const target: DataTransfer = <DataTransfer>(evt.target);
        if (target.files.length !== 1) return;

        const reader: FileReader = new FileReader();
        reader.onload = (e: any) => {
            const bstr: string = e.target.result;
            const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });
            const wsname: string = wb.SheetNames[0];
            const ws: XLSX.WorkSheet = wb.Sheets[wsname];
            const data = (XLSX.utils.sheet_to_json(ws, { header: 1 }));
            this.processExcelData(data);
        };
        reader.readAsBinaryString(target.files[0]);
    }

    private processExcelData(data: any[]) {
        if (!data || data.length === 0) {
            this.alertService.error('Error', 'El archivo Excel está vacío.');
            return;
        }

        // Obtener cabeceras en minúsculas y limpias
        const cabeceras = (data[0] || []).map((c: any) => String(c).toLowerCase().trim());
        const tieneValidDni = cabeceras.includes('dni') || cabeceras.includes('documento') || cabeceras.includes('ruc');

        if (!tieneValidDni) {
            this.alertService.error('Formato inválido', 'El archivo Excel debe contener una columna de cabecera llamada "DNI", "Documento" o "RUC" en la primera fila.');
            return;
        }

        const colIndexDni = cabeceras.findIndex((c: any) => c === 'dni' || c === 'documento' || c === 'ruc');
        const colIndexNombre = cabeceras.findIndex((c: any) => c === 'nombre' || c === 'nombre completo' || c === 'paciente');
        const colIndexTipo = cabeceras.findIndex((c: any) => c === 'tipo' || c === 'afiliacion' || c === 'relacion');

        const importedUsers: any[] = [];
        // Ignorar cabecera
        const rows = data.slice(1);

        rows.forEach(row => {
            const dniVal = row[colIndexDni];
            if (dniVal) {
                const nombreVal = colIndexNombre !== -1 ? row[colIndexNombre] : 'Importado';
                const tipoVal = colIndexTipo !== -1 ? row[colIndexTipo] : 'DIRECTO';

                importedUsers.push({
                    id: String(dniVal),
                    dni: String(dniVal),
                    nombre: nombreVal || 'Importado',
                    tipo: tipoVal || 'DIRECTO'
                });
            }
        });

        if (importedUsers.length > 0) {
            this.usuariosCargados.set(importedUsers);
            this.selectedUserIds.set(importedUsers.map(u => Number(u.id)).filter(id => !isNaN(id)));
            this.syncUsuariosAfectados();
            this.alertService.success('Importación', `${importedUsers.length} usuarios cargados correctamente.`);
        } else {
            this.alertService.error('Error', 'No se encontraron registros válidos en el archivo Excel.');
        }
    }

    cancelImport() {
        this.importReport.set(null);
        this.isValidatingExcel.set(false);
    }

    descargarPlantillaExcel(): void {
        const headers = [['DNI', 'Nombre', 'Tipo']];
        const data = [
            ['77777771', 'Carlos Rodriguez', 'DIRECTO'],
            ['77777772', 'Ana Fernandez', 'FAMILIAR']
        ];

        const worksheet = XLSX.utils.aoa_to_sheet([...headers, ...data]);
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Plantilla Beneficiarios');

        // Generar archivo y forzar descarga
        XLSX.writeFile(workbook, 'plantilla_descuento_beneficiarios.xlsx');
    }
}
