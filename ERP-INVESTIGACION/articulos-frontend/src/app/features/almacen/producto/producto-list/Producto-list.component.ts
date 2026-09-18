import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../auth/services/auth.service';
import { AlertService } from '../../../../core/services/alert.service';
import { ProductoServicioDetailComponent } from '../producto-detail/producto-detail.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { ProductoServicioFormComponent } from '../producto-form/Producto-form.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { ProductoService } from '../../service/producto.service';
import { Producto } from '../../models/producto.model';
import { SelectOption } from '../../../../shared/components/forms/form-select/form-select.component';
import { UserService } from '../../../../core/services/user.service';
import { DrawerComponent } from '../../../../shared/components/drawer/drawer.component';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';
import { FormSwitchComponent } from '../../../../shared/components/forms/form-switch/form-switch.component';

export interface ProductoGroup {
  idCatalogo: number;
  catalogo: any;
  detalles: Producto[];
  stockTotal: number;
  precioVentaUnitario: number; // Referencial (el de la primera fila o promedio)
  expanded?: boolean;
}

@Component({
  selector: 'app-producto-servicio-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProductoServicioFormComponent,
    ProductoServicioDetailComponent,
    HeaderComponent,
    SidebarComponent,
    PrimaryButtonComponent,
    PageHeaderComponent,
    ModalComponent,
    BreadcrumbComponent,
    SearchGenericComponent,
    PaginationComponent,
    DrawerComponent,
    FormInputComponent,
    FormSelectComponent,
    FormSwitchComponent,
    TablaGeneralComponent
  ],
  templateUrl: './producto-list.component.html'
})
export class ProductoServicioListComponent implements OnInit, OnDestroy {
  productos = signal<Producto[]>([]);
  productosAgrupados = signal<ProductoGroup[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  totalPagesBackend = signal(0);

  tabs = [
    { id: 'POR_PRODUCTO', label: 'Por Producto' },
    { id: 'POR_VENCIMIENTO', label: 'Por Fecha de Vencimiento' },
    { id: 'DISPONIBILIDAD', label: 'Disponibilidad' }
  ];
  activeTab = signal('POR_PRODUCTO');
  
  // Sub-tabs para Disponibilidad
  disponibilidadSubTab = signal<'LISTO' | 'SIN_PRECIO'>('SIN_PRECIO');

  setActiveTab(tabId: string): void {
    this.activeTab.set(tabId);
    this.currentPageBackend.set(0);
    this.cargarProductos();
  }

  setDisponibilidadSubTab(subTab: 'LISTO' | 'SIN_PRECIO'): void {
    this.disponibilidadSubTab.set(subTab);
    this.currentPageBackend.set(0);
    this.cargarProductos();
  }

  // Paginación de Lotes interna (por catálogo)
  lotPageMap = signal<Record<number, number>>({}); 

  // Paginación de Catálogos (Frontend)
  pageSizeCatalogo = signal(10);
  
  pagedGroups = computed(() => {
    const start = this.currentPageCatalogo() * this.pageSizeCatalogo();
    const end = start + this.pageSizeCatalogo();
    return this.productosAgrupados().slice(start, end);
  });

  totalCatalogs = computed(() => this.productosAgrupados().length);
  totalPagesCatalogs = computed(() => Math.ceil(this.totalCatalogs() / this.pageSizeCatalogo()));

  // Filtros Avanzados
  usuarioCrea = signal('TODOS');
  fechaDesde = signal('');
  fechaHasta = signal('');
  rotaMas = signal(false);
  queEntra = signal(false);
  cercaVencer = signal(false);
  
  usuarioOptions = signal<SelectOption[]>([]);
  isDrawerOpen = signal(false);

  hasActiveFilters = computed(() => {
    return this.usuarioCrea() !== 'TODOS' ||
           this.fechaDesde() !== '' ||
           this.fechaHasta() !== '' ||
           this.rotaMas() ||
           this.queEntra() ||
           this.cercaVencer();
  });

  activeFiltersLabels = computed(() => {
    const labels: {key: string, label: string}[] = [];
    if (this.usuarioCrea() !== 'TODOS') labels.push({key: 'usuarioCrea', label: `Creador: ${this.usuarioCrea()}`});
    if (this.fechaDesde()) labels.push({key: 'fechaDesde', label: `Desde: ${this.fechaDesde()}`});
    if (this.fechaHasta()) labels.push({key: 'fechaHasta', label: `Hasta: ${this.fechaHasta()}`});
    if (this.rotaMas()) labels.push({key: 'rotaMas', label: 'Más Rotación'});
    if (this.queEntra()) labels.push({key: 'queEntra', label: 'Ingresos Recientes'});
    if (this.cercaVencer()) labels.push({key: 'cercaVencer', label: 'Próximos a Vencer'});
    return labels;
  });
  public Math = Math;

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Almacén', route: '/almacen' },
    { label: 'Productos y Servicios' }
  ];

  // Paginación
  currentPageBackend = signal(0);
  currentPageCatalogo = signal(0);
  pageSizeBackend = signal(10); 
  pageSize = signal(100); 

  // Filtros
  searchTerm = signal('');
  searchType = signal('ALL');

  // Modal
  showModal = signal(false);
  showDetailModal = signal(false);
  selectedProducto = signal<Producto | null>(null);

  // Configuración de Tabla
  columns: Columna[] = [
    { field: 'catalogo', header: 'Código', tipo: 'object', subField: ['codigo'] },
    { field: 'catalogo', header: 'Producto', tipo: 'object', subField: ['nombre'] },
    { field: 'presentacion', header: 'Presentacion', tipo: 'text', subField: [] },
    { field: 'laboratorio', header: 'Laboratorio', tipo: 'text', subField: [] },
    { field: 'catalogo', header: 'P. Activo', tipo: 'object', subField: ['principioActivo'] },
    { field: 'stock', header: 'Stock', tipo: 'text', subField: [] },
    { field: 'precioVentaUnitario', header: 'Venta Unidad', tipo: 'currency', subField: [] },
    { field: 'nroLote', header: 'Lote', tipo: 'text', subField: [] },
    {
      field: 'fechaVencimiento',
      header: 'Vto',
      tipo: 'expiration-clock',
      subField: []
    },
    { field: 'estado', header: 'Estado', tipo: 'status', subField: [] },
  ];

  columnsVencimiento: Columna[] = [
    { 
      field: 'catalogoNombre', 
      header: 'Producto', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'catalogoNombre', class: 'font-bold text-slate-800 dark:text-white' },
        { field: 'catalogoCodigo', class: 'text-[10px] text-slate-500' }
      ],
      subField: []
    },
    { field: 'nroLote', header: 'Lote', tipo: 'text', subField: [] },
    { field: 'fechaVencimiento', header: 'F. Vencimiento', tipo: 'date', subField: [] },
    { 
      field: 'stock', 
      header: 'Stock Actual', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'stock', class: 'font-black text-blue-600 dark:text-blue-400', suffix: ' UND' }
      ], 
      subField: [] 
    },
    { field: 'estadoVencimiento', header: 'Estado', tipo: 'status', subField: [] }
  ];

  columnsDisponibilidadSinPrecio: Columna[] = [
    { 
      field: 'catalogoNombre', 
      header: 'Producto', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'catalogoNombre', class: 'font-bold text-slate-800 dark:text-white' },
        { field: 'catalogoCodigo', class: 'text-[10px] text-slate-500' }
      ],
      subField: []
    },
    { field: 'nroLote', header: 'Lote', tipo: 'text', subField: [] },
    { 
      field: 'stock', 
      header: 'Stock Actual', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'stock', class: 'font-black text-rose-600 dark:text-rose-400', suffix: ' UND' }
      ], 
      subField: [] 
    }
  ];

  columnsDisponibilidadListos: Columna[] = [
    { 
      field: 'catalogoNombre', 
      header: 'Producto', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'catalogoNombre', class: 'font-bold text-slate-800 dark:text-white' },
        { field: 'catalogoCodigo', class: 'text-[10px] text-slate-500' }
      ],
      subField: []
    },
    { field: 'nroLote', header: 'Lote', tipo: 'text', subField: [] },
    { 
      field: 'stock', 
      header: 'Stock Actual', 
      tipo: 'layered-info', 
      layeredConfig: [
        { field: 'stock', class: 'font-black text-emerald-600 dark:text-emerald-400', suffix: ' UND' }
      ], 
      subField: [] 
    },
    { field: 'precioVentaUnitario', header: 'Precio Venta', tipo: 'currency', subField: [] }
  ];

  searchOptions = [
    { label: 'Todo', value: 'ALL' },
    { label: 'Nombre', value: 'NOMBRE' },
    { label: 'Código', value: 'CODIGO' },
    { label: 'Laboratorio', value: 'LABORATORIO' }
  ];

  private sucursalSubscription: any;

  constructor(
    private productoService: ProductoService,
    public sidebarService: SidebarService,
    private authService: AuthService,
    private alertService: AlertService,
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cargarVendedores();
    this.cargarProductos();

    // Recargar lista cuando cambie la sucursal
    this.sucursalSubscription = this.authService.onSucursalChanged$.subscribe(() => {
      console.log('🔄 [PRODUCTOS] Detectado cambio de sucursal, recargando inventario...');
      this.cargarProductos();
    });
  }

  cargarVendedores(): void {
    const sucursalId = this.authService.getSucursalIdFromToken();
    if (!sucursalId) return;

    this.productoService.getVendedores(sucursalId).subscribe({
      next: (res) => {
        if (res && res.success && res.data) {
          const usuarios = res.data.map((username: any) => ({
            label: username,
            value: username
          }));
          this.usuarioOptions.set([
            { label: 'TODOS', value: 'TODOS' },
            ...usuarios
          ]);
        }
      }
    });
  }

  cargarProductos(): void {
    this.loading.set(true);
    this.error.set(null);

    const pageParams: any = {
      page: this.currentPageBackend(),
      size: this.activeTab() === 'POR_PRODUCTO' ? this.pageSize() : this.pageSizeBackend()
    };

    if (this.activeTab() === 'POR_VENCIMIENTO') {
      pageParams.sortBy = 'fechaVencimiento';
      pageParams.direction = 'ASC';
    } else if (this.activeTab() === 'DISPONIBILIDAD') {
      if (this.disponibilidadSubTab() === 'SIN_PRECIO') {
        pageParams.faltanPrecio = true;
      } else {
        pageParams.listosVender = true;
      }
    }

    let observable;

    // Use searchAdvanced when on special tabs OR if filters are active
    if (this.hasActiveFilters() || this.activeTab() !== 'POR_PRODUCTO') {
      const filters = {
        idSucursal: this.authService.getSucursalIdFromToken(),
        usuarioCrea: this.usuarioCrea() === 'TODOS' ? null : this.usuarioCrea(),
        fechaDesde: this.fechaDesde(),
        fechaHasta: this.fechaHasta(),
        rotaMas: this.rotaMas(),
        queEntra: this.queEntra(),
        cercaVencer: this.cercaVencer(),
        ...pageParams
      };
      observable = this.productoService.searchAdvanced(filters);
    } else {
      if (this.searchTerm()) {
        observable = this.productoService.buscar(
          this.searchTerm(),
          this.currentPageBackend(),
          this.activeTab() === 'POR_PRODUCTO' ? this.pageSize() : this.pageSizeBackend(),
          this.authService.getSucursalIdFromToken()!,
          this.searchType() === 'ALL' ? undefined : this.searchType()
        );
      } else {
        observable = this.productoService.listarPorSucursal(
          this.authService.getSucursalIdFromToken()!,
          this.currentPageBackend(),
          this.activeTab() === 'POR_PRODUCTO' ? this.pageSize() : this.pageSizeBackend()
        );
      }
    }

    observable.subscribe({
      next: (response) => {
        if (response.success) {
          const flatList = response.data.content || response.data;
          
          if (response.data.totalPages !== undefined) {
            this.totalPagesBackend.set(response.data.totalPages);
          } else {
             this.totalPagesBackend.set(Math.ceil((flatList.length || 0) / (this.activeTab() === 'POR_PRODUCTO' ? this.pageSize() : this.pageSizeBackend())));
          }

          console.group('📦 [DEBUG LISTA] Datos Recibidos del Servidor');
          flatList.forEach((p: any, idx: number) => {
            console.log(`[${idx}] Producto: ${p.catalogo?.nombre} | Lote: ${p.nroLote} | Precio Unit: ${p.precioVentaUnitario}`);
          });
          console.groupEnd();

          const mappedFlatList = flatList.map((p: any) => ({
            ...p,
            catalogoNombre: p.catalogo?.nombre || 'SIN NOMBRE',
            catalogoCodigo: p.catalogo?.codigo || 'SIN CÓDIGO',
            estadoVencimiento: this.getVencimientoStatus(p)
          }));

          this.productos.set(mappedFlatList);
          this.procesarProductosAgrupados(mappedFlatList);
        }
        this.loading.set(false);
      },
      error: (err) => {
        const errorMsg = 'Error al cargar los productos: ' + err.message;
        this.error.set(errorMsg);
        this.mostrarError(errorMsg);
        this.loading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.sucursalSubscription) {
      this.sucursalSubscription.unsubscribe();
    }
  }

  onFiltrar(): void {
    this.currentPageBackend.set(0);
    this.currentPageCatalogo.set(0);
    this.isDrawerOpen.set(false);
    this.cargarProductos();
  }

  limpiarFiltros(): void {
    this.usuarioCrea.set('TODOS');
    this.fechaDesde.set('');
    this.fechaHasta.set('');
    this.rotaMas.set(false);
    this.queEntra.set(false);
    this.cercaVencer.set(false);
    this.currentPageBackend.set(0);
    this.currentPageCatalogo.set(0);
    this.cargarProductos();
  }

  eliminarFiltro(key: string): void {
    switch (key) {
      case 'usuarioCrea': this.usuarioCrea.set('TODOS'); break;
      case 'fechaDesde': this.fechaDesde.set(''); break;
      case 'fechaHasta': this.fechaHasta.set(''); break;
      case 'rotaMas': this.rotaMas.set(false); break;
      case 'queEntra': this.queEntra.set(false); break;
      case 'cercaVencer': this.cercaVencer.set(false); break;
    }
    this.currentPageBackend.set(0);
    this.currentPageCatalogo.set(0);
    this.cargarProductos();
  }

  // Manejo de eventos del DataTable
  onSearch(event: { q: string, type: string }): void {
    this.searchTerm.set(event.q);
    this.searchType.set(event.type);
    this.currentPageBackend.set(0);
    this.currentPageCatalogo.set(0);
    this.cargarProductos();
  }

  cambiarPagina(page: number): void {
    if (this.activeTab() === 'POR_PRODUCTO') {
      this.currentPageCatalogo.set(page);
    } else {
      this.currentPageBackend.set(page);
      this.cargarProductos();
    }
  }

  cambiarTamanoPagina(size: number): void {
    this.pageSizeCatalogo.set(Number(size));
    this.currentPageCatalogo.set(0);
  }

  cambiarTamanoPaginaBackend(size: number): void {
    this.pageSizeBackend.set(Number(size));
    this.currentPageBackend.set(0);
    this.cargarProductos();
  }

  abrirModalNuevo(): void {
    this.selectedProducto.set(null);
    this.showModal.set(true);
  }

  abrirModalEditar(producto: Producto): void {
    this.selectedProducto.set(producto);
    this.showModal.set(true);
  }

  cerrarModal(): void {
    this.showModal.set(false);
    this.selectedProducto.set(null);
  }

  onGuardado(): void {
    this.cerrarModal();
    this.mostrarExito('Operación realizada con éxito');
    this.cargarProductos();
  }

  abrirModalDetalle(producto: Producto): void {
    this.selectedProducto.set(producto);
    this.showDetailModal.set(true);
  }

  cerrarModalDetalle(): void {
    this.showDetailModal.set(false);
    this.selectedProducto.set(null);
  }

  eliminar(producto: Producto): void {
    if (!producto.idProducto) return;

    this.alertService.confirm(
      '¿Eliminar producto?',
      `¿Estás seguro de eliminar "${producto.catalogo?.nombre}"?`,
      'Eliminar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) {
        this.productoService.eliminar(producto.idProducto!).subscribe({
          next: (response) => {
            if (response.success) {
              this.mostrarExito('Producto eliminado correctamente');
              this.cargarProductos();
            }
          },
          error: (err) => {
            this.mostrarError('Error al eliminar: ' + err.message);
          }
        });
      }
    });
  }

  // Lógica de Paginación Interna de Lotes
  getLotPage(idCatalogo: number): number {
    return this.lotPageMap()[idCatalogo] || 0;
  }

  setLotPage(idCatalogo: number, page: number): void {
    this.lotPageMap.update(map => ({ ...map, [idCatalogo]: page }));
  }

  getLotsForGroup(group: ProductoGroup): Producto[] {
    const page = this.getLotPage(group.idCatalogo);
    const start = page * 10;
    return group.detalles.slice(start, start + 10);
  }

  getTotalLotPages(group: ProductoGroup): number {
    return Math.ceil(group.detalles.length / 10);
  }

  private mostrarExito(mensaje: string): void {
    this.alertService.toast(mensaje, 'success');
  }

  private mostrarError(mensaje: string): void {
    this.alertService.error('Error', mensaje);
  }

  procesarProductosAgrupados(productos: Producto[]): void {
    const groupsMap = new Map<number, ProductoGroup>();

    console.group('🔄 [DEBUG AGRUPACIÓN] Mapeo de Lotes');
    productos.forEach(p => {
      if ((p.stock || 0) <= 0) {
        return;
      }
      console.log(`Procesando lote ${p.nroLote} de ${p.catalogo?.nombre}. Precio actual: ${p.precioVentaUnitario}`);
      
      const idCat = p.idCatalogo;
      if (!groupsMap.has(idCat)) {
        groupsMap.set(idCat, {
          idCatalogo: idCat,
          catalogo: p.catalogo,
          detalles: [],
          stockTotal: 0,
          precioVentaUnitario: p.precioVentaUnitario || 0,
          expanded: false
        });
      }

      const group = groupsMap.get(idCat)!;
      group.detalles.push(p);
      group.stockTotal += (p.stock || 0);
    });
    console.groupEnd();

    const agrupadoss = Array.from(groupsMap.values());
    
    // ORDENAR: Fecha de vencimiento más cercano primero (menor a mayor)
    agrupadoss.forEach(group => {
      group.detalles.sort((a, b) => {
        if (!a.fechaVencimiento) return 1;
        if (!b.fechaVencimiento) return -1;
        return new Date(a.fechaVencimiento).getTime() - new Date(b.fechaVencimiento).getTime();
      });
    });

    this.productosAgrupados.set(agrupadoss);
  }

  toggleGroup(groupId: number): void {
    this.productosAgrupados.update(groups =>
      groups.map(g => g.idCatalogo === groupId ? { ...g, expanded: !g.expanded } : g)
    );
  }

  getDaysRemaining(dateString: any): number {
    if (!dateString) return 999;
    const date = new Date(dateString);
    const today = new Date();
    date.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);
    const diffTime = date.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getVencimientoStatus(det: any): 'NORMAL' | 'ALERTA' | 'PELIGRO' | 'VENCIDO' {
    if (!det.fechaVencimiento) return 'NORMAL';
    const diasFaltantes = this.getDaysRemaining(det.fechaVencimiento);
    const diasAlerta = det.diasAlertaVencimiento || 90;
    
    if (diasFaltantes < 0) {
      return 'VENCIDO'; // Pasó la fecha de vencimiento
    } else if (diasFaltantes <= (diasAlerta * 0.3) || diasFaltantes === 0) {
      return 'PELIGRO';
    } else if (diasFaltantes <= diasAlerta) {
      return 'ALERTA'; // Vence pronto
    }
    return 'NORMAL'; // Vigente
  }

  hacerSalidaPorVencimiento(det: Producto): void {
    this.alertService.toast('Redirigiendo a Salida Diversa...', 'info');
    this.router.navigate(['/procesos/nuevo-movimiento'], {
      queryParams: {
        tipo: 'SALIDA',
        idCatalogo: det.idCatalogo,
        nroLote: det.nroLote || '',
        idAlmacen: det.idAlmacen || '',
        cantidad: det.stock || 0,
        idProducto: det.idProducto || det.id || ''
      }
    });
  }
  
  getProfitLabel(det: Producto): string {
    const cost = det.precioCompra || 0;
    const sale = det.precioVentaUnitario || 0;

    if (cost > 0 && sale > 0) {
      // Margen sobre el Precio de Venta: (Venta - Costo) / Venta
      const margin = ((sale - cost) / sale) * 100;
      return `${Math.round(margin)}%`;
    }

    if (det.gananciaUnidad === undefined || det.gananciaUnidad === null) return '';
    if (det.tipoGananciaUnidad === 'PORCENTAJE') {
      return `${det.gananciaUnidad}%`;
    }
    return `S/ ${Number(det.gananciaUnidad).toFixed(2)}`;
  }

  get paginasArray(): number[] {
    return Array.from({ length: this.totalPagesCatalogs() }, (_, i) => i);
  }
}
