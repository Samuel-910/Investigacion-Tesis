import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TiempoAtencionReporte } from '../interfaces/reporte-tiempo-atencion.interface';
import { ApiResponse } from '../../features/auth/services/auth.service';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ReporteTiempoAtencionService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/reportes/tiempo-atencion`;

    /**
     * Genera el reporte de tiempo de atención
     * @param fechaDesde Fecha inicio en formato yyyy-MM-dd
     * @param fechaHasta Fecha fin en formato yyyy-MM-dd
     * @param areaId ID del área (opcional)
     * @returns Observable con el reporte completo
     */
    generarReporte(
        fechaDesde: string,
        fechaHasta: string,
        areaId?: number
    ): Observable<ApiResponse<TiempoAtencionReporte>> {
        let params = new HttpParams()
            .set('fechaDesde', fechaDesde)
            .set('fechaHasta', fechaHasta);

        // Solo agregar areaId si tiene un valor válido (no null, no undefined)
        if (areaId !== null && areaId !== undefined) {
            params = params.set('areaId', areaId.toString());
        }

        return this.http.get<ApiResponse<TiempoAtencionReporte>>(this.apiUrl, { params });
    }

    /**
     * Health check del módulo
     */
    healthCheck(): Observable<ApiResponse<string>> {
        return this.http.get<ApiResponse<string>>(`${this.apiUrl}/health`);
    }
}
