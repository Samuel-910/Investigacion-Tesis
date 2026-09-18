import { Component, OnInit, signal, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { AdvancedFilterDrawerComponent, AdvancedFilters } from '../../../shared/components/advanced-filter-drawer/advanced-filter-drawer.component';
import { PuntoService } from '../../configuraciones/services/punto.service';
import { SucursalService } from '../../../core/services/sucursal.service';
import { VentaRegistroService } from '../services/venta-registro.service';
import { DocumentoImpresionService } from '../../documentos/services/documento-impresion.service';
import { PuntoDocumentoService } from '../../documentos/services/punto-documento.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ThemeService } from '../../../core/services/theme.service';
import { VentaDetalleModalComponent } from '../listado-ventas/venta-detalle-modal/venta-detalle-modal.component';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-reimpresion',
  standalone: true,
  imports: [CommonModule, FormsModule, VentaDetalleModalComponent, HeaderComponent, SidebarComponent, PageHeaderComponent, TablaGeneralComponent, PaginationComponent, ModalComponent, AdvancedFilterDrawerComponent],
  templateUrl: './reimpresion.component.html'
})
export class ReimpresionComponent implements OnInit {
  @ViewChild('filterDrawer') filterDrawer!: AdvancedFilterDrawerComponent;
  activeFiltersCount = signal<number>(0);

  // Filtro Sucursal
  sucursalId = signal<number | 'TODOS'>('TODOS');
  sucursalOptions = signal<any[]>([{ label: 'Todas', value: 'TODOS' }]);
  
  puntoId = signal<number | 'TODOS'>('TODOS');
  puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);
  
  private puntoService = inject(PuntoService);
  private sucursalService = inject(SucursalService);
  serie = signal<string>('');
  numeroDesde = signal<string>('');
  numeroHasta = signal<string>('');
  tipoDoc = signal<string>('');
  fechaDesde = signal<string>('');
  fechaHasta = signal<string>('');
  estado = signal<string>('TODOS');
  searchTerm = signal<string>('');

  resultados = signal<any[]>([]);
  loading = signal<boolean>(false);
  
  // Selection and mass print
  selectedItems = signal<any[]>([]);
  isMassPrinting = signal<boolean>(false);
  massPrintProgress = signal<number>(0);
  massPrintTotal = signal<number>(0);

  // Pagination states
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  isDetalleModalOpen = signal<boolean>(false);
  selectedVenta = signal<any>(null);

  isHistorialModalOpen = signal<boolean>(false);
  historialReimpresiones = signal<any[]>([]);

  columnas: Columna[] = [
    { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
    { field: 'clienteNombre', header: 'Cliente', tipo: 'text', subField: [] },
    { field: 'tipoDocDesc', header: 'Documento', tipo: 'text', subField: [] },
    { field: 'total', header: 'Total', tipo: 'currency', subField: [] },
    { field: 'estado', header: 'Estado', tipo: 'venta-status', subField: [] }
  ];

  constructor(
    private ventaService: VentaRegistroService,
    private impresionService: DocumentoImpresionService,
    private puntoDocumentoService: PuntoDocumentoService,
    private authService: AuthService,
    private alertService: AlertService,
    public themeService: ThemeService,
    public sidebarService: SidebarService
  ) { }

  ngOnInit(): void {
    const defaultSucursalId = this.authService.getSucursalIdFromToken();
    if (defaultSucursalId) {
        this.sucursalId.set(defaultSucursalId);
    }
    this.cargarSucursales();
    this.cargarPuntos(this.sucursalId());
    
    // Lanzamos la búsqueda inicial
    this.buscarVenta();
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
  }

  onApplyFilters(filters: AdvancedFilters): void {
    this.fechaDesde.set(filters.fechaDesde || '');
    this.fechaHasta.set(filters.fechaHasta || '');
    this.estado.set(filters.estado !== 'TODOS' ? (filters.estado as string) : '');
    
    this.sucursalId.set(filters.sucursalId);
    this.puntoId.set(filters.puntoVentaId);
    if(filters.sucursalId !== 'TODOS' && this.puntoOptions().length === 1) {
        this.cargarPuntos(filters.sucursalId);
    }
    
    this.tipoDoc.set(filters.tipoDocumento !== 'TODOS' ? (filters.tipoDocumento as string) : '');
    this.serie.set(filters.serie || '');
    this.numeroDesde.set(filters.numeroDesde || '');
    this.numeroHasta.set(filters.numeroHasta || '');
    
    this.currentPage.set(0);
    this.buscarVenta();
  }

  onGlobalSearch(term: string): void {
      this.searchTerm.set(term);
      this.currentPage.set(0);
      this.buscarVenta();
  }

  buscarVenta(resetPage: boolean = false): void {
    if (resetPage) {
      this.currentPage.set(0);
    }

    this.loading.set(true);
    const estadoFiltro = this.estado() === 'TODOS' ? '' : this.estado();
    
    // Usamos el endpoint search-advanced que permite buscar por serie, numeroDesde, numeroHasta, tipoDoc
    this.ventaService.search(this.currentPage(), this.pageSize(), this.serie(), this.searchTerm(), this.numeroDesde(), this.numeroHasta(), this.fechaDesde(), this.fechaHasta(), '', '', estadoFiltro, this.tipoDoc(), false, this.sucursalId(), this.puntoId()).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success && res.data) {
          const processedContent = (res.data.content || []).map((item: any) => {
            const isDoc = item.tipoDoc === '01' ? 'Factura' : (item.tipoDoc === '03' ? 'Boleta' : (item.tipoDoc === '07' ? 'Nota de Crédito' : (item.tipoDoc === '12' ? 'Ticket' : 'Nota de Venta')));
            return {
              ...item,
              clienteNombre: item.cliente ? (item.cliente.nombreCompleto || item.cliente.nombre) : (item.nombrePaciente || item.nombrePac || item.pacienteNombre || 'CLIENTE GENÉRICO'),
              tipoDocDesc: `${isDoc} (${item.serie}-${item.numero})`
            };
          });
          this.resultados.set(processedContent);
          
          if (res.data.totalElements !== undefined) {
            this.totalElements.set(res.data.totalElements);
            this.totalPages.set(res.data.totalPages);
          } else {
            this.totalElements.set(processedContent.length);
            this.totalPages.set(1);
          }
        } else {
          this.resultados.set([]);
          this.totalElements.set(0);
          this.totalPages.set(0);
        }
      },
      error: () => {
        this.loading.set(false);
        this.resultados.set([]);
        this.totalElements.set(0);
        this.totalPages.set(0);
        this.alertService.error('Error', 'No se pudo realizar la búsqueda');
      }
    });
  }

  limpiar(): void {
    if (this.filterDrawer) {
      this.filterDrawer.clearFilters();
    }
  }

  cambiarPagina(page: number): void {
    this.currentPage.set(page);
    this.buscarVenta();
  }

  cambiarPageSize(size: number): void {
    this.pageSize.set(size);
    this.buscarVenta(true);
  }

  verDetalles(venta: any): void {
    this.selectedVenta.set(venta);
    this.isDetalleModalOpen.set(true);
  }

  verHistorial(venta: any): void {
    this.loading.set(true);
    this.ventaService.obtenerHistorialReimpresiones(venta.idVenta).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.historialReimpresiones.set(res.data || []);
          this.isHistorialModalOpen.set(true);
        } else {
          this.alertService.error('Error', res.message || 'No se pudo obtener el historial');
        }
      },
      error: () => {
        this.loading.set(false);
        this.alertService.error('Error', 'Error de conexión al obtener el historial');
      }
    });
  }

  onSelectionChange(selected: any[]): void {
    this.selectedItems.set(selected);
  }

  async reimprimirMasa(): Promise<void> {
    const items = this.selectedItems();
    if (!items || items.length === 0) {
      this.alertService.warning('Advertencia', 'Debe seleccionar al menos un documento para imprimir');
      return;
    }

    const confirmed = await this.alertService.confirm(
      'Reimpresión en Masa',
      `¿Está seguro que desea imprimir los ${items.length} documentos seleccionados de una sola vez?`
    );

    if (confirmed) {
      this.isMassPrinting.set(true);
      this.massPrintTotal.set(items.length);
      this.massPrintProgress.set(0);

      const authPuntoId = this.authService.getPuntoIdFromToken();
      if (!authPuntoId) {
        this.alertService.error('Error', 'No se ha detectado el punto de venta asociado al usuario');
        this.isMassPrinting.set(false);
        return;
      }

      try {
        const puntoDocsRes: any = await firstValueFrom(this.puntoDocumentoService.obtenerPorPunto(authPuntoId));
        if (!puntoDocsRes.success || !puntoDocsRes.data) throw new Error('No se pudo obtener la configuración de documentos');

        const documentosParaImprimir = [];

        for (let i = 0; i < items.length; i++) {
          const item = items[i];
          const moduleType = item.tipoDoc === 'NV' ? 'NOTA_VENTA' : 'VENTA';
          const asignacion = puntoDocsRes.data.find((a: any) => {
            const estadoName = typeof a.estado === 'object' ? a.estado?.name : a.estado;
            return a.modulo === moduleType && estadoName === 'ACTIVO' && a.tipoDoc === item.tipoDoc;
          });

          if (asignacion && asignacion.idPlantilla) {
            const idVenta = item.idVenta || item.id;
            
            // Registrar auditoria
            await firstValueFrom(this.ventaService.registrarReimpresion(idVenta, 'Reimpresión en masa'));
            
            // Obtener venta
            const resVenta: any = await firstValueFrom(this.ventaService.obtenerPorId(idVenta));
            if (resVenta.success && resVenta.data) {
              documentosParaImprimir.push({
                idPlantilla: asignacion.idPlantilla,
                datosVenta: resVenta.data
              });
            }
          }
          this.massPrintProgress.set(i + 1);
        }

        if (documentosParaImprimir.length > 0) {
          // Imprimir todos juntos en un solo popup
          await this.impresionService.imprimirMasa(documentosParaImprimir);
          this.alertService.success('Éxito', 'Impresión en masa finalizada');
        } else {
          this.alertService.warning('Aviso', 'No se generaron documentos para imprimir (falta de plantillas)');
        }
      } catch (err: any) {
         this.alertService.error('Error', err.message || 'Error en la impresión masiva');
      }

      this.isMassPrinting.set(false);
      this.selectedItems.set([]);
    }
  }

  reimprimir(venta: any): void {
    const authPuntoId = this.authService.getPuntoIdFromToken();
    if (!authPuntoId) {
      this.alertService.error('Error', 'No se ha detectado el punto de venta asociado al usuario');
      return;
    }

    const moduleType = venta.tipoDoc === 'NV' ? 'NOTA_VENTA' : 'VENTA';

    this.puntoDocumentoService.obtenerPorPunto(authPuntoId).subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          const asignacion = res.data.find((a: any) => {
            const estadoName = typeof a.estado === 'object' ? a.estado?.name : a.estado;
            return a.modulo === moduleType &&
                   estadoName === 'ACTIVO' &&
                   a.tipoDoc === venta.tipoDoc;
          });

          if (!asignacion || !asignacion.idPlantilla) {
            this.alertService.error('Error', `No hay plantilla de impresión configurada para este punto y tipo de documento (${venta.tipoDoc})`);
            return;
          }

          const idPlantilla = asignacion.idPlantilla;
          const idVenta = venta.idVenta || venta.id;

          this.loading.set(true);

          // 1. Registrar auditoría de reimpresión
          this.ventaService.registrarReimpresion(idVenta, 'Reimpresión manual').subscribe({
            next: (resAuditoria) => {
              if (resAuditoria.success) {
                // 2. Obtener datos completos de la venta y lanzar impresión
                this.ventaService.obtenerPorId(idVenta).subscribe({
                  next: (resVenta) => {
                    this.loading.set(false);
                    if (resVenta.success && resVenta.data) {
                      this.impresionService.imprimirVenta(idPlantilla, resVenta.data)
                        .then(() => this.alertService.toast('Documento enviado a impresión', 'success'))
                        .catch(() => this.alertService.error('Error', 'No se pudo generar la impresión'));
                    }
                  },
                  error: () => {
                    this.loading.set(false);
                    this.alertService.error('Error', 'No se pudieron cargar los detalles para imprimir');
                  }
                });
              } else {
                this.loading.set(false);
                this.alertService.error('Error', resAuditoria.message || 'No se pudo registrar la auditoría de reimpresión');
              }
            },
            error: () => {
              this.loading.set(false);
              this.alertService.error('Error', 'Falló el registro de auditoría de reimpresión');
            }
          });
        } else {
          this.alertService.error('Error', 'No se pudo obtener la configuración de documentos del punto de venta');
        }
      },
      error: () => this.alertService.error('Error', 'No se pudo obtener la configuración de documentos del punto de venta')
    });
  }
}
