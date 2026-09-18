import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';


export interface PuntoDocumento {
    id: number;
    puntoId: number;
    puntoNombre?: string;
    tipoDoc: string;
    serie: string;
    numero: number;
    ip?: string;
    idDocimp?: number;
    x?: string;
    idPersonalUser?: number;
    selecc?: string;
    nota?: string;
    serieTicketera?: string;
    estado?: string;
    lpt?: string;
    detNc?: string;
    refact?: string;
    refactDia?: number;
    modulo?: string; // VENTA, COMPRA
    idPlantilla?: number;
    plantillaNombre?: string;
    tipoDocumentoNombre?: string;
    plantillaFormato?: string;
    plantillaOrientacion?: string;
}

export interface PuntoDocumentoAuditoria {
    id: number;
    idPuntoDoc: number;
    usuario: string;
    fechaCambio: string;
    operacion: string;
    datosAnteriores: string;
}

@Injectable({
    providedIn: 'root'
})
export class PuntoDocumentoService {
    private apiUrl = `${environment.apiUrl}/documentos`;

    constructor(private http: HttpClient) { }

    obtenerTodos(page: number = 0, size: number = 10, puntoId?: number): Observable<ApiResponse<PageResponse<PuntoDocumento>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        if (puntoId) {
            params = params.set('puntoId', puntoId.toString());
        }
        return this.http.get<ApiResponse<PageResponse<PuntoDocumento>>>(this.apiUrl, { params });
    }

    obtenerPorId(id: number): Observable<ApiResponse<PuntoDocumento>> {
        return this.http.get<ApiResponse<PuntoDocumento>>(`${this.apiUrl}/${id}`);
    }

    guardar(documento: Partial<PuntoDocumento>): Observable<ApiResponse<PuntoDocumento>> {
        if (documento.id) {
            return this.http.put<ApiResponse<PuntoDocumento>>(`${this.apiUrl}/${documento.id}`, documento);
        }
        return this.http.post<ApiResponse<PuntoDocumento>>(this.apiUrl, documento);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    obtenerPorPunto(puntoId: number, modulos?: string[]): Observable<ApiResponse<PuntoDocumento[]>> {
        let params = new HttpParams();
        if (modulos && modulos.length > 0) {
            modulos.forEach(modulo => {
                params = params.append('modulos', modulo);
            });
        }
        return this.http.get<ApiResponse<PuntoDocumento[]>>(`${this.apiUrl}/activos/punto/${puntoId}`, { params }).pipe(
            map(res => {
                if (res.data) {
                    res.data = res.data.map((item: any) => ({
                        ...item,
                        estado: typeof item.estado === 'object' ? item.estado?.name : item.estado
                    }));
                }
                return res;
            })
        );
    }

    obtenerHistorial(id: number): Observable<ApiResponse<PuntoDocumentoAuditoria[]>> {
        return this.http.get<ApiResponse<PuntoDocumentoAuditoria[]>>(`${this.apiUrl}/${id}/historial`);
    }

    obtenerTiposDocumentos(): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/tipos-documento`);
    }

    // Tipos de documentos configurados por Módulo y Sucursal
    obtenerTiposAsignados(modulo?: string, idSucursal?: number | string): Observable<ApiResponse<any[]>> {
        let params = new HttpParams();
        if (modulo) params = params.set('modulo', modulo);
        if (idSucursal && idSucursal !== 'TODOS') params = params.set('sucursalId', idSucursal.toString());

        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/tipos-asignados`, { params });
    }

    obtenerDocumentosActivosPorSucursal(sucursalId: number | string, modulos?: string[]): Observable<ApiResponse<any[]>> {
        let params = new HttpParams();
        if (modulos && modulos.length > 0) {
            modulos.forEach(modulo => {
                params = params.append('modulos', modulo);
            });
        }
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/activos/sucursal/${sucursalId}`, { params });
    }

    obtenerPlantillasPorModulo(modulo: string, tipo?: string): Observable<ApiResponse<any[]>> {
        let params = new HttpParams().set('modulo', modulo);
        if (tipo) {
            params = params.set('tipo', tipo);
        }
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/plantillas`, { params });
    }
}
