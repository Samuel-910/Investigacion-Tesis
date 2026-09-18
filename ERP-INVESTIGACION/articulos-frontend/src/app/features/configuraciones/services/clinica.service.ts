import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Clinica, ClinicaAuditoria } from '../models/empresa.model';
import { ApiResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class ClinicaService {
    private apiUrl = `${environment.apiUrl}/clinica`;

    constructor(private http: HttpClient) { }

    obtenerPrincipal(): Observable<ApiResponse<Clinica>> {
        return this.http.get<ApiResponse<Clinica>>(this.apiUrl);
    }

    obtenerPorId(id: number): Observable<ApiResponse<Clinica>> {
        return this.http.get<ApiResponse<Clinica>>(`${this.apiUrl}/${id}`);
    }

    obtenerHistorial(idClinica: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/historial/${idClinica}?page=0&size=1000`);
    }

    crear(clinica: Clinica): Observable<ApiResponse<Clinica>> {
        return this.http.post<ApiResponse<Clinica>>(this.apiUrl, clinica);
    }

    actualizar(id: number, clinica: Clinica): Observable<ApiResponse<Clinica>> {
        return this.http.put<ApiResponse<Clinica>>(`${this.apiUrl}/${id}`, clinica);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}
