import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, Subject, throwError, of } from 'rxjs';
import { map, catchError, tap, finalize } from 'rxjs/operators';
import { Router } from '@angular/router';
import { MenuDinamicoService } from '../../../shared/sidebar/menu-dinamico.service';
import { PermissionService2 } from './permission2.service';

export interface LoginRequest {
    login: string;
    password: string;
}

export interface TokenInfo {
    timeRemainingMs: number;
    timeRemainingSeconds: number;
    expirationTimeMs: number;
    isExpired: boolean;
    token?: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    content: T;
    timestamp: string;
}

export interface LoginData {
    token: string;
    type: string;
    userId: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    sexo: string;
    roles: string[];
    permissions: string[];
    sessionTimeoutMs: number;
    puntoId: number;
    puntoNombre: string;
    sucursalId?: number;
    sucursalNombre?: string;
    accesos?: any[];
}

export interface Usuario {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    rol: string;
    token: string;
    sexo: string;
    permissions: string[];
    puntoId?: number;
    puntoNombre?: string;
    sucursalId?: number;
    sucursalNombre?: string;
    isAdmin?: boolean;
    menu?: any[];
    accesos?: any[];
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = 'http://localhost:8085/api/auth';
    private currentUserSubject: BehaviorSubject<Usuario | null>;
    public currentUser: Observable<Usuario | null>;

    // ✅ CAMBIADO: Subject en lugar de ReplaySubject para evitar emisiones de sesiones anteriores
    private sucursalChangedSubject = new Subject<string>();
    public onSucursalChanged$ = this.sucursalChangedSubject.asObservable();

    // ✅ NUEVO: Subject para confirmar cuando la UI se actualizó
    private sucursalUpdatedSubject = new BehaviorSubject<boolean>(false);
    public onSucursalUpdated$ = this.sucursalUpdatedSubject.asObservable();

    constructor(
        private http: HttpClient,
        private router: Router,
        private menuService: MenuDinamicoService,
        private permissionService: PermissionService2
    ) {
        const userStorage = localStorage.getItem('currentUser');
        this.currentUserSubject = new BehaviorSubject<Usuario | null>(
            userStorage ? JSON.parse(userStorage) : null
        );
        this.currentUser = this.currentUserSubject.asObservable();
    }

    public get currentUserValue(): Usuario | null {
        return this.currentUserSubject.value;
    }

    public get isAuthenticated(): boolean {
        return !!this.currentUserValue && !!this.currentUserValue.token;
    }

    login(login: string, password: string): Observable<LoginData> {
        return this.http.post<ApiResponse<LoginData>>(`${this.apiUrl}/login`, {
            login,
            password
        }).pipe(
            // 1. VER RESPUESTA EN BRUTO (Sin procesar)
            tap(responseBruta => {
                console.log('--- RESPUESTA BRUTA DEL BACKEND ---');
                console.log(responseBruta);
                console.log('-----------------------------------');
            }),

            map(apiResponse => apiResponse.data),

            tap(loginData => {
                console.log('Backend login data:', loginData);

                const rolPrincipal = loginData.roles && loginData.roles.length > 0
                    ? loginData.roles[0]
                    : 'PACIENTE';

                const user: Usuario = {
                    id: loginData.userId,
                    username: loginData.username,
                    rol: rolPrincipal,
                    token: loginData.token,
                    sexo: loginData.sexo,
                    firstName: loginData.firstName,
                    lastName: loginData.lastName,
                    permissions: loginData.permissions,
                    puntoNombre: loginData.puntoNombre,
                    sucursalId: loginData.sucursalId,
                    sucursalNombre: loginData.sucursalNombre,
                    isAdmin: loginData.roles?.some(r => r.includes('ADMIN')),
                    accesos: loginData.accesos
                };

                // Limpiar claves antiguas redundantes
                localStorage.removeItem('userId');
                localStorage.removeItem('userFullName');
                localStorage.removeItem('puntoId');
                localStorage.removeItem('puntoNombre');
                localStorage.removeItem('userPermisos');
                localStorage.removeItem('isAdmin');

                // ✅ Única fuente de verdad: currentUser y token (para interceptores)
                localStorage.setItem('currentUser', JSON.stringify(user));
                localStorage.setItem('token', loginData.token);

                this.permissionService.setUser(loginData);

                this.menuService.cargarMenu(1).subscribe({
                    next: () => console.log('✅ Menú cargado en login (ID 1 forzado)'),
                    error: (err) => console.error('❌ Error cargando menú:', err)
                });

                this.currentUserSubject.next(user);
            }),
            catchError(error => {
                console.error('Error en login:', error);
                // Si quieres ver el error en bruto también:
                console.log('--- ERROR BRUTO ---', error);
                return throwError(() => error);
            })
        );
    }

    logout(): void {
        const token = localStorage.getItem('token');

        if (token) {
            const headers = new HttpHeaders({
                'Authorization': `Bearer ${token}`
            });

            this.http.post(`${this.apiUrl}/logout`, {}, { headers })
                .pipe(
                    finalize(() => this.doLogoutCleanup())
                )
                .subscribe({
                    next: () => console.log('Logout registrado exitosamente'),
                    error: (err) => console.error('Error en logout:', err)
                });
        } else {
            this.doLogoutCleanup();
        }
    }

    private doLogoutCleanup(): void {
        localStorage.clear();

        this.permissionService.clearPermisos();
        this.menuService.clearMenu();

        // Resetear subjects
        this.sucursalUpdatedSubject.next(false);
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
    }

    hasAnyPermission(permissions: string[]): boolean {
        const user = this.currentUserValue;
        if (!user || !user.permissions) {
            return false;
        }
        return permissions.some(permission => user.permissions.includes(permission));
    }

    getSucursalesAutorizadas(userId: number): Observable<ApiResponse<any[]>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<any[]>>(
            `${this.apiUrl}/sucursales/${userId}`,
            { headers }
        ).pipe(
            catchError(error => {
                console.error('Error al obtener sucursales:', error);
                return throwError(() => error);
            })
        );
    }

    getPuntosAutorizados(userId: number): Observable<ApiResponse<any[]>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<any[]>>(
            `${this.apiUrl}/sucursales/${userId}/puntos`,
            { headers }
        ).pipe(
            catchError(error => {
                console.error('Error al obtener puntos:', error);
                return throwError(() => error);
            })
        );
    }

    seleccionarSucursal(userId: number, sucursalId: number, password: string): Observable<ApiResponse<{ token: string; sucursalNombre: string }>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<ApiResponse<{ token: string; sucursalNombre: string }>>(
            `${this.apiUrl}/sucursales/select`,
            { userId, sucursalId, password },
            { headers }
        ).pipe(
            tap(response => {
                if (response.success && response.data) {
                    localStorage.setItem('token', response.data.token);

                    // ✅ Actualizar estado completo desde el nuevo token
                    this.updateStateFromToken(response.data.token);

                    // ✅ Resetear el flag de actualización
                    this.sucursalUpdatedSubject.next(false);

                    console.log('🔔 [AUTH SERVICE] Emitiendo cambio de sucursal:', response.data.sucursalNombre);
                    this.sucursalChangedSubject.next(response.data.sucursalNombre);
                }
            }),
            catchError(error => {
                console.error('Error al seleccionar sucursal:', error);
                return throwError(() => error);
            })
        );
    }

    seleccionarPunto(userId: number, puntoId: number, password: string): Observable<ApiResponse<{ token: string; sucursalNombre: string }>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<ApiResponse<{ token: string; sucursalNombre: string }>>(
            `${this.apiUrl}/puntos/select`,
            { userId, puntoId, password },
            { headers }
        ).pipe(
            tap(response => {
                if (response.success && response.data) {
                    console.log('🔧 [AUTH SERVICE] Guardando nuevo token tras cambio de punto');
                    localStorage.setItem('token', response.data.token);

                    // ✅ Actualizar estado completo (Reactivo + LocalStorage) desde el nuevo token
                    this.updateStateFromToken(response.data.token);

                    this.sucursalUpdatedSubject.next(false);
                    this.sucursalChangedSubject.next(response.data.sucursalNombre);
                }
            }),
            catchError(error => {
                console.error('Error al seleccionar punto:', error);
                return throwError(() => error);
            })
        );
    }

    // ✅ NUEVO: Método para confirmar que la UI se actualizó
    confirmSucursalUpdated(): void {
        console.log('✅ [AUTH SERVICE] UI confirmó actualización de sucursal');
        this.sucursalUpdatedSubject.next(true);
    }

    private decodeToken(token: string): any {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        } catch (error) {
            console.error('Error al decodificar token UTF-8:', error);
            return null;
        }
    }

    getSucursalFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload ? payload.sucursalNombre || null : null;
    }

    getPuntoFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload ? payload.puntoNombre || null : null;
    }

    getPuntoIdFromToken(): number | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload ? payload.puntoId || null : null;
    }

    private updateStateFromToken(token: string): void {
        try {
            const payload = this.decodeToken(token);
            if (!payload) return;
            console.log('📦 [AUTH SERVICE] Actualizando estado desde token:', payload);

            const userStorage = localStorage.getItem('currentUser');
            if (userStorage) {
                const user: Usuario = JSON.parse(userStorage);
                user.token = token;
                user.puntoId = payload.puntoId;
                user.puntoNombre = payload.puntoNombre;
                user.sucursalId = payload.sucursalId;
                user.sucursalNombre = payload.sucursalNombre;
                user.permissions = payload.permissions;

                // Actualizar Única fuente de verdad
                localStorage.setItem('currentUser', JSON.stringify(user));
                localStorage.setItem('token', token);

                // Notificar a los suscriptores reactivos
                this.currentUserSubject.next(user);

                // Actualizar permisos si es necesario
                this.permissionService.setUser(payload);

                console.log('✅ [AUTH SERVICE] Estado de usuario sincronizado:', user.puntoNombre);
            }
        } catch (error) {
            console.error('❌ Error al actualizar estado desde token:', error);
        }
    }

    getSucursalIdFromToken(): number | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload && payload.sucursalId ? Number(payload.sucursalId) : null;
    }

    getUsuarioIdFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload ? payload.sub || payload.username || null : null;
    }

    getUserIdFromToken(): number | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload && payload.userId ? Number(payload.userId) : null;
    }

    cambiarContraseña(contraseñaActual: string, contraseñaNueva: string): Observable<any> {
        return this.http.put(`${this.apiUrl}/cambiar-contraseña`, {
            contraseñaActual,
            contraseñaNueva
        }).pipe(
            catchError(error => {
                console.error('Error al cambiar contraseña:', error);
                return throwError(() => error);
            })
        );
    }

    verificarToken(): Observable<boolean> {
        const token = localStorage.getItem('token');
        if (!token) {
            return of(false);
        }

        return this.http.get<{ valid: boolean }>(`${this.apiUrl}/verificar-token`).pipe(
            map(response => response.valid),
            catchError(() => {
                this.logout();
                return of(false);
            })
        );
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    getTokenInfo(): Observable<ApiResponse<TokenInfo>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<TokenInfo>>(`${this.apiUrl}/token-info`, { headers });
    }

    getSessionTimeout(): Observable<number> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });
        return this.http.get<ApiResponse<{ valor: number }>>(`${this.apiUrl}/timeout`, { headers })
            .pipe(map(response => response.data.valor));
    }

    updateSessionTimeout(minutes: number): Observable<void> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });
        return this.http.put<void>(`${this.apiUrl}/update-timeout`, { minutes }, { headers });
    }

    refreshToken(): Observable<ApiResponse<TokenInfo>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<ApiResponse<TokenInfo>>(`${this.apiUrl}/refresh-token`, {}, { headers })
            .pipe(
                tap(response => {
                    if (response.success && response.data && response.data.token) {
                        localStorage.setItem('token', response.data.token);
                    }
                })
            );
    }
}