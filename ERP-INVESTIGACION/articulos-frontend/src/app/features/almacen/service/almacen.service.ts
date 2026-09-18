import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Almacen, AlmacenRequest } from '../models/almacen.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AlmacenService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/almacenes`;

    getBySucursal(idSucursal: number): Observable<Almacen[]> {
        return this.http.get<Almacen[]>(`${this.apiUrl}/sucursal/${idSucursal}`);
    }

    getById(id: number): Observable<Almacen> {
        return this.http.get<Almacen>(`${this.apiUrl}/${id}`);
    }

    create(almacen: AlmacenRequest): Observable<Almacen> {
        return this.http.post<Almacen>(this.apiUrl, almacen);
    }

    update(id: number, almacen: AlmacenRequest): Observable<Almacen> {
        return this.http.put<Almacen>(`${this.apiUrl}/${id}`, almacen);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getAll(): Observable<any> {
        return this.http.get<any>(this.apiUrl);
    }
}
