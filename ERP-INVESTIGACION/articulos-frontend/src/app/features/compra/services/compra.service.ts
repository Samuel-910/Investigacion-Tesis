import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CompraRequest, CompraResponse, OrdenCreateRequest } from '../models/compra.model';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class CompraService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/compras`;

    registrar(request: CompraRequest): Observable<ApiResponse<CompraResponse>> {
        return this.http.post<ApiResponse<CompraResponse>>(this.apiUrl, request);
    }

    crearOrden(request: OrdenCreateRequest): Observable<ApiResponse<CompraResponse>> {
        return this.http.post<ApiResponse<CompraResponse>>(`${this.apiUrl}/orden`, request);
    }

    actualizar(id: number, request: CompraRequest): Observable<ApiResponse<CompraResponse>> {
        return this.http.put<ApiResponse<CompraResponse>>(`${this.apiUrl}/${id}`, request);
    }

    obtener(id: number): Observable<ApiResponse<CompraResponse>> {
        return this.http.get<ApiResponse<CompraResponse>>(`${this.apiUrl}/${id}`);
    }

    listar(idSucursal: number, page: number = 0, size: number = 10, estado: string | null = null): Observable<ApiResponse<any>> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());
        if (estado) params = params.set('estado', estado);
        return this.http.get<ApiResponse<any>>(this.apiUrl, { params });
    }

    listarOrdenes(idSucursal: number, page: number = 0, size: number = 10): Observable<ApiResponse<any>> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/orden`, { params });
    }

    buscar(
        idSucursal: number,
        idProveedor?: number,
        fechaInicio?: string,
        fechaFin?: string,
        estado?: string,
        page: number = 0,
        size: number = 10
    ): Observable<ApiResponse<any>> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (idProveedor) params = params.set('idProveedor', idProveedor.toString());
        if (fechaInicio) params = params.set('fechaInicio', fechaInicio);
        if (fechaFin) params = params.set('fechaFin', fechaFin);
        if (estado) params = params.set('estado', estado);

        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/buscar`, { params });
    }

    // Método legacy para no romper todo de golpe si se usa en otros lados, pero lo ideal es migrarlo
    buscarLegacy(q: string, type: string, page: number = 0, size: number = 10, estado: string | null = null): Observable<ApiResponse<any>> {
        // Por ahora redirigimos a listar si no tenemos el idSucursal aquí, 
        // pero lo ideal es inyectar AuthService o pasar el ID.
        return this.listar(1, page, size, estado);
    }

    buscarDetallesPorProducto(term: string, idSucursal: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/detalles/buscar-producto`, { params: { term, idSucursal: idSucursal.toString() } });
    }

    obtenerPrecioMaximoHistorico(idCatalogo: number): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/producto/${idCatalogo}/precio-maximo`);
    }

    obtenerHistorialPorProducto(idCatalogo: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/producto/${idCatalogo}/historial`);
    }

    seleccionarGanadora(id: number, nombreGrupo: string): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${id}/seleccionar-ganadora?nombreGrupo=${nombreGrupo}`, {});
    }

    anular(id: number, motivo: string = ''): Observable<ApiResponse<void>> {
        const params = new HttpParams().set('motivo', motivo);
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`, { params });
    }

    actualizarOrden(id: number, orden: any): Observable<ApiResponse<any>> {
        return this.http.put<ApiResponse<any>>(`${this.apiUrl}/orden/${id}`, orden);
    }

    imprimir(id: number): Observable<ApiResponse<string>> {
        return this.http.get<ApiResponse<string>>(`${this.apiUrl}/${id}/imprimir`);
    }

    imprimirGrupo(nombreGrupo: string, idSucursal: number): Observable<ApiResponse<string>> {
        const params = new HttpParams()
            .set('nombreGrupo', nombreGrupo)
            .set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<string>>(`${this.apiUrl}/grupos/imprimir`, { params });
    }

    listarGrupos(idSucursal: number, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<string>>> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<ApiResponse<PageResponse<string>>>(`${this.apiUrl}/grupos`, { params });
    }

    obtenerProductosPorGrupo(nombreGrupo: string, idSucursal: number): Observable<ApiResponse<any>> {
        const params = new HttpParams()
            .set('nombreGrupo', nombreGrupo)
            .set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/grupos/productos`, { params });
    }

    obtenerProveedoresPorGrupo(nombreGrupo: string, idSucursal: number): Observable<ApiResponse<number[]>> {
        const params = new HttpParams()
            .set('nombreGrupo', nombreGrupo)
            .set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/grupos/proveedores`, { params });
    }
}
