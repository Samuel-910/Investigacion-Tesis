import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface DocumentoFormato {
    id: number;
    nombre: string;
    anchoPx: number;
    altoPx?: number;
    descripcion?: string;
}

@Injectable({
    providedIn: 'root'
})
export class FormatoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/documentos/formatos`;

    listar(): Observable<ApiResponse<DocumentoFormato[]>> {
        return this.http.get<ApiResponse<DocumentoFormato[]>>(this.apiUrl);
    }

    obtenerPorId(id: number): Observable<ApiResponse<DocumentoFormato>> {
        return this.http.get<ApiResponse<DocumentoFormato>>(`${this.apiUrl}/${id}`);
    }

    guardar(formato: DocumentoFormato): Observable<ApiResponse<DocumentoFormato>> {
        return this.http.post<ApiResponse<DocumentoFormato>>(this.apiUrl, formato);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
