import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto, ProductoRequest } from '../models/producto.model';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class ProductoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/productos`;

    crear(request: ProductoRequest): Observable<ApiResponse<Producto>> {
        return this.http.post<ApiResponse<Producto>>(this.apiUrl, request);
    }

    obtenerPorId(idProducto: number): Observable<ApiResponse<Producto>> {
        return this.http.get<ApiResponse<Producto>>(`${this.apiUrl}/${idProducto}`);
    }

    obtenerPorIdYSucursal(idProducto: number, idSucursal: number): Observable<ApiResponse<Producto>> {
        return this.http.get<ApiResponse<Producto>>(`${this.apiUrl}/${idProducto}/sucursal/${idSucursal}`);
    }

    listarPorSucursal(idSucursal: number, page: number = 0, size: number = 10, sortBy: string = 'idProducto', direction: string = 'ASC', soloParaVenta?: boolean): Observable<ApiResponse<PageResponse<Producto>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortBy', sortBy)
            .set('direction', direction);

        if (soloParaVenta) {
            params = params.set('soloParaVenta', 'true');
        }

        return this.http.get<ApiResponse<PageResponse<Producto>>>(`${this.apiUrl}/sucursal/${idSucursal}`, { params });
    }

    listarParaDescuentos(idSucursal: number, page: number = 0, size: number = 50, sortBy: string = 'idCatalogo', direction: string = 'ASC'): Observable<ApiResponse<PageResponse<Producto>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortBy', sortBy)
            .set('direction', direction);

        return this.http.get<ApiResponse<PageResponse<Producto>>>(`${this.apiUrl}/sucursal/${idSucursal}/para-descuentos`, { params });
    }

    listarPorCatalogo(idCatalogo: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/catalogo/${idCatalogo}`);
    }

    listarPorCatalogoYSucursal(idCatalogo: number, idSucursal: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/catalogo/${idCatalogo}/sucursal/${idSucursal}`);
    }

    actualizar(idProducto: number, request: ProductoRequest): Observable<ApiResponse<Producto>> {
        return this.http.put<ApiResponse<Producto>>(`${this.apiUrl}/${idProducto}`, request);
    }

    actualizarStock(idProducto: number, nuevoStock: number): Observable<ApiResponse<Producto>> {
        const params = new HttpParams().set('nuevoStock', nuevoStock.toString());
        return this.http.patch<ApiResponse<Producto>>(`${this.apiUrl}/${idProducto}/stock`, null, { params });
    }

    eliminar(idProducto: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${idProducto}`);
    }

    listarProductosBajoStock(idSucursal: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/bajo-stock`);
    }

    listarProductosSobreStock(idSucursal: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/sobre-stock`);
    }

    listarProductosProximosAVencer(idSucursal: number, dias: number = 90): Observable<ApiResponse<Producto[]>> {
        const params = new HttpParams().set('dias', dias.toString());
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/proximos-vencer`, { params });
    }

    listarProductosVencidos(idSucursal: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/vencidos`);
    }

    listarProductosSinStock(idSucursal: number): Observable<ApiResponse<Producto[]>> {
        return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/sin-stock`);
    }

    calcularValorInventario(idSucursal: number): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/sucursal/${idSucursal}/valor-inventario`);
    }

    buscar(searchTerm: string, page: number = 0, size: number = 10, idSucursal?: number, tipo?: string, soloParaVenta?: boolean): Observable<ApiResponse<PageResponse<Producto>>> {
        let params = new HttpParams()
            .set('q', searchTerm)
            .set('page', page.toString())
            .set('size', size.toString());

        if (idSucursal !== undefined && idSucursal !== null) {
            params = params.set('idSucursal', idSucursal.toString());
        }

        if (tipo) {
            params = params.set('tipo', tipo);
        }

        if (soloParaVenta) {
            params = params.set('soloParaVenta', 'true');
        }

        return this.http.get<ApiResponse<PageResponse<Producto>>>(`${this.apiUrl}/buscar`, { params });
    }

    listarPorTipo(tipo: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Producto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<Producto>>>(`${this.apiUrl}/tipo/${tipo}`, { params });
    }

    obtenerStockValorizado(idSucursal: number, page?: number, size?: number): Observable<ApiResponse<any>> {
        let params = new HttpParams();
        if (page !== undefined && page !== null) {
            params = params.set('page', page.toString());
        }
        if (size !== undefined && size !== null) {
            params = params.set('size', size.toString());
        }
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/sucursal/${idSucursal}/stock-valorizado`, { params });
    }

    searchAdvanced(params: any): Observable<ApiResponse<PageResponse<Producto>>> {
        let httpParams = new HttpParams();
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
                httpParams = httpParams.append(key, params[key]);
            }
        });

        return this.http.get<ApiResponse<PageResponse<Producto>>>(`${this.apiUrl}/search-advanced`, { params: httpParams });
    }

    getVendedores(idSucursal: number): Observable<ApiResponse<string[]>> {
        return this.http.get<ApiResponse<string[]>>(`${this.apiUrl}/sucursal/${idSucursal}/vendedores`);
    }
}