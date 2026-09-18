import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class CuentaProveedorService {
    private apiUrl = `${environment.apiUrl}/cuentas-proveedor`;

    constructor(private http: HttpClient) { }

    obtenerCuenta(idProveedor: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/proveedor/${idProveedor}`);
    }

    listarMovimientos(idProveedor: number, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<any>>> {
        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/proveedor/${idProveedor}/movimientos`, {
            params: { page: page.toString(), size: size.toString() }
        });
    }

    registrarMovimiento(idProveedor: number, movimiento: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/proveedor/${idProveedor}/movimiento`, movimiento);
    }

    listarCuentasPendientes(): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/pendientes`);
    }

    listarDeudasPendientes(idProveedor: number): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/proveedor/${idProveedor}/deudas`);
    }

    listarDeudasPendientesPaginadas(page: number = 0, size: number = 10, searchTerm?: string): Observable<ApiResponse<PageResponse<any>>> {
        let params: any = { page: page.toString(), size: size.toString() };
        if (searchTerm) {
            params.searchTerm = searchTerm;
        }
        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/deudas/pendientes/paginadas`, { params });
    }
}
