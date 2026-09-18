import { Component, OnInit, signal, OnDestroy, computed } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, finalize } from 'rxjs';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { VentaRegistroService } from '../../services/venta-registro.service';
import { PuntoService } from '../../../configuraciones/services/punto.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { AuthService } from '../../../auth/services/auth.service';
import { inject } from '@angular/core';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';

@Component({
  selector: 'app-detalle-documentos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    PageHeaderComponent,
    BreadcrumbComponent,
    SidebarComponent,
    HeaderComponent,
    PaginationComponent,
    TablaGeneralComponent,
    AdvancedFilterDrawerComponent
  ],
  templateUrl: './detalle-documentos.component.html',
  providers: [CurrencyPipe, DatePipe]
})
export class DetalleDocumentosComponent implements OnInit, OnDestroy {
  breadcrumbItems = [
    { label: 'Inicio', link: '/dashboard' },
    { label: 'Procesos' },
    { label: 'Detalle de Documentos' }
  ];

  sucursalId = signal<number | 'TODOS'>('TODOS');
  sucursalOptions = signal<any[]>([{ label: 'Todas', value: 'TODOS' }]);
  
  puntoId = signal<number | 'TODOS'>('TODOS');
  puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);
  
  private puntoService = inject(PuntoService);
  private sucursalService = inject(SucursalService);
  private authService = inject(AuthService);
  fechaDesde = signal<string>('');
  fechaHasta = signal<string>('');
  
  tipoDocFiltro = signal<string>('');
  estadoFiltro = signal<string>('TODOS');
  
  loading = signal<boolean>(false);
  
  columnas: Columna[] = [
    { field: 'fechaDisplay', header: 'Fecha Emisión', tipo: 'multi-date', subField: [] },
    { field: 'comprobanteCompleto', header: 'Comprobante', tipo: 'layered-info', layeredConfig: [
      { field: 'comprobanteCompleto', class: 'font-bold text-slate-800 dark:text-slate-200 tracking-wide' },
      { field: 'tipoComprobanteDesc', class: 'font-medium text-slate-500' }
    ], subField: [] },
    { field: 'emisorDisplay', header: 'Usuario Emisor', tipo: 'text', subField: [] },
    { field: 'puntoDisplay', header: 'Punto de Venta', tipo: 'text', subField: [] },
    { field: 'estadoStr', header: 'Estado SUNAT', tipo: 'venta-status', subField: [] },
    { field: 'totalDisplay', header: 'Importe Total', tipo: 'currency-status', subField: [] }
  ];

  activeFiltersCount = signal<number>(0);
  
  // Pagination
  currentPage = signal<number>(0);
  pageSize = signal<number>(25);
  documentos = signal<any[]>([]);
  documentosPaginados = computed(() => {
    const start = this.currentPage() * this.pageSize();
    return this.documentos().slice(start, start + this.pageSize());
  });
  totalPages = computed(() => Math.ceil(this.documentos().length / this.pageSize()));

  totalesPorTipo = signal<{ tipo: string, nombre: string, total: number, cantidad: number, badgeClass: string }[]>([]);
  totalGlobal = signal<number>(0); // Keep global if needed, or remove it. Let's keep it just in case, but calculate both.
  
  Math = Math; // Expose Math to template
  
  private destroy$ = new Subject<void>();

  constructor(
    public sidebarService: SidebarService,
    private ventaService: VentaRegistroService,
    private datePipe: DatePipe,
    private currencyPipe: CurrencyPipe
  ) {}

  ngOnInit(): void {
    const defaultSucursalId = this.authService.getSucursalIdFromToken();
    if (defaultSucursalId) {
        this.sucursalId.set(defaultSucursalId);
    }
    this.cargarSucursales();
    this.cargarPuntos(this.sucursalId());

    this.fechaDesde.set('');
    this.fechaHasta.set('');
    this.buscar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarSucursales(): void {
    this.sucursalService.getActivas().subscribe({
        next: (res: any) => {
            if (res.success && res.data) {
                const ops = [{ label: 'Todas', value: 'TODOS' }];
                res.data.forEach((s: any) => {
                    ops.push({ label: s.nombreSucursal, value: s.idSucursal });
                });
                this.sucursalOptions.set(ops);
            }
        }
    });
  }

  onSucursalChange(val: any): void {
      this.sucursalId.set(val);
      this.puntoId.set('TODOS');
      this.cargarPuntos(val);
      this.buscar();
  }

  cargarPuntos(idSucursal: number | 'TODOS'): void {
      if (idSucursal === 'TODOS') {
          this.puntoOptions.set([{ label: 'Todos', value: 'TODOS' }]);
          return;
      }
      this.puntoService.listarTodosSinPaginacion(idSucursal as number).subscribe({
          next: (res: any) => {
              if (res.success && res.data) {
                  const ops = [{ label: 'Todos', value: 'TODOS' }];
                  res.data.forEach((p: any) => {
                      ops.push({ label: p.nombrePunto || p.nombre, value: p.idPunto });
                  });
                  this.puntoOptions.set(ops);
              }
          }
      });
  }

  onPuntoChange(val: any): void {
      this.puntoId.set(val);
      this.buscar();
  }

  onApplyFilters(filters: AdvancedFilters): void {
      this.fechaDesde.set(filters.fechaDesde || '');
      this.fechaHasta.set(filters.fechaHasta || '');
      this.estadoFiltro.set(filters.estado !== 'TODOS' ? (filters.estado as string) : '');
      this.tipoDocFiltro.set(filters.tipoDocumento !== 'TODOS' ? (filters.tipoDocumento as string) : '');
      this.sucursalId.set(filters.sucursalId);
      this.puntoId.set(filters.puntoVentaId);

      if(filters.sucursalId !== 'TODOS' && this.puntoOptions().length === 1) {
          this.cargarPuntos(filters.sucursalId);
      }
      this.buscar();
  }

  buscar(): void {
    
    this.loading.set(true);
    const estadoFiltro = this.estadoFiltro() === 'TODOS' ? '' : this.estadoFiltro();
    // Fetch a large page to calculate full sums
    this.ventaService.search(0, 10000, '', '', undefined, undefined, this.fechaDesde(), this.fechaHasta(), '', '', estadoFiltro, this.tipoDocFiltro(), false, this.sucursalId(), this.puntoId())
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (res: any) => {
          if (res.success && res.data) {
            const mapped = (res.data.content || []).map((doc: any) => {
              const estadoStr = typeof doc.estado === 'object' && doc.estado !== null ? doc.estado.name : doc.estado;
              // Si es nota de crédito y no está anulada, el total debería mostrarse en negativo visualmente
              const esNCResta = doc.tipoDoc === '07' && estadoStr !== 'ANULADO';
              
              return {
                ...doc,
                fechaDisplay: doc.fecha,
                comprobanteCompleto: `${doc.serie}-${String(doc.numero).padStart(7, '0')}`,
                tipoComprobanteDesc: this.getTipoComprobante(doc.tipoDoc),
                emisorDisplay: doc.nombreVendedor || doc.idUser || 'N/A',
                puntoDisplay: doc.punto || 'N/A',
                estadoStr: estadoStr,
                totalDisplay: esNCResta ? -Math.abs(doc.total) : doc.total
              };
            });
            this.documentos.set(mapped);
            this.currentPage.set(0); // Reset page on new search
            this.calcularTotal();
          }
        },
        error: (err: any) => {
          console.error('Error al cargar documentos:', err);
          this.documentos.set([]);
          this.totalGlobal.set(0);
          this.totalesPorTipo.set([]);
        }
      });
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  onPageSizeChange(size: number): void {
    this.pageSize.set(size);
    this.currentPage.set(0);
  }

  calcularTotal(): void {
    const docs = this.documentos();
    let totalGeneral = 0;
    
    // Map to group totals
    const mapTotales = new Map<string, { total: number, cantidad: number }>();
    
    for (const doc of docs) {
      const estadoStr = typeof doc.estado === 'object' && doc.estado !== null ? doc.estado.name : doc.estado;
      if (estadoStr === 'ANULADO') {
        continue;
      }
      
      const tipo = doc.tipoDoc || 'OTRO';
      const monto = doc.total || 0;
      
      const actual = mapTotales.get(tipo) || { total: 0, cantidad: 0 };
      mapTotales.set(tipo, {
        total: actual.total + monto,
        cantidad: actual.cantidad + 1
      });
      
      if (tipo === '07') {
        totalGeneral -= monto; // Notas de credito restan al global
      } else {
        totalGeneral += monto;
      }
    }
    
    const arrayTotales = [];
    for (const [tipo, data] of mapTotales.entries()) {
      arrayTotales.push({
        tipo: tipo,
        nombre: this.getTipoComprobante(tipo),
        total: data.total,
        cantidad: data.cantidad,
        badgeClass: this.getTipoDocBadgeClass(tipo)
      });
    }
    
    // Order: Facturas, Boletas, Tickets, NC, NV
    arrayTotales.sort((a, b) => {
      const order = ['01', '03', '00', '07', '08', 'NV'];
      const indexA = order.indexOf(a.tipo) !== -1 ? order.indexOf(a.tipo) : 99;
      const indexB = order.indexOf(b.tipo) !== -1 ? order.indexOf(b.tipo) : 99;
      return indexA - indexB;
    });

    this.totalesPorTipo.set(arrayTotales);
    this.totalGlobal.set(totalGeneral);
  }

  getTipoComprobante(tipoDoc: string): string {
    switch(tipoDoc) {
      case '01': return 'Factura';
      case '03': return 'Boleta';
      case '07': return 'Nota de Crédito';
      case '08': return 'Nota de Débito';
      case '00': return 'Ticket';
      case 'NV': return 'Nota de Venta';
      default: return tipoDoc || 'OTRO';
    }
  }

  getTipoDocCorto(tipoDoc: string): string {
    switch(tipoDoc) {
      case '01': return 'FAC';
      case '03': return 'BOL';
      case '07': return 'NC';
      case '08': return 'ND';
      case '00': return 'TK';
      case 'NV': return 'NV';
      default: return 'DOC';
    }
  }

  getTipoDocBadgeClass(tipoDoc: string): string {
    switch(tipoDoc) {
      case '01': return 'bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800'; // Factura
      case '03': return 'bg-teal-100 text-teal-700 border-teal-200 dark:bg-teal-900/30 dark:text-teal-400 dark:border-teal-800'; // Boleta
      case '07': return 'bg-rose-100 text-rose-700 border-rose-200 dark:bg-rose-900/30 dark:text-rose-400 dark:border-rose-800'; // Nota Credito
      case '00': return 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700'; // Ticket
      case 'NV': return 'bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800'; // NV
      default: return 'bg-gray-100 text-gray-700 border-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-700';
    }
  }

  exportarExcel(): void {
    alert('Función de exportar a Excel en desarrollo.');
  }
}
