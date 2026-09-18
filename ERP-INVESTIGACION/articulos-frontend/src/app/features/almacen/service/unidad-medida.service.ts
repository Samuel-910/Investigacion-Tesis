import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { UnidadMedidaRequest, UnidadMedidaResponse } from '../../configuraciones/models/unidad-medida.model';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class UnidadMedidaService {
    private apiUrl = `${environment.apiUrl}/unidades-medida`;

    constructor(private http: HttpClient) { }

    listarActivas(): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/activas?page=0&size=1000`);
    }

    listar(page: number, size: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}?page=${page}&size=${size}`);
    }

    crear(request: UnidadMedidaRequest): Observable<ApiResponse<UnidadMedidaResponse>> {
        return this.http.post<ApiResponse<UnidadMedidaResponse>>(this.apiUrl, request);
    }

    actualizar(id: number, request: UnidadMedidaRequest): Observable<ApiResponse<UnidadMedidaResponse>> {
        return this.http.put<ApiResponse<UnidadMedidaResponse>>(`${this.apiUrl}/${id}`, request);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
