import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { EstadoGeneral } from '../../../core/interfaces/estado-general.interface';

export interface DataPunto {
    label: string;
    value: number;
}

export interface ReporteVentaDetalle {
    total: number;
    data: DataPunto[];
}

export interface ReporteCompraDetalle {
    total: number;
    data: DataPunto[];
}

export interface ReporteDeudaDetalle {
    total: number;
    data: DataPunto[];
}

export interface ReporteResumen {
    totalVentas: number;
    totalCompras: number;
    totalVencido: number;
    totalPorPagar: number;
    ventasMensuales: DataPunto[];
    comprasMensuales: DataPunto[];
    deudasPorProveedor: DataPunto[];
    topProductosVendidos: DataPunto[];
}

export interface SireVenta {
    periodo: string;
    carSunat: string;
    fecha: string;
    fechaVencimiento: string;
    tipoDoc: string;
    serie: string;
    numero: string;
    numeroFinal: string;
    nroDocCli: string;
    cliente: string;
    exportacion: number;
    baseImponible: number;
    descuentoBase: number;
    igv: number;
    descuentoIgv: number;
    exonerado: number;
    inafecto: number;
    isc: number;
    baseArroz: number;
    igvArroz: number;
    icbper: number;
    otrosConceptos: number;
    total: number;
    moneda: string;
    tipoCambio: number;
    fechaRef?: string;
    tipoRef?: string;
    serieRef?: string;
    numeroRef?: string;
    estado: string | EstadoGeneral;
}

export interface SireCompra {
    periodo: string;
    carSunat: string;
    fecha: string;
    fechaVencimiento: string;
    tipoDoc: string;
    serie: string;
    numero: string;
    numeroFinal: string;
    nroDocProv: string;
    proveedor: string;
    exportacion: number;
    baseImponible: number;
    descuentoBase: number;
    igv: number;
    descuentoIgv: number;
    exonerado: number;
    inafecto: number;
    isc: number;
    baseArroz: number;
    igvArroz: number;
    icbper: number;
    otrosConceptos: number;
    total: number;
    moneda: string;
    tipoCambio: number;
    fechaRef?: string;
    tipoRef?: string;
    serieRef?: string;
    numeroRef?: string;
    estado: string | EstadoGeneral;
}

export interface Estadisticas {
    cantidadDocumentos: number;
    ticketPromedio: number;
    nuevosRegistros: number;
    anulaciones: number;
}

export interface SeriesData {
    name: string;
    data: number[];
}

export interface MultiseriesData {
    labels: string[];
    series: SeriesData[];
}

export interface WidgetConfig {
    titulo: string;
    tipoGrafico: 'BAR' | 'LINE' | 'DONUT' | 'AREA' | 'PIE' | 'COLUMN' | 'SCATTER' | 'RADAR' | 'POLAR_AREA' | 'GAUGE' | 'TREEMAP' | 'HEATMAP' | 'FUNNEL' | 'WATERFALL' | 'CARD' | 'TABLE';
    metrica: 'VENTAS' | 'VENTAS_CANTIDAD' | 'COMPRAS' | 'COMPRAS_CANTIDAD' | 'DEUDA' | 'PRODUCTOS' | 'GASTOS' | 'INGRESOS' | 'STOCK_FISICO' | 'VALOR_INVENTARIO' | 'PRODUCTOS_VENCIDOS' | 'SALDO_CAJA' | 'VENTAS_VS_COMPRAS' | 'UTILIDAD_NETA' | 'TICKET_PROMEDIO' | 'DESCUENTOS_TOTALES' | 'TICKET_COMPRA_PROMEDIO' | 'STOCK_CRITICO' | 'RENTABILIDAD' | 'ARTICULOS_PERDIDA';
    dimension: 'TIEMPO_MES' | 'TIEMPO_DIA' | 'PROVEEDOR' | 'CATEGORIA' | 'PRODUCTO_NOMBRE' | 'SUCURSAL' | 'USUARIO_REGISTRO' | 'METODO_PAGO' | 'CLIENTE' | 'DIA_SEMANA' | 'MARCA' | 'CONCEPTO_GASTO';
    orden: number;
    columns: number;
    height: number;
}

export interface DashboardConfig {
    id: number;
    nombre: string;
    widgets: WidgetConfig[];
}

export interface DashboardOptionsDTO {
    metricas: WidgetConfig['metrica'][];
    dimensiones: WidgetConfig['dimension'][];
}

@Injectable({
    providedIn: 'root'
})
export class ReporteService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/reportes`;

    obtenerOpciones(categoria: string): Observable<ApiResponse<DashboardOptionsDTO>> {
        const params = new HttpParams().set('categoria', categoria);
        return this.http.get<ApiResponse<DashboardOptionsDTO>>(`${this.apiUrl}/dashboard/options`, { params });
    }

    obtenerResumen(idSucursal: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<ReporteResumen>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS');
        return this.http.get<ApiResponse<ReporteResumen>>(`${this.apiUrl}/resumen`, { params });
    }

    obtenerVentasMensuales(idSucursal: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS');
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/ventas`, { params });
    }

    obtenerComprasMensuales(idSucursal: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS');
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/compras`, { params });
    }

    obtenerDeudas(): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/cuentas-pagar`);
    }

    obtenerProductos(idSucursal: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<DataPunto[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS');
        return this.http.get<ApiResponse<DataPunto[]>>(`${this.apiUrl}/productos`, { params });
    }

    // DASHBOARD
    obtenerConfiguracion(idSucursal: number, categoria: string = 'GENERAL'): Observable<ApiResponse<DashboardConfig>> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('categoria', categoria);
        return this.http.get<ApiResponse<DashboardConfig>>(`${this.apiUrl}/dashboard/current`, { params });
    }

    obtenerConfiguracionPorId(id: number): Observable<ApiResponse<DashboardConfig>> {
        return this.http.get<ApiResponse<DashboardConfig>>(`${this.apiUrl}/dashboard/${id}`);
    }

    guardarConfiguracion(config: any, idUsuario: number, idSucursal: number, categoria: string = 'GENERAL'): Observable<ApiResponse<DashboardConfig>> {
        const params = new HttpParams()
            .set('idUsuario', idUsuario.toString())
            .set('idSucursal', idSucursal.toString());

        const payload = { ...config, categoria };
        return this.http.post<ApiResponse<DashboardConfig>>(`${this.apiUrl}/dashboard/save`, payload, { params });
    }

    obtenerDatosDinamicos(metrica: string, dimension: string, idSucursal: string, idPuntoVenta: string): Observable<ApiResponse<DataPunto[]>> {
        let params = new HttpParams()
            .set('metrica', metrica)
            .set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS');
        if (dimension) {
            params = params.set('dimension', dimension);
        }
        return this.http.get<ApiResponse<DataPunto[]>>(`${this.apiUrl}/dashboard/data`, { params });
    }

    obtenerDatosMultiseries(metrica: string, dimension: string, idSucursal: string, idPuntoVenta: string): Observable<ApiResponse<MultiseriesData[]>> {
        let params = new HttpParams()
            .set('metrica', metrica)
            .set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS');
        if (dimension) {
            params = params.set('dimension', dimension);
        }
        return this.http.get<ApiResponse<MultiseriesData[]>>(`${this.apiUrl}/dashboard/multidata`, { params });
    }

    obtenerCategorias(idSucursal: number): Observable<ApiResponse<string[]>> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<string[]>>(`${this.apiUrl}/dashboard/categories`, { params });
    }

    exportarRVIE(idSucursal: string, fechaInicio: string, fechaFin: string, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS')
            .set('fechaInicio', fechaInicio)
            .set('fechaFin', fechaFin);
        return this.http.get(`${this.apiUrl}/export-rvie`, { params, responseType: 'blob' });
    }

    exportarRCE(idSucursal: string, fechaInicio: string, fechaFin: string, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS')
            .set('fechaInicio', fechaInicio)
            .set('fechaFin', fechaFin);
        return this.http.get(`${this.apiUrl}/export-rce`, { params, responseType: 'blob' });
    }

    exportarProductosExcel(idSucursal: string, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS');
        return this.http.get(`${this.apiUrl}/export-productos`, { params, responseType: 'blob' });
    }

    obtenerSireVentas(idSucursal: string, inicio: string, fin: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<SireVenta[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('inicio', inicio).set('fin', fin);
        return this.http.get<ApiResponse<SireVenta[]>>(`${this.apiUrl}/sire-ventas`, { params });
    }

    obtenerSireCompras(idSucursal: string, inicio: string, fin: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<SireCompra[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('inicio', inicio).set('fin', fin);
        return this.http.get<ApiResponse<SireCompra[]>>(`${this.apiUrl}/sire-compras`, { params });
    }

    obtenerStatsVentas(idSucursal: string, inicio: string, fin: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<Estadisticas>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('inicio', inicio).set('fin', fin);
        return this.http.get<ApiResponse<Estadisticas>>(`${this.apiUrl}/stats-ventas`, { params });
    }

    obtenerStatsCompras(idSucursal: string, inicio: string, fin: string, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<Estadisticas>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('inicio', inicio).set('fin', fin);
        return this.http.get<ApiResponse<Estadisticas>>(`${this.apiUrl}/stats-compras`, { params });
    }

    exportarReporteCajas(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get(`${this.apiUrl}/export-cajas`, { params, responseType: 'blob' });
    }

    exportarReporteDescuentos(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get(`${this.apiUrl}/export-descuentos`, { params, responseType: 'blob' });
    }

    exportarKardex(idSucursal: string, idProducto: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idProducto', idProducto).set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get(`${this.apiUrl}/export-kardex`, { params, responseType: 'blob' });
    }

    exportarAnulados(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<Blob> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get(`${this.apiUrl}/export-anulados`, { params, responseType: 'blob' });
    }

    obtenerReporteCajas(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/cajas`, { params });
    }

    obtenerReporteDescuento(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/descuento`, { params });
    }

    obtenerComprobantesAnulados(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/comprobantes-anulados`, { params });
    }

    obtenerNotasCredito(idSucursal: string, mes: number, anio: number, idPuntoVenta: string = 'TODOS'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('idSucursal', idSucursal).set('idPuntoVenta', idPuntoVenta || 'TODOS').set('idPuntoVenta', idPuntoVenta || 'TODOS').set('mes', mes.toString()).set('anio', anio.toString());
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/nota-credito`, { params });
    }

    // Configuración de Columnas en BD
    guardarConfiguracionColumnas(reportKey: string, idUsuario: number, idSucursal: number, visibleColumns: string): Observable<ApiResponse<string>> {
        const params = new HttpParams()
            .set('reportKey', reportKey)
            .set('idUsuario', idUsuario.toString())
            .set('idSucursal', idSucursal.toString())
            .set('visibleColumns', visibleColumns);
        
        return this.http.post<ApiResponse<string>>(`${this.apiUrl}/column-config`, null, { params });
    }

    obtenerConfiguracionColumnas(reportKey: string, idUsuario: number, idSucursal: number): Observable<ApiResponse<string>> {
        const params = new HttpParams()
            .set('reportKey', reportKey)
            .set('idUsuario', idUsuario.toString())
            .set('idSucursal', idSucursal.toString());
            
        return this.http.get<ApiResponse<string>>(`${this.apiUrl}/column-config`, { params });
    }
}
