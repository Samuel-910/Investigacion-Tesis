import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';
import { TransferenciaRequest, TransferenciaSucursal } from '../models/transferencia.model';

@Injectable({
    providedIn: 'root'
})
export class TransferenciaService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/inventario/transferencia`;

    listar(idSucursal: number, q?: string, type?: string, page?: number, size?: number): Observable<ApiResponse<TransferenciaSucursal[]>> {
        let params = new HttpParams().set('idSucursal', idSucursal.toString());
        if (q) params = params.set('q', q);
        if (type) params = params.set('type', type);
        if (page !== undefined) params = params.set('page', page.toString());
        if (size !== undefined) params = params.set('size', size.toString());
        return this.http.get<ApiResponse<TransferenciaSucursal[]>>(this.apiUrl, { params });
    }

    obtenerPorId(id: number): Observable<ApiResponse<TransferenciaSucursal>> {
        return this.http.get<ApiResponse<TransferenciaSucursal>>(`${this.apiUrl}/${id}`);
    }

    solicitar(request: TransferenciaRequest): Observable<ApiResponse<void>> {
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/solicitar`, request);
    }

    enviar(id: number, idUsuario: number, detalles: any[]): Observable<ApiResponse<void>> {
        const params = new HttpParams().set('idUsuario', idUsuario.toString());
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/enviar`, detalles, { params });
    }

    recibir(id: number, idUsuario: number, idAlmacenDestino: number): Observable<ApiResponse<void>> {
        const params = new HttpParams()
            .set('idUsuario', idUsuario.toString())
            .set('idAlmacenDestino', idAlmacenDestino.toString());
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/recibir`, null, { params });
    }

    rechazar(id: number, idUsuario: number, motivo: string): Observable<ApiResponse<void>> {
        const params = new HttpParams()
            .set('idUsuario', idUsuario.toString())
            .set('motivo', motivo);
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/rechazar`, null, { params });
    }
}
