import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { environment } from '../../../environments/environment';
import { AtributoRequest, BaseAtributo } from '../../configuraciones/models/atributo.model';

export abstract class BaseAtributoService<T extends BaseAtributo> {
    protected abstract getResourceUrl(): string;

    constructor(protected http: HttpClient) { }

    get apiUrl(): string {
        return `${environment.apiUrl}/${this.getResourceUrl()}`;
    }

    listar(page: number = 0, size: number = 10, q: string = ''): Observable<ApiResponse<PageResponse<T>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (q) {
            params = params.set('q', q);
        }

        return this.http.get<ApiResponse<PageResponse<T>>>(this.apiUrl, { params });
    }

    crear(entidad: AtributoRequest): Observable<ApiResponse<T>> {
        return this.http.post<ApiResponse<T>>(this.apiUrl, entidad);
    }

    actualizar(id: number, entidad: AtributoRequest): Observable<ApiResponse<T>> {
        return this.http.put<ApiResponse<T>>(`${this.apiUrl}/${id}`, entidad);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    // For dropdowns mostly
    listarActivos(): Observable<ApiResponse<PageResponse<T>>> {
        return this.http.get<ApiResponse<PageResponse<T>>>(`${this.apiUrl}/activos?size=100`);
    }
}

@Injectable({ providedIn: 'root' })
export class CategoriaService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'categorias'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class LaboratorioService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'laboratorios'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class PrincipioActivoService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'principios-activos'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class AccionTerapeuticaService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'acciones-terapeuticas'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class UbicacionService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'ubicaciones'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class TipoService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'puntos/tipos'; }
    constructor(http: HttpClient) { super(http); }
}

@Injectable({ providedIn: 'root' })
export class ProcesoService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'puntos/procesos'; }
    constructor(http: HttpClient) { super(http); }
}
@Injectable({ providedIn: 'root' })
export class MetodoPagoService extends BaseAtributoService<BaseAtributo> {
    protected getResourceUrl() { return 'metodos-pago'; }
    constructor(http: HttpClient) { super(http); }
}
