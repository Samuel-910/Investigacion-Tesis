import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class VentaRegistroService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/ventas`;

    registrar(venta: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/registro`, venta);
    }

    listar(page: number = 0, size: number = 10, estado: string = 'V'): Observable<ApiResponse<PageResponse<any>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('estado', estado);
        return this.http.get<ApiResponse<PageResponse<any>>>(this.apiUrl, { params });
    }

    buscar(q: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<any>>> {
        const params = new HttpParams()
            .set('q', q)
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/search`, { params });
    }

    search(
        page: number = 0,
        size: number = 10,
        serie: string = '',
        numero: string = '',
        numeroDesde: string = '',
        numeroHasta: string = '',
        fechaDesde: string = '',
        fechaHasta: string = '',
        idVendedor: string = '',
        condicionPago: string = '',
        estado: string = '',
        tipoDoc: string = '',
        porSucursal: boolean = false,
        idSucursal?: number | string,
        idPuntoVenta?: number | string
    ): Observable<ApiResponse<PageResponse<any>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (serie) params = params.set('serie', serie);
        if (numero) params = params.set('numero', numero);
        if (numeroDesde) params = params.set('numeroDesde', numeroDesde);
        if (numeroHasta) params = params.set('numeroHasta', numeroHasta);
        if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
        if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
        if (idVendedor) params = params.set('idVendedor', idVendedor);
        if (condicionPago) params = params.set('condicionPago', condicionPago);
        if (estado) params = params.set('estado', estado);
        if (tipoDoc) params = params.set('tipoDoc', tipoDoc);
        if (porSucursal) params = params.set('porSucursal', 'true');
        if (idSucursal !== undefined && idSucursal !== null) {
            params = params.set('idSucursal', idSucursal === 'TODOS' ? '0' : idSucursal.toString());
        }
        if (idPuntoVenta !== undefined && idPuntoVenta !== null) {
            params = params.set('idPuntoVenta', idPuntoVenta === 'TODOS' ? '0' : idPuntoVenta.toString());
        }

        return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}/search-advanced`, { params });
    }

    obtenerPorId(id: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/${id}`);
    }

    anular(id: number, motivo: string = ''): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('motivo', motivo);
        return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${id}/anular`, {}, { params });
    }

    anularCotizacion(id: number): Observable<ApiResponse<any>> {
        return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${id}/anular-cotizacion`, {});
    }

    obtenerPaciente(id: number): Observable<any> {
        return this.http.get<ApiResponse<any>>(`${environment.apiUrl}/users/${id}`).pipe(
            catchError(() => of(null))
        );
    }

    obtenerCorrelatividad(fechaInicio: string, fechaFin: string, idSucursal?: number, puntoId?: number, q?: string, type?: string, page?: number, size?: number): Observable<ApiResponse<any[]>> {
        let params = new HttpParams()
            .set('fechaInicio', fechaInicio)
            .set('fechaFin', fechaFin);

        if (idSucursal) {
            params = params.set('idSucursal', idSucursal.toString());
        }

        if (puntoId) {
            params = params.set('puntoId', puntoId.toString());
        }
        if (q) params = params.set('q', q);
        if (type) params = params.set('type', type);
        if (page !== undefined) params = params.set('page', page.toString());
        if (size !== undefined) params = params.set('size', size.toString());

        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/correlatividad`, { params });
    }

    obtenerDetalleCorrelatividad(fechaInicio: string, fechaFin: string, idSucursal?: number, tipoDoc?: string, serie?: string, puntoId?: number, q?: string, type?: string, page?: number, size?: number): Observable<ApiResponse<any[]>> {
        let params = new HttpParams()
            .set('fechaInicio', fechaInicio)
            .set('fechaFin', fechaFin)
            .set('tipoDoc', tipoDoc || '')
            .set('serie', serie || '');

        if (idSucursal) {
            params = params.set('idSucursal', idSucursal.toString());
        }

        if (puntoId) {
            params = params.set('puntoId', puntoId.toString());
        }
        if (q) params = params.set('q', q);
        if (type) params = params.set('type', type);
        if (page !== undefined) params = params.set('page', page.toString());
        if (size !== undefined) params = params.set('size', size.toString());

        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/correlatividad/detalle`, { params });
    }

    exportarExcelCorrelatividad(fechaInicio: string, fechaFin: string, idSucursal?: number, tipoDoc?: string, puntoId?: number): Observable<any> {
        let params = new HttpParams()
            .set('fechaInicio', fechaInicio)
            .set('fechaFin', fechaFin);

        if (idSucursal) {
            params = params.set('idSucursal', idSucursal.toString());
        }

        if (tipoDoc) {
            params = params.set('tipoDoc', tipoDoc);
        }

        if (puntoId) {
            params = params.set('puntoId', puntoId.toString());
        }

        return this.http.get(`${this.apiUrl}/export/correlatividad`, {
            params,
            responseType: 'blob',
            observe: 'response'
        });
    }

    realizarDevolucion(id: number, devolucion: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${id}/devolver`, devolucion);
    }

    descargarXml(id: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/${id}/xml`, {
            responseType: 'blob',
            observe: 'response'
        });
    }

    descargarHtml(id: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/${id}/html`, {
            responseType: 'blob',
            observe: 'response'
        });
    }

    enviarPorEmail(id: number, email: string, idPlantilla: number): Observable<ApiResponse<void>> {
        return this.descargarPdf(id, idPlantilla).pipe(
            switchMap((res: any) => {
                const pdfBlob = res.body;
                const formData = new FormData();
                formData.append('to', email);
                formData.append('subject', `Comprobante de Venta #${id}`);
                formData.append('body', `Hola,\n\nAdjuntamos el comprobante de venta solicitado.\n\nSaludos.`);
                formData.append('file', pdfBlob, `comprobante-${id}.pdf`);
                
                return this.http.post<ApiResponse<void>>('http://localhost:8087/api/email/send-with-attachment', formData);
            })
        );
    }

    enviarPorWhatsApp(id: number, telefono: string, idPlantilla: number): Observable<ApiResponse<void>> {
        const params = new HttpParams()
            .set('telefono', telefono)
            .set('idPlantilla', idPlantilla.toString());
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/enviar-whatsapp`, {}, { params });
    }

    descargarPdf(id: number, idPlantilla: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/${id}/pdf`, {
            params: new HttpParams().set('idPlantilla', idPlantilla.toString()),
            responseType: 'blob',
            observe: 'response'
        });
    }

    registrarReimpresion(id: number, motivo: string = 'Reimpresión solicitada por usuario'): Observable<ApiResponse<any>> {
        const params = new HttpParams().set('motivo', motivo);
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${id}/reimprimir`, {}, { params });
    }

    obtenerHistorialReimpresiones(id: number): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/${id}/reimpresiones`);
    }

    listarVentasPorPaciente(idPersonal: string): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/paciente/${idPersonal}`);
    }
}
