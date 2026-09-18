import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse } from '../../features/auth/services/auth.service';
import { environment } from '../../environments/environment';

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface ResetPasswordRequest {
    newPassword: string;
    confirmPassword: string;
}

export interface UserResponse {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    nombreCompleto: string;
    numdoc?: string;
    ruc?: string;
    active: boolean;
    roles: string[];
    isImported?: boolean;
    tipoBeneficiario?: 'BENEFICIARIO' | 'FAMILIAR';
    isCompania?: boolean;
    fechaNacimiento?: string;
    fecha_nacimiento?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    /**
     * Obtiene solo usuarios activos con paginación
     */
    getActiveUsers(page: number = 0, size: number = 10): Observable<PageResponse<UserResponse>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<PageResponse<UserResponse>>>(
            `${this.apiUrl}/active?page=${page}&size=${size}`,
            { headers }
        ).pipe(
            map(response => response.data),
            catchError(error => {
                console.error('Error obteniendo usuarios activos:', error);
                return throwError(() => error);
            })
        );
    }

    /**
     * Busca usuarios por nombre o DNI
     */
    searchUsers(query: string, page: number = 0, size: number = 10): Observable<PageResponse<UserResponse>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<PageResponse<UserResponse>>>(
            `${this.apiUrl}/search?q=${query}&page=${page}&size=${size}`,
            { headers }
        ).pipe(
            map(response => response.data),
            catchError(error => {
                console.error('Error buscando usuarios:', error);
                return throwError(() => error);
            })
        );
    }

    /**
     * Cambia la contraseña de un usuario
     */
    changePassword(userId: number, request: ChangePasswordRequest): Observable<boolean> {
        const token = localStorage.getItem('token');
        if (!token) {
            return throwError(() => new Error('No existe token de autenticación'));
        }

        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${userId}/change-password`, request, { headers })
            .pipe(
                map(response => response.success),
                catchError(error => {
                    console.error('Error cambiando contraseña:', error);
                    return throwError(() => error);
                })
            );
    }

    /**
     * Cambia la contraseña de un usuario (Reseteo administrativo)
     */
    adminResetPassword(userId: number, request: ResetPasswordRequest): Observable<boolean> {
        const token = localStorage.getItem('token');
        if (!token) {
            return throwError(() => new Error('No existe token de autenticación'));
        }

        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${userId}/reset-password`, request, { headers })
            .pipe(
                map(response => response.success),
                catchError(error => {
                    console.error('Error reseteando contraseña:', error);
                    return throwError(() => error);
                })
            );
    }

    /**
     * Obtiene usuarios por rol
     */
    getUsersByRole(roleName: string): Observable<any> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/role/${roleName}`, { headers })
            .pipe(
                map(response => response.data),
                catchError(error => {
                    console.error(`Error obteniendo usuarios por rol ${roleName}:`, error);
                    return throwError(() => error);
                })
            );
    }
}
