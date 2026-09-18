import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { PlantillaAsignacion, PuntoDocumento, TipoDocumento } from '../models/asignacion-plantilla.model';

@Injectable({
    providedIn: 'root'
})
export class AsignacionPlantillaService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/documentos/plantilla-asignacion`;

    listarPorModulo(modulo: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<PlantillaAsignacion>>> {
        return this.http.get<ApiResponse<PageResponse<PlantillaAsignacion>>>(`${this.apiUrl}/modulo/${modulo}?page=${page}&size=${size}`);
    }

    listarPorModuloTodo(modulo: string): Observable<ApiResponse<PlantillaAsignacion[]>> {
        return this.http.get<ApiResponse<PlantillaAsignacion[]>>(`${this.apiUrl}/modulo/${modulo}/all`);
    }

    guardar(asignacion: PlantillaAsignacion): Observable<ApiResponse<PlantillaAsignacion>> {
        return this.http.post<ApiResponse<PlantillaAsignacion>>(this.apiUrl, asignacion);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }

    // Helpers para combos
    listarPuntosDocumento(): Observable<ApiResponse<PuntoDocumento[]>> {
        return this.http.get<ApiResponse<PuntoDocumento[]>>(`${environment.apiUrl}/documentos/list`);
    }

    listarTiposDocumento(): Observable<any> {
        return this.http.get<any>(`${environment.apiUrl}/tipo-documento`);
    }
}
