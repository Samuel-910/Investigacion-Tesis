import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../../features/auth/services/auth.service';

export interface CajaChicaResponse {
    id: number;
    nombre: string;
    saldoActual: number;
    estado: string;
    saldoInicial: number;
    idPuntoVenta: number;
    idUsuarioCajero: string;
    fechaCierre?: string;
    createdAt: string;
}

export interface CajaChicaRequest {
    nombre: string;
    idSucursal: number;
    idPuntoVenta: number;
    saldoInicial: number;
}

@Injectable({
    providedIn: 'root'
})
export class CajaChicaService {
    private apiUrl = `${environment.apiUrl}/caja-chica`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });
    }

    obtenerCajaAbierta(): Observable<CajaChicaResponse | null> {
        return this.http.get<ApiResponse<CajaChicaResponse>>(
            `${this.apiUrl}/abierta`,
            { headers: this.getHeaders() }
        ).pipe(
            map(response => response.data),
            catchError(error => {
                console.error('Error obteniendo caja abierta:', error);
                return throwError(() => error);
            })
        );
    }

    abrirCaja(request: CajaChicaRequest): Observable<CajaChicaResponse> {
        return this.http.post<ApiResponse<CajaChicaResponse>>(
            `${this.apiUrl}/cajas`,
            request,
            { headers: this.getHeaders() }
        ).pipe(
            map(response => response.data),
            catchError(error => {
                console.error('Error abriendo caja:', error);
                return throwError(() => error);
            })
        );
    }
}
