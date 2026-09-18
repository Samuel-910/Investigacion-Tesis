import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { Clasificacion, ClasificacionRequest } from '../models/clasificacion.model';

@Injectable({
    providedIn: 'root'
})
export class ClasificacionService {
    private apiUrl = `${environment.apiUrl}/inventario/clasificaciones`;

    constructor(private http: HttpClient) { }

    listar(tipo?: string, q?: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Clasificacion>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        if (tipo) params = params.set('tipo', tipo);
        if (q) params = params.set('q', q);
        return this.http.get<ApiResponse<PageResponse<Clasificacion>>>(this.apiUrl, { params });
    }

    crear(request: ClasificacionRequest): Observable<ApiResponse<Clasificacion>> {
        return this.http.post<ApiResponse<Clasificacion>>(this.apiUrl, request);
    }

    actualizar(id: number, request: ClasificacionRequest): Observable<ApiResponse<Clasificacion>> {
        return this.http.put<ApiResponse<Clasificacion>>(`${this.apiUrl}/${id}`, request);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
