import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CajaChica, Movimiento, CajaResumen } from '../models/caja-chica.model';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class CajaChicaService {
    private apiUrl = `${environment.apiUrl}/caja-chica`;

    constructor(private http: HttpClient) { }

    listarCajas(): Observable<ApiResponse<CajaChica[]>> {
        return this.http.get<ApiResponse<CajaChica[]>>(`${this.apiUrl}/cajas`);
    }

    crearCaja(request: Partial<CajaChica>): Observable<ApiResponse<CajaChica>> {
        return this.http.post<ApiResponse<CajaChica>>(`${this.apiUrl}/cajas`, request);
    }

    registrarMovimiento(request: Movimiento): Observable<ApiResponse<Movimiento>> {
        return this.http.post<ApiResponse<Movimiento>>(`${this.apiUrl}/movimientos`, request);
    }

    listarMovimientos(cajaId: number, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Movimiento>>> {
        return this.http.get<ApiResponse<PageResponse<Movimiento>>>(
            `${this.apiUrl}/movimientos/${cajaId}?page=${page}&size=${size}`
        );
    }

    obtenerResumen(id: number): Observable<ApiResponse<CajaResumen>> {
        return this.http.get<ApiResponse<CajaResumen>>(`${this.apiUrl}/resumen/${id}`);
    }

    cerrarCaja(id: number, saldoCierreReal: number, transferirACajaGeneral: boolean = false): Observable<ApiResponse<CajaChica>> {
        return this.http.put<ApiResponse<CajaChica>>(`${this.apiUrl}/cajas/${id}/cerrar?saldoCierreReal=${saldoCierreReal}&transferirACajaGeneral=${transferirACajaGeneral}`, {});
    }

    cerrarCajaDetallado(id: number, request: { saldoCierreReal: number, transferirACajaGeneral: boolean, diferencias: any[] }): Observable<ApiResponse<CajaChica>> {
        return this.http.put<ApiResponse<CajaChica>>(`${this.apiUrl}/cajas/${id}/cerrar-detallado`, request);
    }

    eliminarMovimiento(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/movimientos/${id}`);
    }
}
