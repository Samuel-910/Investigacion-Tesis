import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role, RoleRequest } from '../models/role.model';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly apiUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) { }

  // ========================================
  // CRUD BÁSICO
  // ========================================

  createRole(request: RoleRequest): Observable<ApiResponse<Role>> {
    return this.http.post<ApiResponse<Role>>(this.apiUrl, request);
  }

  updateRole(id: number, request: RoleRequest): Observable<ApiResponse<Role>> {
    return this.http.put<ApiResponse<Role>>(`${this.apiUrl}/${id}`, request);
  }

  getRoleById(id: number): Observable<ApiResponse<Role>> {
    return this.http.get<ApiResponse<Role>>(`${this.apiUrl}/${id}`);
  }

  getRoleByName(name: string): Observable<ApiResponse<Role>> {
    return this.http.get<ApiResponse<Role>>(`${this.apiUrl}/name/${name}`);
  }

  deleteRole(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  // ========================================
  // LISTADOS PAGINADOS
  // ========================================

  getAllRoles(page = 0, size = 10, sort = 'name'): Observable<ApiResponse<PageResponse<Role>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<ApiResponse<PageResponse<Role>>>(this.apiUrl, { params });
  }

  getActiveRoles(page = 0, size = 20, sort = 'name'): Observable<ApiResponse<PageResponse<Role>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<ApiResponse<PageResponse<Role>>>(`${this.apiUrl}/active`, { params });
  }

  searchByFilter(
    q: string,
    type: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<Role>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'name')
      .set('query', q)
      .set('type', type);
    return this.http.get<ApiResponse<PageResponse<Role>>>(`${this.apiUrl}/filter`, { params });
  }

  // ========================================
  // GESTIÓN DE PERMISOS
  // ========================================

  /**
   * Asigna una lista de IDs de permisos a un rol específico
   * POST /api/roles/{roleId}/permissions
   */
  assignPermissions(roleId: number, permissionIds: number[]): Observable<ApiResponse<Role>> {
    return this.http.post<ApiResponse<Role>>(`${this.apiUrl}/${roleId}/permissions`, permissionIds);
  }

  /**
   * Remueve una lista de IDs de permisos de un rol específico
   * DELETE /api/roles/{roleId}/permissions
   */
  removePermissions(roleId: number, permissionIds: number[]): Observable<ApiResponse<Role>> {
    // En Angular, para enviar un body en un DELETE se usa la propiedad 'body' en las opciones
    return this.http.delete<ApiResponse<Role>>(`${this.apiUrl}/${roleId}/permissions`, {
      body: permissionIds,
    });
  }
}
