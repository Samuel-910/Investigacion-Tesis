import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';
import { CronogramaPago } from '../models/compra.model';

@Injectable({
    providedIn: 'root'
})
export class CronogramaPagoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/cronograma-pagos`;

    solicitarPago(id: number): Observable<ApiResponse<CronogramaPago>> {
        return this.http.post<ApiResponse<CronogramaPago>>(`${this.apiUrl}/${id}/solicitar`, {});
    }

    pagarCuota(id: number, numeroOperacion: string, voucher?: File): Observable<ApiResponse<CronogramaPago>> {
        const formData = new FormData();
        formData.append('numeroOperacion', numeroOperacion);
        if (voucher) {
            formData.append('voucher', voucher);
        }
        return this.http.post<ApiResponse<CronogramaPago>>(`${this.apiUrl}/${id}/pagar`, formData);
    }

    solicitarPagoCompra(idCompra: number, params: { usarSaldoFavor: boolean, montoUsarSaldo: number } = { usarSaldoFavor: false, montoUsarSaldo: 0 }): Observable<ApiResponse<CronogramaPago>> {
        return this.http.post<ApiResponse<CronogramaPago>>(`${this.apiUrl}/solicitar/compra/${idCompra}`, params);
    }
}
