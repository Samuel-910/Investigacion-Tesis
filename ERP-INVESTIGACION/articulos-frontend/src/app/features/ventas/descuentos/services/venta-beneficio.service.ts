import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { VentaBeneficio } from '../models/venta-descuento.model';

@Injectable({
    providedIn: 'root'
})
export class VentaBeneficioService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/venta-beneficios`;

    guardar(beneficio: VentaBeneficio): Observable<any> {
        return this.http.post<any>(this.apiUrl, beneficio);
    }

    listarPorVenta(idVenta: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/venta/${idVenta}`);
    }

    obtenerPorId(id: number): Observable<VentaBeneficio> {
        return this.http.get<VentaBeneficio>(`${this.apiUrl}/${id}`);
    }

    eliminar(id: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`);
    }
}
