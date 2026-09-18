import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CatalogoRequest, CatalogoResponse } from '../models/catalogo.model';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class CatalogoService {
    private apiUrl = `${environment.apiUrl}/catalogo`;

    constructor(private http: HttpClient) { }

    listarTodos(page: number = 0, size: number = 10, type?: string, idCategoria?: number, esGenerico?: boolean | null, manejaLotes?: boolean | null, estado?: string | null, tipoAfectacion?: string | null): Observable<ApiResponse<PageResponse<CatalogoResponse>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (type && type !== 'ALL') {
            params = params.set('tipo', type);
        }
        if (idCategoria) {
            params = params.set('idCategoria', idCategoria.toString());
        }
        if (esGenerico !== null && esGenerico !== undefined) {
            params = params.set('esGenerico', esGenerico.toString());
        }
        if (manejaLotes !== null && manejaLotes !== undefined) {
            params = params.set('manejaLotes', manejaLotes.toString());
        }
        if (estado) {
            params = params.set('estado', estado);
        }
        if (tipoAfectacion) {
            params = params.set('tipoAfectacion', tipoAfectacion);
        }

        return this.http.get<ApiResponse<PageResponse<CatalogoResponse>>>(this.apiUrl, { params });
    }

    buscar(q: string, searchType: string = '', productType: string = '', page: number = 0, size: number = 10, idCategoria?: number, esGenerico?: boolean | null, manejaLotes?: boolean | null, estado?: string | null, tipoAfectacion?: string | null): Observable<ApiResponse<PageResponse<CatalogoResponse>>> {
        let params = new HttpParams()
            .set('q', q)
            .set('page', page.toString())
            .set('size', size.toString());

        if (searchType) params = params.set('searchType', searchType);
        if (productType && productType !== 'ALL') params = params.set('tipo', productType);

        if (idCategoria) {
            params = params.set('idCategoria', idCategoria.toString());
        }
        if (esGenerico !== null && esGenerico !== undefined) {
            params = params.set('esGenerico', esGenerico.toString());
        }
        if (manejaLotes !== null && manejaLotes !== undefined) {
            params = params.set('manejaLotes', manejaLotes.toString());
        }
        if (estado) {
            params = params.set('estado', estado);
        }
        if (tipoAfectacion) {
            params = params.set('tipoAfectacion', tipoAfectacion);
        }

        return this.http.get<ApiResponse<PageResponse<CatalogoResponse>>>(`${this.apiUrl}/buscar`, { params });
    }











    crear(request: CatalogoRequest): Observable<ApiResponse<CatalogoResponse>> {
        return this.http.post<ApiResponse<CatalogoResponse>>(this.apiUrl, request);
    }

    obtenerPorId(id: number): Observable<ApiResponse<CatalogoResponse>> {
        return this.http.get<ApiResponse<CatalogoResponse>>(`${this.apiUrl}/${id}`);
    }

    obtenerPorCodigo(codigo: string): Observable<ApiResponse<CatalogoResponse>> {
        return this.http.get<ApiResponse<CatalogoResponse>>(`${this.apiUrl}/codigo/${codigo}`);
    }

    obtenerPorCodigoBarra(codigoBarra: string): Observable<ApiResponse<CatalogoResponse>> {
        return this.http.get<ApiResponse<CatalogoResponse>>(`${this.apiUrl}/codigo-barra/${codigoBarra}`);
    }

    listarActivos(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<CatalogoResponse>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<CatalogoResponse>>>(`${this.apiUrl}/activos`, { params });
    }

    listarPorTipo(tipo: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<CatalogoResponse>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<CatalogoResponse>>>(`${this.apiUrl}/tipo/${tipo}`, { params });
    }

    actualizar(id: number, request: CatalogoRequest): Observable<ApiResponse<CatalogoResponse>> {
        return this.http.put<ApiResponse<CatalogoResponse>>(`${this.apiUrl}/${id}`, request);
    }

    cambiarEstado(id: number, estado: boolean): Observable<ApiResponse<CatalogoResponse>> {
        const params = new HttpParams().set('estado', estado.toString());
        return this.http.patch<ApiResponse<CatalogoResponse>>(`${this.apiUrl}/${id}/estado`, null, { params });
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }

    listarProductosControlados(): Observable<ApiResponse<CatalogoResponse[]>> {
        return this.http.get<ApiResponse<CatalogoResponse[]>>(`${this.apiUrl}/controlados`);
    }

    listarProductosConReceta(): Observable<ApiResponse<CatalogoResponse[]>> {
        return this.http.get<ApiResponse<CatalogoResponse[]>>(`${this.apiUrl}/con-receta`);
    }
}
