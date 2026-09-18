import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';
import { PageResponse } from '../../../shared/modals/response.model';
import { RegArticuloKardex } from '../models/kardex.model';

@Injectable({
    providedIn: 'root'
})
export class KardexService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/kardex`;

    getMovimientos(
        idCatalogo: number,
        idSucursal: number,
        page: number = 0,
        size: number = 10,
        desde?: string,
        hasta?: string
    ): Observable<ApiResponse<PageResponse<RegArticuloKardex>>> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (desde) params = params.set('desde', desde);
        if (hasta) params = params.set('hasta', hasta);

        return this.http.get<ApiResponse<PageResponse<RegArticuloKardex>>>(`${this.apiUrl}/catalogo/${idCatalogo}`, { params });
    }

    exportarExcel(
        idCatalogo: number,
        idSucursal: number,
        desde?: string,
        hasta?: string
    ): Observable<Blob> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString());

        if (desde) params = params.set('desde', desde);
        if (hasta) params = params.set('hasta', hasta);

        return this.http.get(`${this.apiUrl}/export/catalogo/${idCatalogo}`, { params, responseType: 'blob' });
    }
}
