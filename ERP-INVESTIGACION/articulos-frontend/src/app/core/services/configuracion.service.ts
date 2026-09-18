import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ConfiguracionService {
    private configUrl = `${environment.apiUrl}/config`;
    private authUrl = `${environment.apiUrl}/auth`;

    constructor(private http: HttpClient) { }

    // Deprecated - Only for global settings
    getConfig(clave: string): Observable<any> {
        const headers = this.getHeaders();
        return this.http.get<any>(`${this.configUrl}/${clave}`, { headers });
    }

    // Deprecated - Only for global settings
    updateConfig(clave: string, valor: string): Observable<any> {
        const headers = this.getHeaders();
        return this.http.put<any>(`${this.configUrl}/${clave}`, { valor }, { headers });
    }

    // --- Global Session Timeout (Admin) ---
    getGlobalSessionTimeout(): Observable<any> {
        const headers = this.getHeaders();
        return this.http.get<any>(`${this.configUrl}/session-timeout`, { headers });
    }

    updateGlobalSessionTimeout(minutes: number): Observable<any> {
        const headers = this.getHeaders();
        return this.http.post<any>(`${this.configUrl}/session-timeout`, { minutes }, { headers });
    }

    // --- User Specific Methods ---

    getUserTimeout(): Observable<any> {
        const headers = this.getHeaders();
        return this.http.get<any>(`${this.authUrl}/timeout`, { headers });
    }

    updateUserTimeout(valor: number): Observable<any> {
        const headers = this.getHeaders();
        return this.http.put<any>(`${this.authUrl}/update-timeout`, { valor }, { headers });
    }

    private getHeaders(): HttpHeaders {
        const user = localStorage.getItem('currentUser');
        const token = user ? JSON.parse(user).token : '';
        return new HttpHeaders({
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        });
    }
}
