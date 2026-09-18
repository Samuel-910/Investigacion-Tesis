import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';

export interface SolicitudAnulacionDTO {
    id: number;
    tipo: 'VENTA' | 'COMPRA' | 'MOVIMIENTO_DIVERSO';
    referenciaId: number;
    motivo: string;
    idUsuarioSolicita: number;
    usuarioSolicita?: string;
    fechaSolicitud: string;
    estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
    usuarioAtiende?: string;
    fechaAtiende?: string;
    observacionAtiende?: string;
    documentoReferencia?: string;
    clienteProveedor?: string;
    monto?: number;
    tipoDocumento?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AprobacionService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/aprobaciones/anulaciones`;

    obtenerPendientes(q?: string, type?: string, page?: number, size?: number, idSucursal?: number | string, idPuntoVenta?: number | string): Observable<ApiResponse<SolicitudAnulacionDTO[]>> {
        let params = new HttpParams();
        if (q) params = params.set('q', q);
        if (type) params = params.set('type', type);
        if (page !== undefined && page !== null) params = params.set('page', page.toString());
        if (size !== undefined && size !== null) params = params.set('size', size.toString());
        if (idSucursal !== undefined && idSucursal !== null) {
            params = params.set('idSucursal', idSucursal === 'TODOS' ? '0' : idSucursal.toString());
        }
        if (idPuntoVenta !== undefined && idPuntoVenta !== null) {
            params = params.set('idPuntoVenta', idPuntoVenta === 'TODOS' ? '0' : idPuntoVenta.toString());
        }
        return this.http.get<ApiResponse<SolicitudAnulacionDTO[]>>(`${this.apiUrl}/pendientes`, { params });
    }

    atender(id: number, aprobada: boolean, observacion: string): Observable<ApiResponse<void>> {
        const params = new HttpParams()
            .set('aprobada', aprobada.toString())
            .set('observacion', observacion);
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/atender`, {}, { params });
    }

    obtenerHistorial(q?: string, type?: string, page?: number, size?: number, idSucursal?: number | string, idPuntoVenta?: number | string): Observable<ApiResponse<SolicitudAnulacionDTO[]>> {
        let params = new HttpParams();
        if (q) params = params.set('q', q);
        if (type) params = params.set('type', type);
        if (page !== undefined && page !== null) params = params.set('page', page.toString());
        if (size !== undefined && size !== null) params = params.set('size', size.toString());
        if (idSucursal !== undefined && idSucursal !== null) {
            params = params.set('idSucursal', idSucursal === 'TODOS' ? '0' : idSucursal.toString());
        }
        if (idPuntoVenta !== undefined && idPuntoVenta !== null) {
            params = params.set('idPuntoVenta', idPuntoVenta === 'TODOS' ? '0' : idPuntoVenta.toString());
        }
        return this.http.get<ApiResponse<SolicitudAnulacionDTO[]>>(`${this.apiUrl}/historial`, { params });
    }
}
