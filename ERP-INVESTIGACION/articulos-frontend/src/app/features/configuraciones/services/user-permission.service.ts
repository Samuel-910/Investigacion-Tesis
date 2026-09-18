import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import {
    UserWithPermissionsResponse, UserDirectPermissionResponse,
    AssignDirectPermissionsRequest, GrantDirectPermissionRequest,
    TogglePermissionModeRequest
} from '../models/user-permission.model';
import { Permission } from '../models/permission.model';

@Injectable({
    providedIn: 'root'
})
export class UserPermissionService {
    private readonly apiUrl = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    // ========================================
    // ASIGNACIÓN Y GESTIÓN DE PERMISOS
    // ========================================

    assignDirectPermissions(request: AssignDirectPermissionsRequest): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.post<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/permissions/assign`, request);
    }

    grantDirectPermission(request: GrantDirectPermissionRequest): Observable<ApiResponse<UserDirectPermissionResponse>> {
        return this.http.post<ApiResponse<UserDirectPermissionResponse>>(`${this.apiUrl}/permissions/grant`, request);
    }

    removeDirectPermissions(userId: number, permissionIds: number[]): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.delete<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/${userId}/permissions`, {
            body: permissionIds
        });
    }

    revokeDirectPermission(userId: number, permissionId: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${userId}/permissions/${permissionId}`);
    }

    clearAllDirectPermissions(userId: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${userId}/permissions/all`);
    }

    // ========================================
    // CONSULTAS DE PERMISOS
    // ========================================

    getDirectPermissions(userId: number): Observable<ApiResponse<UserDirectPermissionResponse[]>> {
        return this.http.get<ApiResponse<UserDirectPermissionResponse[]>>(`${this.apiUrl}/${userId}/permissions/direct`);
    }

    getEffectivePermissions(userId: number): Observable<ApiResponse<Permission[]>> {
        return this.http.get<ApiResponse<Permission[]>>(`${this.apiUrl}/${userId}/permissions/effective`);
    }

    getUserWithPermissions(userId: number): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.get<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/${userId}/with-permissions`);
    }

    hasPermission(userId: number, permissionName: string): Observable<ApiResponse<{ hasPermission: boolean }>> {
        return this.http.get<ApiResponse<{ hasPermission: boolean }>>(`${this.apiUrl}/${userId}/has-permission/${permissionName}`);
    }

    // ========================================
    // CONFIGURACIÓN DE MODO Y COPIADO
    // ========================================

    togglePermissionMode(request: TogglePermissionModeRequest): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.put<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/permissions/toggle-mode`, request);
    }

    copyPermissions(sourceId: number, targetId: number, reason?: string): Observable<ApiResponse<UserWithPermissionsResponse>> {
        let params = new HttpParams();
        if (reason) params = params.set('reason', reason);
        return this.http.post<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/${sourceId}/permissions/copy-to/${targetId}`, {}, { params });
    }

    // ========================================
    // MANTENIMIENTO Y AUDITORÍA
    // ========================================

    deactivateExpiredPermissions(): Observable<ApiResponse<{ deactivatedCount: number }>> {
        return this.http.post<ApiResponse<{ deactivatedCount: number }>>(`${this.apiUrl}/permissions/deactivate-expired`, {});
    }

    getExpiringPermissions(days: number = 7): Observable<ApiResponse<UserDirectPermissionResponse[]>> {
        const params = new HttpParams().set('days', days.toString());
        return this.http.get<ApiResponse<UserDirectPermissionResponse[]>>(`${this.apiUrl}/permissions/expiring`, { params });
    }

    getPermissionStats(userId: number): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/${userId}/permissions/stats`);
    }
}