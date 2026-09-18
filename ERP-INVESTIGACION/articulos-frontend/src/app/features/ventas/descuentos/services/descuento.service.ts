import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Descuento, DescuentoRequest } from '../models/descuento.model';

@Injectable({
    providedIn: 'root'
})
export class DescuentoService {
    private apiUrl = `${environment.apiUrl}/descuentos`;

    constructor(private http: HttpClient) { }

    listar(page: number, size: number, nombre?: string, activo?: boolean): Observable<any> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (nombre) params = params.set('nombre', nombre);
        if (activo !== undefined && activo !== null) params = params.set('activo', activo.toString());

        return this.http.get<any>(this.apiUrl, { params });
    }

    obtener(id: number): Observable<Descuento> {
        return this.http.get<Descuento>(`${this.apiUrl}/${id}`);
    }

    crear(descuento: DescuentoRequest): Observable<any> {
        return this.http.post<any>(this.apiUrl, descuento);
    }

    actualizar(id: number, descuento: DescuentoRequest): Observable<any> {
        return this.http.post<any>(this.apiUrl, { ...descuento, id });
    }

    eliminar(id: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`);
    }

    listarVigentes(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/vigentes?size=1000`);
    }

    listarAplicables(idPaciente: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/aplicables/${idPaciente}`);
    }

    // Mantener para compatibilidad si es necesario, pero redirigir a vigentes
    listarActivos(): Observable<any> {
        return this.listarVigentes();
    }
}
