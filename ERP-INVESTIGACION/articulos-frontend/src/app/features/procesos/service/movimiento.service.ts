import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class InventarioService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/inventario`;

    listarAjustesAgrupados(idSucursal: number, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<any>>> {
        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/ajuste?idSucursal=${idSucursal}&page=${page}&size=${size}`);
    }

    registrarMovimientoMixtoBatch(request: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/movimiento-mixto`, request);
    }

    registrarIngresoDiverso(
        idCatalogo: number,
        idSucursal: number,
        idAlmacen: number,
        cantidad: number,
        costo: number,
        nroLote: string,
        fechaVenc: string | null,
        motivo: string,
        idUsuario: number,
        idClasificacion?: number
    ): Observable<ApiResponse<void>> {
        let params = new HttpParams()
            .set('idCatalogo', idCatalogo.toString())
            .set('idSucursal', idSucursal.toString())
            .set('idAlmacen', idAlmacen.toString())
            .set('cantidad', cantidad.toString())
            .set('costo', costo.toString())
            .set('nroLote', nroLote)
            .set('motivo', motivo)
            .set('idUsuario', idUsuario.toString());

        if (fechaVenc) {
            params = params.set('fechaVenc', fechaVenc);
        }

        if (idClasificacion) {
            params = params.set('idClasificacion', idClasificacion.toString());
        }

        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/ingreso-diverso`, null, { params });
    }

    registrarSalidaDiversa(
        idProducto: number,
        cantidad: number,
        motivo: string,
        idUsuario: number
    ): Observable<ApiResponse<void>> {
        const params = new HttpParams()
            .set('idProducto', idProducto.toString())
            .set('cantidad', cantidad.toString())
            .set('motivo', motivo)
            .set('idUsuario', idUsuario.toString());

        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/salida-diversa`, null, { params });
    }

    listarMovimientos(
        idSucursal: number,
        desde?: string,
        hasta?: string,
        signo?: string,
        nombre?: string,
        idClasificacion?: number,
        page: number = 0,
        size: number = 10
    ): Observable<ApiResponse<PageResponse<any>>> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (desde) params = params.set('desde', desde);
        if (hasta) params = params.set('hasta', hasta);
        if (signo) params = params.set('signo', signo);
        if (nombre) params = params.set('nombre', nombre);
        if (idClasificacion) params = params.set('idClasificacion', idClasificacion.toString());

        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/movimientos`, { params });
    }

    obtenerClasificaciones(tipo: string): Observable<ApiResponse<PageResponse<any>>> {
        return this.http.get<ApiResponse<PageResponse<any[]>>>(`${this.apiUrl}/clasificaciones?tipo=${tipo}`);
    }

    obtenerCostoPrevioLote(idCatalogo: number, nroLote: string): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/costo-previo-lote?idCatalogo=${idCatalogo}&nroLote=${nroLote}`);
    }

    obtenerUltimoCosto(idCatalogo: number, idSucursal: number): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/ultimo-costo?idCatalogo=${idCatalogo}&idSucursal=${idSucursal}`);
    }

    buscarMovimientosDiversos(
        idSucursal: number | string,
        desde?: string,
        hasta?: string,
        estado?: string,
        buscar?: string,
        page: number = 0,
        size: number = 10,
        idPuntoVenta?: number | string,
        tipoDocumento?: string,
        serie?: string,
        numero?: string
    ): Observable<ApiResponse<PageResponse<any>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (idSucursal !== 'TODOS' && idSucursal !== 0 && idSucursal !== null) {
            params = params.set('idSucursal', idSucursal.toString());
        }

        if (idPuntoVenta !== undefined && idPuntoVenta !== 'TODOS' && idPuntoVenta !== null) {
            params = params.set('idPuntoVenta', idPuntoVenta.toString());
        }

        if (desde) params = params.set('desde', desde);
        if (hasta) params = params.set('hasta', hasta);
        if (estado) params = params.set('estado', estado);
        if (buscar) params = params.set('buscar', buscar);
        if (tipoDocumento && tipoDocumento !== 'TODOS') params = params.set('tipoDocumento', tipoDocumento);
        if (serie) params = params.set('serie', serie);
        if (numero) params = params.set('numero', numero);

        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/movimientos-diversos`, { params });
    }

    obtenerDetallesDiverso(idMovimiento: number): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/movimientos-diversos/${idMovimiento}/detalles`);
    }

    obtenerMovimientoDiverso(idMovimiento: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/movimientos-diversos/${idMovimiento}`);
    }

    solicitarAnulacion(idMovimiento: number, motivo: string): Observable<ApiResponse<void>> {
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/movimientos-diversos/${idMovimiento}/solicitar-anulacion`, { motivo });
    }
}
