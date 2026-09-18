import { Component, OnInit, Input, Output, EventEmitter, signal, inject, computed, ContentChild, TemplateRef, input, effect, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../header/header.component';
import { SidebarComponent } from '../../sidebar/sidebar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../page-header/page-header';
import { SidebarService } from '../../sidebar/sidebar.service';
import { DashboardViewerComponent } from '../../../features/reportes/shared/dashboard-viewer/dashboard-viewer.component';
import { AuthService } from '../../../features/auth/services/auth.service';

import { PaginationComponent } from '../pagination/pagination';
import { SearchGenericComponent } from '../reusable-search-selector/reusable-search-selector';
import { Router } from '@angular/router';

import { FormSelectComponent, SelectOption } from '../forms/form-select/form-select.component';
import { SucursalService } from '../../../core/services/sucursal.service';
import { PuntoService } from '../../../features/configuraciones/services/punto.service';
import { SearchableSelectComponent } from '../searchable-select/searchable-select.component';
import { ReporteService } from '../../../features/reportes/services/reporte.service';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../advanced-filter-drawer/advanced-filter-drawer.component';

@Component({
  selector: 'app-reporte-base',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, SidebarComponent, BreadcrumbComponent, PageHeaderComponent, DashboardViewerComponent, PaginationComponent, SearchGenericComponent, AdvancedFilterDrawerComponent],
  templateUrl: './reporte-base.component.html'
})
export class ReporteBaseComponent implements OnInit {
  sidebarService = inject(SidebarService);
  private authService = inject(AuthService);
  private reporteService = inject(ReporteService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  title = input<string>('Reporte');
  subtitle = input<string>('Gestión y análisis de datos');
  breadcrumbItems = input<any[]>([]);
  dashboardCategory = input<string>('General');
  exportButtonText = input<string>('GENERAR REPORTE');
  mainIcon = input<string>('fas fa-file-invoice-dollar');
  moduleLabel = input<string>('MÓDULO DE REPORTES');
  description = input<string>('Prepare su información y exporte los datos necesarios.');
  loading = input<boolean>(false);
  itemsFound = input<number>(0);
  previewTitle = input<string>('Tabla de Registros');
  data = input<any[]>([]);
  searchFields = input<string[]>([]);

  hasInfo: boolean = false;

  @Output() onFiltroChange = new EventEmitter<{ mes: number, anio: number, idSucursal: string, idPuntoVenta: string }>();
  @Output() onExportar = new EventEmitter<{ idSucursal: string, idPuntoVenta: string, mes: number, anio: number }>();

  private sucursalService = inject(SucursalService);
  private puntoService = inject(PuntoService);

  sucursalId = signal<string>('TODOS');
  sucursalOptions = signal<any[]>([]);
  puntoId = signal<string>('TODOS');
  puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);

  get initialSucursalIdParsed(): number | 'TODOS' {
    const val = this.sucursalId();
    if (val === 'TODOS' || !val) return 'TODOS';
    const num = Number(val);
    return isNaN(num) ? 'TODOS' : num;
  }

  get initialPuntoVentaIdParsed(): number | 'TODOS' {
    const val = this.puntoId();
    if (val === 'TODOS' || !val) return 'TODOS';
    const num = Number(val);
    return isNaN(num) ? 'TODOS' : num;
  }

  activeStep = signal(1);
  mes = signal(new Date().getMonth() + 1);
  anio = signal(new Date().getFullYear());

  // Búsqueda y Paginación (Compatibilidad con compartidos)
  searchTerm = signal('');
  currentSearchType = signal('ALL');
  currentPage = signal(0); // 0-indexed para app-pagination
  itemsPerPage = signal(10);
  columnConfigs = input<any[]>([]);
  visibleColumns = signal<Set<string>>(new Set());
  originalVisibleColumns = new Set<string>();
  isColumnModalOpen = signal(false);

  searchOptions = computed(() => {
    return [
      { label: 'Todos los campos', value: 'ALL' },
      ...this.searchFields().map(f => ({ label: f.charAt(0).toUpperCase() + f.slice(1), value: f }))
    ];
  });

  activeFiltersCount = signal<number>(0);
  advancedFilters = signal<AdvancedFilters | null>(null);

  onApplyAdvancedFilters(filtros: AdvancedFilters) {
    this.advancedFilters.set(filtros);
    this.currentPage.set(0);

    let changed = false;
    
    if (filtros.sucursalId && filtros.sucursalId !== 'TODOS' && this.sucursalId() !== filtros.sucursalId.toString()) {
      this.sucursalId.set(filtros.sucursalId.toString());
      changed = true;
    } else if (filtros.sucursalId === 'TODOS' && this.sucursalId() !== 'TODOS') {
      this.sucursalId.set('TODOS');
      changed = true;
    }

    if (filtros.mes && filtros.mes !== 'TODOS' && this.mes() !== Number(filtros.mes)) {
      this.mes.set(Number(filtros.mes));
      changed = true;
    }

    if (filtros.anio && filtros.anio !== 'TODOS' && this.anio() !== Number(filtros.anio)) {
      this.anio.set(Number(filtros.anio));
      changed = true;
    }

    if (filtros.puntoVentaId && filtros.puntoVentaId !== 'TODOS' && this.puntoId() !== filtros.puntoVentaId.toString()) {
      this.puntoId.set(filtros.puntoVentaId.toString());
      changed = true;
    } else if (filtros.puntoVentaId === 'TODOS' && this.puntoId() !== 'TODOS') {
      this.puntoId.set('TODOS');
      changed = true;
    }

    if (changed) {
      this.handleFiltroChange();
    }
  }

  filteredData = computed(() => {
    let sourceData = this.data();

    // 1. Advanced Filters
    const advFilters = this.advancedFilters();
    if (advFilters) {
      sourceData = sourceData.filter(item => {
        let match = true;

        if (advFilters.tipoDocumento && advFilters.tipoDocumento !== 'TODOS') {
          const docType = (item.tipoDoc || item.tipoDocumento || '').toString().toLowerCase();
          if (docType !== advFilters.tipoDocumento.toLowerCase()) match = false;
        }

        if (advFilters.serie) {
          const serie = (item.serie || '').toString().toLowerCase();
          if (!serie.includes(advFilters.serie.toLowerCase())) match = false;
        }

        if (advFilters.numeroComprobante) {
          const num = (item.numero || item.numeroComprobante || '').toString().toLowerCase();
          if (!num.includes(advFilters.numeroComprobante.toLowerCase())) match = false;
        }

        if (advFilters.estado && advFilters.estado !== 'TODOS') {
          const est = item.estado;
          let isVigente = false;
          if (typeof est === 'string') {
              isVigente = (est === 'V' || est === 'ACTIVO');
          } else if (est && est.valor !== undefined) {
              isVigente = est.valor === 1;
          } else if (typeof est === 'boolean') {
              isVigente = est;
          }
          
          if (advFilters.estado === 'ACTIVO' && !isVigente) match = false;
          if (advFilters.estado === 'ANULADO' && isVigente) match = false;
        }

        return match;
      });
    }

    const term = this.searchTerm().toLowerCase();
    const type = this.currentSearchType();

    if (!term) return sourceData;

    return sourceData.filter(item => {
      if (type === 'ALL') {
        return this.searchFields().some(field => {
          const val = item[field];
          return val && val.toString().toLowerCase().includes(term);
        });
      } else {
        const val = item[type];
        return val && val.toString().toLowerCase().includes(term);
      }
    });
  });

  paginatedData = computed(() => {
    const start = this.currentPage() * this.itemsPerPage();
    const end = start + this.itemsPerPage();
    return this.filteredData().slice(start, end);
  });

  totalPages = computed(() => Math.ceil(this.filteredData().length / this.itemsPerPage()));

  totalItems = computed(() => this.filteredData().length);

  mesOptions: SelectOption[] = [
    { label: 'Enero', value: 1 }, { label: 'Febrero', value: 2 }, { label: 'Marzo', value: 3 },
    { label: 'Abril', value: 4 }, { label: 'Mayo', value: 5 }, { label: 'Junio', value: 6 },
    { label: 'Julio', value: 7 }, { label: 'Agosto', value: 8 }, { label: 'Septiembre', value: 9 },
    { label: 'Octubre', value: 10 }, { label: 'Noviembre', value: 11 }, { label: 'Diciembre', value: 12 }
  ];

  anioOptions: SelectOption[] = [];

  constructor() {
    this.generarAnios();

    effect(() => {
      const configs = this.columnConfigs();
      const reportTitle = this.title();
      const sucursalId = this.authService.getSucursalIdFromToken();
      const userId = this.authService.getUserIdFromToken();

      if (configs.length > 0 && sucursalId && userId && reportTitle) {
        this.reporteService.obtenerConfiguracionColumnas(reportTitle, userId, sucursalId).subscribe({
          next: (res) => {
            if (res.success && res.data) {
              const keys = res.data.split(',');
              this.visibleColumns.set(new Set(keys));
            } else {
              if (this.visibleColumns().size === 0) {
                this.visibleColumns.set(new Set(configs.map(c => c.key)));
              }
            }
          },
          error: () => {
            if (this.visibleColumns().size === 0) {
              this.visibleColumns.set(new Set(configs.map(c => c.key)));
            }
          }
        });
      }
    }, { allowSignalWrites: true });

    effect(() => {
      this.data();
      this.currentPage.set(0);
    }, { allowSignalWrites: true });

    effect(() => {
      this.paginatedData();
      this.cdr.detectChanges();
    });
  }

  goToPersonalizar() {
    this.router.navigate(['/reportes/personalizado', this.dashboardCategory()]);
  }

  generarAnios() {
    const anioActual = new Date().getFullYear();
    const anioInicio = 2020;
    const anioFin = anioActual + 1;

    for (let a = anioFin; a >= anioInicio; a--) {
      this.anioOptions.push({ label: a.toString(), value: a });
    }
  }

  setStep(step: number) {
    this.activeStep.set(step);
  }

  ngOnInit(): void {
    this.cargarSucursales();
  }

  cargarSucursales(): void {
    this.sucursalService.getActivas().subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          const ops = [{ label: 'Todas', value: 'TODOS' }];
          res.data.forEach((s: any) => {
            ops.push({ label: s.nombreSucursal, value: s.idSucursal.toString() });
          });
          this.sucursalOptions.set(ops);
          const defaultSucursalId = this.authService.getSucursalIdFromToken();
          if (defaultSucursalId) {
             this.sucursalId.set(defaultSucursalId.toString());
          }
          this.cargarPuntos(this.sucursalId());
        }
      }
    });
  }

  
  

  

  
  cargarPuntos(idSucursal: any) {
    if (!idSucursal || idSucursal === 'TODOS') {
      this.puntoOptions.set([{ label: 'Todos', value: 'TODOS' }]);
      this.puntoId.set('TODOS');
      return;
    }
    this.puntoService.listarTodosSinPaginacion(idSucursal).subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          const ops = [{ label: 'Todos', value: 'TODOS' }];
          res.data.forEach((p: any) => ops.push({ label: p.nombrePunto, value: p.idPunto }));
          this.puntoOptions.set(ops);
          
          if (!res.data.find((p:any) => p.idPunto === this.puntoId())) {
             this.puntoId.set('TODOS');
          }
        }
      }
    });
  }

  onSucursalChange(val: any) {
    this.sucursalId.set(val);
    this.cargarPuntos(val);
    this.handleFiltroChange();
  }

  onPuntoChange(val: any) {
    this.puntoId.set(val);
    this.handleFiltroChange();
  }

  handleFiltroChange() {
    this.onFiltroChange.emit({ mes: this.mes(), anio: this.anio(), idSucursal: this.sucursalId(), idPuntoVenta: this.puntoId() });
  }

  handleExportar() {
    this.onExportar.emit({
      idSucursal: this.sucursalId(),
      idPuntoVenta: this.puntoId(),
      mes: this.mes(),
      anio: this.anio()
    });
  }

  onSearchChange(event: { q: string, type: string }) {
    this.searchTerm.set(event.q);
    this.currentSearchType.set(event.type);
    this.currentPage.set(0);
  }

  onPageChange(page: number) {
    this.currentPage.set(page);
  }

  onPageSizeChange(size: number) {
    this.itemsPerPage.set(size);
    this.currentPage.set(0);
  }

  toggleColumn(key: string) {
    const current = new Set(this.visibleColumns());
    if (current.has(key)) {
      current.delete(key);
    } else {
      current.add(key);
    }
    this.visibleColumns.set(current);
  }

  isColumnVisible(key: string): boolean {
    return this.visibleColumns().has(key);
  }

  openColumnModal() {
    this.originalVisibleColumns = new Set(this.visibleColumns());
    this.isColumnModalOpen.set(true);
  }

  saveColumnChanges() {
    this.isColumnModalOpen.set(false);
    this.saveColumnConfig();
  }

  cancelColumnChanges() {
    this.visibleColumns.set(new Set(this.originalVisibleColumns));
    this.isColumnModalOpen.set(false);
  }

  private saveColumnConfig() {
    const reportTitle = this.title();
    const sucursalId = this.authService.getSucursalIdFromToken();
    const userId = this.authService.getUserIdFromToken();

    if (sucursalId && userId && reportTitle) {
      const keys = Array.from(this.visibleColumns()).join(',');
      this.reporteService.guardarConfiguracionColumnas(reportTitle, userId, sucursalId, keys).subscribe();
    }
  }
}
