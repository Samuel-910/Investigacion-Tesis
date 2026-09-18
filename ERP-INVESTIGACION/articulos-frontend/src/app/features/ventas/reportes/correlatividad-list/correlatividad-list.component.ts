import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { VentaRegistroService } from '../../services/venta-registro.service';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { PuntoService } from '../../../configuraciones/services/punto.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent,
        } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-correlatividad-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        BreadcrumbComponent,
        ModalComponent,
        SearchGenericComponent,
        PaginationComponent, SearchableSelectComponent
    ],
    templateUrl: './correlatividad-list.component.html'
})
export class CorrelatividadListComponent implements OnInit {
    datos = signal<any[]>([]);
    loading = signal(false);
    tabActiva = signal('MES');

    // Filtros
    anio = signal(new Date().getFullYear());
    mes = signal(new Date().getMonth() + 1);
    moduloSeleccionado = signal('VENTA'); // VENTA, INGRESO_DIVERSO, SALIDA_DIVERSA

    sucursalId = signal<string | null>(null);
    sucursalOptions = signal<any[]>([]);
    puntoId = signal<string | null>(null);
    puntoOptions = signal<any[]>([{ label: 'Todos', value: 'TODOS' }]);

    // Paginación y Filtrado Principal
    tieneDatosIniciales = signal<boolean>(false);
    paginaActualPrincipal = signal<number>(0);
    tamanoPaginaPrincipal = signal<number>(10);
    searchQueryPrincipal = signal<string>('');
    searchTypePrincipal = signal<string>('ALL');
    searchOptionsPrincipal = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Tipo Documento', value: 'TIPO_DOC' },
        { label: 'Serie', value: 'SERIE' }
    ];
    datosPaginados = signal<any[]>([]);

    // Paginación y Filtrado Detalle
    tieneDetalleInicial = signal<boolean>(false);
    paginaActualDetalle = signal<number>(0);
    tamanoPaginaDetalle = signal<number>(10);
    searchQueryDetalle = signal<string>('');
    searchTypeDetalle = signal<string>('ALL');
    searchOptionsDetalle = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Nro. Comprobante', value: 'NUMERO' },
        { label: 'Usuario', value: 'USUARIO' }
    ];
    detallePaginado = signal<any[]>([]);

    meses = [
        { value: 1, label: 'ENERO' }, { value: 2, label: 'FEBRERO' }, { value: 3, label: 'MARZO' },
        { value: 4, label: 'ABRIL' }, { value: 5, label: 'MAYO' }, { value: 6, label: 'JUNIO' },
        { value: 7, label: 'JULIO' }, { value: 8, label: 'AGOSTO' }, { value: 9, label: 'SETIEMBRE' },
        { value: 10, label: 'OCTUBRE' }, { value: 11, label: 'NOVIEMBRE' }, { value: 12, label: 'DICIEMBRE' }
    ];

    modulos = [
        { value: 'VENTA', label: 'VENTAS (BOLETA/FACTURA)' },
        { value: 'INGRESO_DIVERSO', label: 'INGRESOS DIVERSOS' },
        { value: 'SALIDA_DIVERSA', label: 'SALIDAS DIVERSAS' }
    ];

    anios = [2023, 2024, 2025, 2026];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Ventas', route: '/ventas' },
        { label: 'Reportes', route: '/ventas/reportes' },
        { label: 'Correlatividad' }
    ];

    columns: Columna[] = [
        { field: 'tipoDocDescripcion', header: 'Tipo Doc.', tipo: 'text', subField: [] },
        { field: 'serie', header: 'Serie', tipo: 'text', subField: [] },
        { field: 'cantidad', header: 'Cant.', tipo: 'text', subField: [] },
        { field: 'desde', header: 'Desde', tipo: 'text', subField: [] },
        { field: 'hasta', header: 'Hasta', tipo: 'text', subField: [] },
        { field: 'baseImp', header: 'Base Imp.', tipo: 'currency', subField: [] },
        { field: 'valorInaf', header: 'Inafecto', tipo: 'currency', subField: [] },
        { field: 'valorExo', header: 'Exonerado', tipo: 'currency', subField: [] },
        { field: 'igv', header: 'Igv.', tipo: 'currency', subField: [] },
        { field: 'total', header: 'Total', tipo: 'currency', subField: [] }
    ];

    showModalDetalle = signal(false);
    selectedRow = signal<any>(null);
    detalleDatos = signal<any[]>([]);
    loadingDetalle = signal(false);

    // Totales calculados para la paginación principal
    totalRegistrosPrincipal = signal<number>(0);
    totalPagesPrincipal = computed(() => Math.ceil(this.totalRegistrosPrincipal() / this.tamanoPaginaPrincipal()));

    // Totales calculados para la paginación de detalle
    totalRegistrosDetalle = signal<number>(0);
    totalPagesDetalle = computed(() => Math.ceil(this.totalRegistrosDetalle() / this.tamanoPaginaDetalle()));

    constructor(
        private ventaService: VentaRegistroService,
        private alertService: AlertService,
        private authService: AuthService,
        private puntoService: PuntoService,
        private sucursalService: SucursalService,
        public sidebarService: SidebarService
    ) { }
    ngOnInit(): void {
        const tokenSuc = this.authService.getSucursalIdFromToken();
        if (tokenSuc) {
            this.sucursalId.set(tokenSuc.toString());
        }
        this.cargarSucursales();
        this.cargarReporte();
    }
    // --- Métodos de Segmentación y Filtrado Explícitos ---
    aplicarFiltroYPaginaPrincipal(): void {
        const d = this.datos();
        const q = this.searchQueryPrincipal().toLowerCase().trim();
        const type = this.searchTypePrincipal();

        // 1. Filtrar
        let filtrados = d;
        if (q) {
            filtrados = d.filter(item => {
                const tipoMatch = (item.tipoDocDescripcion || '').toLowerCase().includes(q);
                const serieMatch = (item.serie || '').toLowerCase().includes(q);

                if (type === 'TIPO_DOC') return tipoMatch;
                if (type === 'SERIE') return serieMatch;
                return tipoMatch || serieMatch;
            });
        }
        // 2. Total
        this.totalRegistrosPrincipal.set(filtrados.length);

        // 3. Segmentar
        const inicio = this.paginaActualPrincipal() * this.tamanoPaginaPrincipal();
        const fin = inicio + this.tamanoPaginaPrincipal();
        this.datosPaginados.set(filtrados.slice(inicio, fin));
    }
    aplicarFiltroYPaginaDetalle(): void {
        const d = this.detalleDatos();
        const q = this.searchQueryDetalle().toLowerCase().trim();
        const type = this.searchTypeDetalle();

        // 1. Filtrar
        let filtrados = d;
        if (q) {
            filtrados = d.filter(item => {
                const numeroMatch = (item.numeroDocumento || '').toLowerCase().includes(q);
                const usuarioMatch = (item.idUser || '').toLowerCase().includes(q);

                if (type === 'NUMERO') return numeroMatch;
                if (type === 'USUARIO') return usuarioMatch;
                return numeroMatch || usuarioMatch;
            });
        }
        // 2. Total
        this.totalRegistrosDetalle.set(filtrados.length);

        // 3. Segmentar
        const inicio = this.paginaActualDetalle() * this.tamanoPaginaDetalle();
        const fin = inicio + this.tamanoPaginaDetalle();
        this.detallePaginado.set(filtrados.slice(inicio, fin));
    }
    cargarReporte(): void {
        this.loading.set(true);

        let fInicio: string;
        let fFin: string;

        if (this.tabActiva() === 'MES') {
            const firstDay = new Date(this.anio(), this.mes() - 1, 1);
            const lastDay = new Date(this.anio(), this.mes(), 0);
            fInicio = firstDay.toISOString().split('T')[0];
            fFin = lastDay.toISOString().split('T')[0];
        } else {
            // Histórico (Sin tiempo)
            fInicio = '2000-01-01';
            fFin = '2099-12-31';
        }
        const q = this.searchQueryPrincipal();
        const type = this.searchTypePrincipal();
        const page = this.paginaActualPrincipal();
        const size = this.tamanoPaginaPrincipal();

        this.ventaService.obtenerCorrelatividad(fInicio, fFin, this.getSucursalParam(), this.getPuntoParam(), q, type, page, size).subscribe({
            next: (res) => {
                if (res.success) {
                    let filteredData = res.data;
                    // Si estamos en la pestaña de tipo doc, filtramos por el módulo seleccionado
                    if (this.tabActiva() === 'TIPO_DOC') {
                        const mod = this.moduloSeleccionado();
                        if (mod === 'VENTA') {
                            filteredData = res.data.filter((d: any) => !['ID', 'SD'].includes(d.tipoDoc));
                        } else if (mod === 'INGRESO_DIVERSO') {
                            filteredData = res.data.filter((d: any) => d.tipoDoc === 'ID');
                        } else if (mod === 'SALIDA_DIVERSA') {
                            filteredData = res.data.filter((d: any) => d.tipoDoc === 'SD');
                        }
                    }
                    this.datos.set(filteredData);
                    this.tieneDatosIniciales.set(filteredData.length > 0);
                    this.aplicarFiltroYPaginaPrincipal();
                }
                this.loading.set(false);
            },
            error: (err) => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudo cargar el reporte de correlatividad');
            }
        });
    }
    onFiltrar(): void {
        this.cargarReporte();
    }
    cambiarTab(tab: string): void {
        this.tabActiva.set(tab);
        this.paginaActualPrincipal.set(0);
        this.searchQueryPrincipal.set('');
        this.cargarReporte();
    }
    cargarDetalle(row: any): void {
        this.loadingDetalle.set(true);

        let fInicio: string;
        let fFin: string;

        if (this.tabActiva() === 'MES') {
            const firstDay = new Date(this.anio(), this.mes() - 1, 1);
            const lastDay = new Date(this.anio(), this.mes(), 0);
            fInicio = firstDay.toISOString().split('T')[0];
            fFin = lastDay.toISOString().split('T')[0];
        } else {
            fInicio = '2000-01-01';
            fFin = '2099-12-31';
        }
        const q = this.searchQueryDetalle();
        const type = this.searchTypeDetalle();
        const page = this.paginaActualDetalle();
        const size = this.tamanoPaginaDetalle();

        this.ventaService.obtenerDetalleCorrelatividad(
            fInicio,
            fFin,
            this.getSucursalParam(),
            row.tipoDoc,
            row.serie,
            this.getPuntoParam(),
            q,
            type,
            page,
            size
        ).subscribe({
            next: (res) => {
                if (res.success) {
                    this.detalleDatos.set(res.data);
                    this.tieneDetalleInicial.set(res.data.length > 0);
                    this.aplicarFiltroYPaginaDetalle();
                }
                this.loadingDetalle.set(false);
            },
            error: (err) => {
                this.loadingDetalle.set(false);
                this.alertService.error('Error', 'No se pudo cargar el detalle de correlatividad');
            }
        });
    }
    abrirDetalle(row: any): void {
        this.selectedRow.set(row);
        this.showModalDetalle.set(true);
        
        // Resetear búsqueda y paginación de detalle
        this.paginaActualDetalle.set(0);
        this.searchQueryDetalle.set('');
        this.tieneDetalleInicial.set(false);
        this.detalleDatos.set([]);

        this.cargarDetalle(row);
    }
    cerrarDetalle(): void {
        this.showModalDetalle.set(false);
        this.selectedRow.set(null);
        this.detalleDatos.set([]);
    }
    datosFiltradosSinPaginacion(): any[] {
        const d = this.datos();
        const q = this.searchQueryPrincipal().toLowerCase().trim();
        const type = this.searchTypePrincipal();
        if (!q) return d;
        return d.filter(item => {
            const tipoMatch = (item.tipoDocDescripcion || '').toLowerCase().includes(q);
            const serieMatch = (item.serie || '').toLowerCase().includes(q);
            if (type === 'TIPO_DOC') return tipoMatch;
            if (type === 'SERIE') return serieMatch;
            return tipoMatch || serieMatch;
        });
    }
    getTotalSum(field: string): number {
        return this.datosFiltradosSinPaginacion().reduce((sum, current) => sum + (current[field] || 0), 0);
    }
    // --- Controladores de Eventos Principal ---
    handleSearchPrincipal(event: { q: string, type: string }) {
        this.searchQueryPrincipal.set(event.q);
        this.searchTypePrincipal.set(event.type);
        this.paginaActualPrincipal.set(0);
        this.cargarReporte();
    }
    onPageChangePrincipal(page: number) {
        this.paginaActualPrincipal.set(page);
        this.cargarReporte();
    }
    onPageSizeChangePrincipal(size: number) {
        this.tamanoPaginaPrincipal.set(Number(size));
        this.paginaActualPrincipal.set(0);
        this.cargarReporte();
    }
    // --- Controladores de Eventos Detalle ---
    handleSearchDetalle(event: { q: string, type: string }) {
        this.searchQueryDetalle.set(event.q);
        this.searchTypeDetalle.set(event.type);
        this.paginaActualDetalle.set(0);
        const row = this.selectedRow();
        if (row) {
            this.cargarDetalle(row);
        }
    }
    onPageChangeDetalle(page: number) {
        this.paginaActualDetalle.set(page);
        const row = this.selectedRow();
        if (row) {
            this.cargarDetalle(row);
        }
    }
    onPageSizeChangeDetalle(size: number) {
        this.tamanoPaginaDetalle.set(Number(size));
        this.paginaActualDetalle.set(0);
        const row = this.selectedRow();
        if (row) {
            this.cargarDetalle(row);
        }
    }
    onExportarExcel(): void {
        let fInicio: string;
        let fFin: string;

        if (this.tabActiva() === 'MES') {
            const firstDay = new Date(this.anio(), this.mes() - 1, 1);
            const lastDay = new Date(this.anio(), this.mes(), 0);
            fInicio = firstDay.toISOString().split('T')[0];
            fFin = lastDay.toISOString().split('T')[0];
        } else {
            fInicio = '2000-01-01';
            fFin = '2099-12-31';
        }
        const tipoDoc = this.tabActiva() === 'TIPO_DOC' ? this.moduloSeleccionado() : '';

        this.ventaService.exportarExcelCorrelatividad(fInicio, fFin, this.getSucursalParam(), tipoDoc, this.getPuntoParam()).subscribe({
            next: (response: any) => {
                const contentDisposition = response.headers.get('content-disposition');
                let fileName = `correlatividad_${this.anio()}_${this.mes()}.xlsx`;
                
                if (contentDisposition) {
                    const fileNameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                    const matches = fileNameRegex.exec(contentDisposition);
                    if (matches != null && matches[1]) {
                        fileName = matches[1].replace(/['"]/g, '');
                    }
                }
                const url = window.URL.createObjectURL(response.body);
                const a = document.createElement('a');
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            },
            error: (err) => {
                this.alertService.error('Error', 'No se pudo generar el Excel de correlatividad');
            }
        });
    }

    getSucursalParam(): number | undefined {
        const val = this.sucursalId();
        return (val && val !== 'TODOS') ? parseInt(val) : undefined;
    }

    getPuntoParam(): number | undefined {
        const val = this.puntoId();
        return (val && val !== 'TODOS') ? parseInt(val) : undefined;
    }

    onSucursalChange(val: string): void {
        this.sucursalId.set(val);
        if (val && val !== "TODOS") { this.cargarPuntosDeVenta(Number(val)); } else { this.puntoOptions.set([{ label: "TODOS", value: "TODOS" }]); this.puntoId.set("TODOS"); }
        this.cargarReporte();
    }

    onPuntoChange(): void {
        this.cargarReporte();
    }

    cargarSucursales(): void {
        this.sucursalService.getActivas().subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const mapped = res.data.map((s: any) => ({
                        label: s.nombreSucursal,
                        value: s.idSucursal.toString()
                    }));
                    this.sucursalOptions.set([{ label: 'TODOS', value: 'TODOS' }, ...mapped]);
                    if (this.sucursalId() && this.sucursalId() !== 'TODOS') {
                        this.cargarPuntosDeVenta(Number(this.sucursalId()));
                    }
                }
            }
        });
    }

    cargarPuntosDeVenta(idSucursal: number): void {
        this.puntoService.buscarPorSucursal(idSucursal).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const mapped = res.data.map((p: any) => ({
                        label: p.nombre,
                        value: p.puntoId.toString()
                    }));
                    this.puntoOptions.set([{ label: 'TODOS', value: 'TODOS' }, ...mapped]);
                }
            }
        });
    }
}
