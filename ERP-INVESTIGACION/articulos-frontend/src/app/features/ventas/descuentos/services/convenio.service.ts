import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConvenioService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/companias`;

    listarCompanias(): Observable<any> {
        return this.http.get<any>(this.apiUrl);
    }

    obtenerCompania(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/${id}`);
    }

    obtenerVinculos(idCompania: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/${idCompania}/vinculos`);
    }

    vincularPaciente(idCompania: number, idPaciente: number, dto: any = {}): Observable<any> {
        const params = new HttpParams().set('idPaciente', idPaciente.toString());
        return this.http.post<any>(`${this.apiUrl}/${idCompania}/vincular`, dto, { params });
    }

    vincularPacientesMasivo(idCompania: number, idPacientes: number[], dto: any = {}): Observable<any> {
        const params = new HttpParams().set('idPacientes', idPacientes.join(','));
        return this.http.post<any>(`${this.apiUrl}/${idCompania}/vincular-masivo`, dto, { params });
    }

    vincularPacientesMasivoDetalle(idCompania: number, vinculos: any[]): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idCompania}/vincular-masivo-detalle`, vinculos);
    }

    vincularPacientesPorDni(idCompania: number, dnis: string[], dto: any = {}): Observable<any> {
        const params = new HttpParams().set('dnis', dnis.join(','));
        return this.http.post<any>(`${this.apiUrl}/${idCompania}/vincular-dnis`, dto, { params });
    }

    vincularPacientesPorDniDetalle(idCompania: number, vinculos: any[]): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idCompania}/vincular-dnis-detalle`, vinculos);
    }

    actualizarVinculo(idVinculo: number, dto: any): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/vinculos/${idVinculo}`, dto);
    }

    eliminarVinculo(idVinculo: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/vinculos/${idVinculo}`);
    }

    listarDescuentos(idCompania: number, page = 0, size = 50): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('idCompania', idCompania.toString());
        return this.http.get<any>(`${environment.apiUrl}/descuentos`, { params });
    }

    crearDescuento(dto: any): Observable<any> {
        return this.http.post<any>(`${environment.apiUrl}/ventas/beneficios`, dto);
    }
}
