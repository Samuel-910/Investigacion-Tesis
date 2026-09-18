import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentoPlantilla } from '../models/documento.model';
import { environment } from '../../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class DocumentoService {
    private apiUrl = `${environment.apiUrl}/configuraciones/documentos`;

    constructor(private http: HttpClient) { }

    listarTodos(): Observable<DocumentoPlantilla[]> {
        return this.http.get<DocumentoPlantilla[]>(this.apiUrl);
    }

    obtenerPorId(id: number): Observable<DocumentoPlantilla> {
        return this.http.get<DocumentoPlantilla>(`${this.apiUrl}/${id}`);
    }

    actualizar(id: number, documento: DocumentoPlantilla): Observable<DocumentoPlantilla> {
        return this.http.put<DocumentoPlantilla>(`${this.apiUrl}/${id}`, documento);
    }

    crear(documento: DocumentoPlantilla): Observable<DocumentoPlantilla> {
        return this.http.post<DocumentoPlantilla>(this.apiUrl, documento);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
