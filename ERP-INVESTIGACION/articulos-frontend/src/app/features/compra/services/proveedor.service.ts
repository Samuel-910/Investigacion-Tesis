import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Proveedor } from '../models/proveedor.model';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class ProveedorService {
    private apiUrl = `${environment.apiUrl}/proveedores`;

    constructor(private http: HttpClient) { }

    listarTodos(page: number = 0, size: number = 10, sortBy: string = 'razonSocial', direction: string = 'ASC'): Observable<ApiResponse<PageResponse<Proveedor>>> {
        return this.http.get<ApiResponse<PageResponse<Proveedor>>>(
            `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`
        );
    }

    obtenerPorId(id: number): Observable<ApiResponse<Proveedor>> {
        return this.http.get<ApiResponse<Proveedor>>(`${this.apiUrl}/${id}`);
    }

    buscar(q: string, type: string = '', page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Proveedor>>> {
        return this.http.get<ApiResponse<PageResponse<Proveedor>>>(
            `${this.apiUrl}/buscar?q=${q}&type=${type}&page=${page}&size=${size}`
        );
    }

    crear(proveedor: Proveedor): Observable<ApiResponse<Proveedor>> {
        return this.http.post<ApiResponse<Proveedor>>(this.apiUrl, proveedor);
    }

    actualizar(id: number, proveedor: Proveedor): Observable<ApiResponse<Proveedor>> {
        return this.http.put<ApiResponse<Proveedor>>(`${this.apiUrl}/${id}`, proveedor);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
