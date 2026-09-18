import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Bloque, Plantilla, DocumentoFormato } from '../models/documento.model';
import { ApiResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class DocumentoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/documentos`;
    private formatosUrl = `${environment.apiUrl}/documentos/formatos`;

    // --- BLOQUES ---
    listarFormatos(): Observable<ApiResponse<DocumentoFormato[]>> {
        return this.http.get<ApiResponse<DocumentoFormato[]>>(this.formatosUrl);
    }

    listarBloques(modulo?: string): Observable<ApiResponse<Bloque[]>> {
        let params: any = {};
        if (modulo) params.modulo = modulo;
        return this.http.get<ApiResponse<Bloque[]>>(`${this.apiUrl}/bloques`, { params });
    }

    obtenerBloque(id: number): Observable<ApiResponse<Bloque>> {
        return this.http.get<ApiResponse<Bloque>>(`${this.apiUrl}/bloques/${id}`);
    }

    guardarBloque(bloque: Bloque): Observable<ApiResponse<Bloque>> {
        return this.http.post<ApiResponse<Bloque>>(`${this.apiUrl}/bloques`, bloque);
    }

    actualizarBloque(id: number, bloque: Bloque): Observable<ApiResponse<Bloque>> {
        return this.http.put<ApiResponse<Bloque>>(`${this.apiUrl}/bloques/${id}`, bloque);
    }

    eliminarBloque(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/bloques/${id}`);
    }

    // --- PLANTILLAS ---

    listarPlantillas(modulo?: string, tipo?: string): Observable<ApiResponse<Plantilla[]>> {
        let params: any = {};
        if (modulo) params.modulo = modulo;
        if (tipo) params.tipo = tipo;
        return this.http.get<ApiResponse<Plantilla[]>>(`${this.apiUrl}/plantillas`, { params });
    }

    obtenerPlantilla(id: number): Observable<ApiResponse<Plantilla>> {
        return this.http.get<ApiResponse<Plantilla>>(`${this.apiUrl}/plantillas/${id}`);
    }

    guardarPlantilla(plantilla: Plantilla): Observable<ApiResponse<Plantilla>> {
        return this.http.post<ApiResponse<Plantilla>>(`${this.apiUrl}/plantillas`, plantilla);
    }

    actualizarPlantilla(id: number, plantilla: Plantilla): Observable<ApiResponse<Plantilla>> {
        return this.http.put<ApiResponse<Plantilla>>(`${this.apiUrl}/plantillas/${id}`, plantilla);
    }

    eliminarPlantilla(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/plantillas/${id}`);
    }

    // --- GENERACIÓN ---

    generarPdf(id: number, datos: any): Observable<Blob> {
        return this.http.post(`${this.apiUrl}/plantillas/${id}/generar-pdf`, datos, {
            responseType: 'blob'
        });
    }
}
