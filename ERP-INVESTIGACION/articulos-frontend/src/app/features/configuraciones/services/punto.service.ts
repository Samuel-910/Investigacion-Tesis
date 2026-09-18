import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Punto, PuntoRequest } from '../models/punto.model';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class PuntoService {
    private apiUrl = `${environment.apiUrl}/puntos`;

    constructor(private http: HttpClient) { }

    listarTodos(idSucursal: number, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<Punto>>> {
        const params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<ApiResponse<PageResponse<Punto>>>(this.apiUrl, { params });
    }

    listarTodosSinPaginacion(idSucursal: number): Observable<ApiResponse<Punto[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<Punto[]>>(`${this.apiUrl}/list`, { params });
    }

    obtenerPorId(id: number): Observable<ApiResponse<Punto>> {
        return this.http.get<ApiResponse<Punto>>(`${this.apiUrl}/${id}`);
    }

    crear(punto: PuntoRequest): Observable<ApiResponse<Punto>> {
        return this.http.post<ApiResponse<Punto>>(this.apiUrl, punto);
    }

    actualizar(id: number, punto: PuntoRequest): Observable<ApiResponse<Punto>> {
        return this.http.put<ApiResponse<Punto>>(`${this.apiUrl}/${id}`, punto);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    buscarPorSucursal(idSucursal: number): Observable<ApiResponse<Punto[]>> {
        const params = new HttpParams().set('idSucursal', idSucursal.toString());
        return this.http.get<ApiResponse<Punto[]>>(`${this.apiUrl}/buscar/sucursal`, { params });
    }

    buscar(q: string, idSucursal: number, page: number = 0, size: number = 10, tipo?: string): Observable<ApiResponse<PageResponse<Punto>>> {
        let params = new HttpParams()
            .set('q', q)
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (tipo) {
            params = params.set('tipo', tipo);
        }

        return this.http.get<ApiResponse<PageResponse<Punto>>>(`${this.apiUrl}/search`, { params });
    }

    buscarTodos(): Observable<ApiResponse<Punto[]>> {
        return this.http.get<ApiResponse<Punto[]>>(`${this.apiUrl}/buscar`);
    }
}
