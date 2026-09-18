import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DescuentoDetalle } from '../models/descuento.model';

@Injectable({
    providedIn: 'root'
})
export class DescuentoDetalleService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/descuento-detalles`;

    guardar(idDescuento: number, detalle: DescuentoDetalle): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/descuento/${idDescuento}`, detalle);
    }

    listarPorDescuento(idDescuento: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/descuento/${idDescuento}`);
    }

    obtenerPorId(id: number): Observable<DescuentoDetalle> {
        return this.http.get<DescuentoDetalle>(`${this.apiUrl}/${id}`);
    }

    eliminar(id: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`);
    }
}
