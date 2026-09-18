import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class CompaniaService {
    private apiUrl = `${environment.apiUrl}/v1/companias`;

    constructor(private http: HttpClient) { }

    listar(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}?page=0&size=1000`);
    }
}
