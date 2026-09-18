import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../../shared/modals/response.model';


export interface CajaGeneralSaldo {
    metodoPago: string;
    saldo: number;
}

export interface CajaGeneral {
    id: number;
    nombre: string;
    idSucursal: number;
    saldoActual: number;
    saldosPorMetodo: CajaGeneralSaldo[];
}

export interface CajaGeneralMovimiento {
    id: number;
    tipo: 'INGRESO' | 'EGRESO' | 'TRANSFERENCIA_RECIBIDA' | 'PAGO_PROVEEDOR' | 'TRANSFERENCIA_ENTRE_METODOS';
    monto: number;
    descripcion: string;
    referencia: string;
    metodoPago: string;
    fechaRegistro: string;
    usuario: string;
}

@Injectable({
    providedIn: 'root'
})
export class CajaGeneralService {
    private apiUrl = `${environment.apiUrl}/caja-general`;

    constructor(private http: HttpClient) { }

    obtenerSaldo(idSucursal: number): Observable<ApiResponse<CajaGeneral>> {
        return this.http.get<ApiResponse<CajaGeneral>>(`${this.apiUrl}/sucursal/${idSucursal}`);
    }

    listarMovimientos(
        idSucursal: number, 
        metodoPago?: string, 
        query?: string, 
        searchType?: string, 
        page: number = 0, 
        size: number = 10
    ): Observable<ApiResponse<any>> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (metodoPago) params = params.set('metodoPago', metodoPago);
        if (query) params = params.set('query', query);
        if (searchType) params = params.set('searchType', searchType);

        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/sucursal/${idSucursal}/movimientos`, { params });
    }

    registrarMovimiento(movimiento: any): Observable<ApiResponse<CajaGeneralMovimiento>> {
        return this.http.post<ApiResponse<CajaGeneralMovimiento>>(`${this.apiUrl}/movimiento`, movimiento);
    }

    actualizarMovimiento(id: number, movimiento: any): Observable<ApiResponse<CajaGeneralMovimiento>> {
        return this.http.put<ApiResponse<CajaGeneralMovimiento>>(`${this.apiUrl}/movimiento/${id}`, movimiento);
    }

    eliminarMovimiento(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/movimiento/${id}`);
    }
}
