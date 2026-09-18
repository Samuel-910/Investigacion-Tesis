import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../../shared/modals/response.model';
import { Sucursal } from '../../features/configuraciones/models/sucursal.model';

@Injectable({
    providedIn: 'root'
})
export class SucursalService {
    private apiUrl = `${environment.apiUrl}/sucursales`;

    constructor(private http: HttpClient) { }

    getAll(page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<Sucursal>>> {
        return this.http.get<ApiResponse<PageResponse<Sucursal>>>(`${this.apiUrl}?page=${page}&size=${size}`);
    }

    buscar(page: number = 0, size: number = 20, q: string = '', type: string = 'ALL'): Observable<ApiResponse<PageResponse<Sucursal>>> {
        return this.http.get<ApiResponse<PageResponse<Sucursal>>>(`${this.apiUrl}/buscar?page=${page}&size=${size}&q=${q}&type=${type}`);
    }

    getAllList(): Observable<ApiResponse<Sucursal[]>> {
        return this.http.get<ApiResponse<Sucursal[]>>(`${this.apiUrl}/list`);
    }

    getActivas(): Observable<ApiResponse<Sucursal[]>> {
        return this.http.get<ApiResponse<Sucursal[]>>(`${this.apiUrl}/activas/list`);
    }

    getById(id: number): Observable<ApiResponse<Sucursal>> {
        return this.http.get<ApiResponse<Sucursal>>(`${this.apiUrl}/${id}`);
    }

    crear(sucursal: Partial<Sucursal>): Observable<ApiResponse<Sucursal>> {
        return this.http.post<ApiResponse<Sucursal>>(this.apiUrl, sucursal);
    }

    actualizar(id: number, sucursal: Partial<Sucursal>): Observable<ApiResponse<Sucursal>> {
        return this.http.put<ApiResponse<Sucursal>>(`${this.apiUrl}/${id}`, sucursal);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
