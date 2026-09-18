import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class IntercambioService {
    private apiUrl = `${environment.apiUrl}/compras/intercambio`;

    constructor(private http: HttpClient) { }

    obtenerProveedor(idProducto: number, lote: string): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/proveedor`, {
            params: { idProducto: idProducto.toString(), lote }
        });
    }

    procesarIntercambio(request: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(this.apiUrl, request);
    }
}
