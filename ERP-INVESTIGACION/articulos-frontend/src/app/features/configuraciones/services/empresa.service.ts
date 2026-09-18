import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Empresa, EmpresaPersonaVinculo, VinculoDTO } from '../models/empresa.model';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class EmpresaService {
    private apiUrl = `${environment.apiUrl}/empresas`;

    constructor(private http: HttpClient) { }

    listarTodas(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Empresa>>> {
        return this.http.get<ApiResponse<PageResponse<Empresa>>>(`${this.apiUrl}?page=${page}&size=${size}`);
    }

    buscar(q: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Empresa>>> {
        return this.http.get<ApiResponse<PageResponse<Empresa>>>(`${this.apiUrl}/search?q=${q}&page=${page}&size=${size}`);
    }

    listarSinPaginacion(): Observable<ApiResponse<Empresa[]>> {
        return this.http.get<ApiResponse<Empresa[]>>(`${this.apiUrl}?all=true`);
    }

    obtenerPorId(id: number): Observable<ApiResponse<Empresa>> {
        return this.http.get<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`);
    }

    crear(empresa: Empresa): Observable<ApiResponse<Empresa>> {
        return this.http.post<ApiResponse<Empresa>>(this.apiUrl, empresa);
    }

    actualizar(id: number, empresa: Empresa): Observable<ApiResponse<Empresa>> {
        return this.http.put<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`, empresa);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }

    crearVinculo(vinculo: VinculoDTO): Observable<ApiResponse<EmpresaPersonaVinculo>> {
        return this.http.post<ApiResponse<EmpresaPersonaVinculo>>(`${this.apiUrl}/vinculo`, vinculo);
    }

    obtenerVinculos(idEmpresa: number): Observable<ApiResponse<EmpresaPersonaVinculo[]>> {
        return this.http.get<ApiResponse<EmpresaPersonaVinculo[]>>(`${this.apiUrl}/${idEmpresa}/vinculos`);
    }
}
