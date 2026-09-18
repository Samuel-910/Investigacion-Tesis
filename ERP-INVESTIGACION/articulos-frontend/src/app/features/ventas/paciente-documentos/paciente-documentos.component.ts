import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentaRegistroService } from '../services/venta-registro.service';
import { UserService } from '../../../core/services/user.service';
import { ThemeService } from '../../../core/services/theme.service';
import { AlertService } from '../../../core/services/alert.service';
import { VentaDetalleModalComponent } from '../listado-ventas/venta-detalle-modal/venta-detalle-modal.component';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { VentaSidebarComponent } from '../venta-registro/components/venta-sidebar/venta-sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PuntoService } from '../../configuraciones/services/punto.service';
import { SucursalService } from '../../../core/services/sucursal.service';
import { AuthService } from '../../auth/services/auth.service';
import { inject } from '@angular/core';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';

@Component({
  selector: 'app-paciente-documentos',
  standalone: true,
  imports: [CommonModule, FormsModule, VentaDetalleModalComponent, HeaderComponent, SidebarComponent, PageHeaderComponent, VentaSidebarComponent, TablaGeneralComponent, PaginationComponent, AdvancedFilterDrawerComponent],
  templateUrl: './paciente-documentos.component.html',
  styleUrls: ['./paciente-documentos.component.css']
})
export class PacienteDocumentosComponent implements OnInit {
  pacienteSeleccionado = signal<any>(null);

  ventas = signal<any[]>([]);
  allVentasLocales = signal<any[]>([]);
  loadingVentas = signal<boolean>(false);

  filtroSucursal = signal<number | 'TODOS'>('TODOS');
  sucursalOptions = signal<any[]>([{ label: 'Todas', value: 'TODOS' }]);

  filtroPunto = signal<number | 'TODOS'>('TODOS');
  puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);

  private puntoService = inject(PuntoService);
  private sucursalService = inject(SucursalService);
  private authService = inject(AuthService);
  filtroTipoDoc = signal<string>('');
  filtroEstado = signal<string>('');
  filtroSerie = signal<string>('');
  filtroNumero = signal<string>('');
  globalSearchTerm = signal<string>('');
  filtroFechaDesde = signal<string>('');
  filtroFechaHasta = signal<string>('');
  activeFiltersCount = signal<number>(0);

  ventasFiltradas = computed(() => {
    let list = this.allVentasLocales();

    if (this.filtroSucursal() && this.filtroSucursal() !== 'TODOS') {
      list = list.filter(v => (v.sucursalObj?.idSucursal || v.idSucursal) == this.filtroSucursal());
    }
    if (this.filtroPunto() && this.filtroPunto() !== 'TODOS') {
      list = list.filter(v => (v.puntoObj?.idPunto || v.punto || v.idPunto) == this.filtroPunto());
    }
    if (this.filtroTipoDoc()) {
      list = list.filter(v => v.tipoDoc === this.filtroTipoDoc());
    }
    if (this.filtroEstado()) {
      list = list.filter(v => {
        const est = typeof v.estado === 'object' && v.estado !== null ? v.estado.name : v.estado;
        return est === this.filtroEstado();
      });
    }
    if (this.filtroSerie()) {
      list = list.filter(v => v.serie?.toLowerCase().includes(this.filtroSerie().toLowerCase()));
    }
    if (this.filtroNumero()) {
      list = list.filter(v => v.numero?.toString().includes(this.filtroNumero()));
    }
    if (this.filtroFechaDesde()) {
      list = list.filter(v => v.fecha >= this.filtroFechaDesde());
    }
    if (this.filtroFechaHasta()) {
      list = list.filter(v => v.fecha <= this.filtroFechaHasta());
    }

    const search = this.globalSearchTerm().toLowerCase();
    if (search) {
      list = list.filter(v =>
        (v.serie && v.serie.toLowerCase().includes(search)) ||
        (v.numero && v.numero.toString().includes(search)) ||
        (v.clienteNombre && v.clienteNombre.toLowerCase().includes(search)) ||
        (v.documentoDesc && v.documentoDesc.toLowerCase().includes(search))
      );
    }

    return list;
  });

  // Pagination states
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);

  totalElements = computed(() => this.pacienteSeleccionado() ? this.ventasFiltradas().length : this.allVentasLocales().length);
  totalPages = computed(() => Math.ceil(this.totalElements() / this.pageSize()));

  ventasPaginadas = computed(() => {
    const list = this.pacienteSeleccionado() ? this.ventasFiltradas() : this.allVentasLocales();
    const start = this.currentPage() * this.pageSize();
    return list.slice(start, start + this.pageSize());
  });

  isDetalleModalOpen = signal<boolean>(false);
  selectedVenta = signal<any>(null);

  columnas = computed<Columna[]>(() => {
    if (this.pacienteSeleccionado()) {
      return [
        { field: 'documentoDesc', header: 'Documento', tipo: 'text', subField: [] },
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'total', header: 'Monto Total', tipo: 'currency', subField: [] },
        { field: 'sucursal', header: 'Sucursal', tipo: 'text', subField: [] },
        { field: 'puntoVenta', header: 'Punto de Venta', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'venta-status', subField: [] }
      ];
    }
    return [
      { field: 'clienteNombre', header: 'Paciente / Cliente', tipo: 'text', subField: [] },
      { field: 'documentoDesc', header: 'Documento', tipo: 'text', subField: [] },
      { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
      { field: 'total', header: 'Monto Total', tipo: 'currency', subField: [] },
      { field: 'sucursal', header: 'Sucursal', tipo: 'text', subField: [] },
      { field: 'puntoVenta', header: 'Punto de Venta', tipo: 'text', subField: [] },
      { field: 'estado', header: 'Estado', tipo: 'venta-status', subField: [] }
    ];
  });

  constructor(
    private ventaService: VentaRegistroService,
    private userService: UserService,
    public themeService: ThemeService,
    private alertService: AlertService,
    public sidebarService: SidebarService
  ) { }

  ngOnInit(): void {
    const defaultSucursalId = this.authService.getSucursalIdFromToken();
    if (defaultSucursalId) {
      this.filtroSucursal.set(defaultSucursalId);
    }
    this.cargarSucursales();
    this.cargarPuntos(this.filtroSucursal());
    this.cargarVentasRecientes();
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
    this.filtroSucursal.set(val);
    this.filtroPunto.set('TODOS');
    this.cargarPuntos(val);

    this.currentPage.set(0);
    if (!this.pacienteSeleccionado()) {
      this.cargarVentasRecientes();
    }
  }

  onApplyFilters(filters: AdvancedFilters): void {
    this.filtroSucursal.set(filters.sucursalId);
    this.filtroPunto.set(filters.puntoVentaId);
    this.filtroFechaDesde.set(filters.fechaDesde || '');
    this.filtroFechaHasta.set(filters.fechaHasta || '');
    this.filtroEstado.set(filters.estado !== 'TODOS' ? (filters.estado as string) : '');
    this.filtroTipoDoc.set(filters.tipoDocumento !== 'TODOS' ? (filters.tipoDocumento as string) : '');
    this.filtroSerie.set(filters.serie || '');
    this.filtroNumero.set(filters.numeroComprobante || '');

    this.currentPage.set(0);

    if (filters.sucursalId !== 'TODOS' && this.puntoOptions().length === 1) {
      this.cargarPuntos(filters.sucursalId);
    }

    if (!this.pacienteSeleccionado()) {
      this.cargarVentasRecientes();
    }
  }

  onGlobalSearch(term: string): void {
    this.globalSearchTerm.set(term);
    this.currentPage.set(0);
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
    this.filtroPunto.set(val);
    this.currentPage.set(0);
    if (!this.pacienteSeleccionado()) {
      this.cargarVentasRecientes();
    }
  }


  cargarVentasRecientes(): void {
    this.loadingVentas.set(true);
    this.ventaService.search(0, 100, '', '', '', '', '', '', '', '', '', '', false, this.filtroSucursal(), this.filtroPunto()).subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          const content = res.data.content || res.data || [];
          const processedContent = content.map((item: any) => {
            const isDoc = item.tipoDoc === '01' ? 'Factura' : (item.tipoDoc === '03' ? 'Boleta' : (item.tipoDoc === '07' ? 'Nota de Crédito' : (item.tipoDoc === '12' ? 'Ticket' : 'Nota de Venta')));
            return {
              ...item,
              clienteNombre: item.cliente ? (item.cliente.nombreCompleto || item.cliente.nombre) : (item.pacienteNombre || item.nombrePac || 'CLIENTE GENÉRICO'),
              documentoDesc: `${isDoc} (${item.serie}-${item.numero})`,
              sucursalObj: item.sucursal,
              puntoObj: item.puntoVenta || item.punto,
              sucursal: item.sucursal?.nombre || (item.idSucursal ? 'Suc. ' + item.idSucursal : 'N/A'),
              puntoVenta: item.punto || item.puntoVenta?.nombre || 'N/A'
            };
          });
          this.allVentasLocales.set(processedContent);
        } else {
          this.allVentasLocales.set([]);
        }
        this.loadingVentas.set(false);
      },
      error: () => {
        this.loadingVentas.set(false);
        this.allVentasLocales.set([]);
      }
    });
  }

  seleccionarPaciente(paciente: any): void {
    this.currentPage.set(0);
    this.pacienteSeleccionado.set(paciente);
    this.limpiarFiltros();
    this.cargarDocumentos(paciente.idPersonal || paciente.id);
  }

  limpiarFiltros(): void {
    this.filtroTipoDoc.set('');
    this.filtroEstado.set('');
    this.filtroSerie.set('');
    this.filtroNumero.set('');
    this.filtroFechaDesde.set('');
    this.filtroFechaHasta.set('');
    const defaultSucursalId = this.authService.getSucursalIdFromToken();
    this.filtroSucursal.set(defaultSucursalId || 'TODOS');
    this.filtroPunto.set('TODOS');
    this.cargarPuntos(this.filtroSucursal());
  }

  limpiarSeleccion(): void {
    this.currentPage.set(0);
    this.pacienteSeleccionado.set(null);
    this.allVentasLocales.set([]);
    this.cargarVentasRecientes();
  }

  cargarDocumentos(idPersonal: string | number): void {
    this.loadingVentas.set(true);
    this.ventaService.listarVentasPorPaciente(idPersonal.toString()).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const processedContent = (res.data || []).map((item: any) => {
            const isDoc = item.tipoDoc === '01' ? 'Factura' : (item.tipoDoc === '03' ? 'Boleta' : (item.tipoDoc === '07' ? 'Nota de Crédito' : (item.tipoDoc === '12' ? 'Ticket' : 'Nota de Venta')));
            return {
              ...item,
              clienteNombre: item.cliente ? (item.cliente.nombreCompleto || item.cliente.nombre) : (item.pacienteNombre || item.nombrePac || 'CLIENTE GENÉRICO'),
              documentoDesc: `${isDoc} (${item.serie}-${item.numero})`,
              sucursalObj: item.sucursal,
              puntoObj: item.puntoVenta || item.punto,
              sucursal: item.sucursal?.nombre || (item.idSucursal ? 'Suc. ' + item.idSucursal : 'N/A'),
              puntoVenta: item.punto || item.puntoVenta?.nombre || 'N/A'
            };
          });
          this.allVentasLocales.set(processedContent);

          if (this.allVentasLocales().length === 0) {
            this.alertService.info('Sin resultados', 'Este paciente no tiene comprobantes emitidos.');
          }
        } else {
          this.allVentasLocales.set([]);
        }
        this.loadingVentas.set(false);
      },
      error: () => {
        this.loadingVentas.set(false);
        this.allVentasLocales.set([]);
        this.alertService.error('Error', 'No se pudieron cargar los comprobantes del paciente');
      }
    });
  }

  cambiarPagina(page: number): void {
    this.currentPage.set(page);
  }

  cambiarPageSize(size: number): void {
    this.pageSize.set(size);
    this.currentPage.set(0);
  }

  verDetalles(venta: any): void {
    this.selectedVenta.set(venta);
    this.isDetalleModalOpen.set(true);
  }
}
