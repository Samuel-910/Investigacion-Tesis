import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import {
  UserResponse,
  CreateUserRequest,
  UpdateUserRequest,
  UpdateMyProfileRequest,
  ChangePasswordRequest,
} from '../models/user.model';


@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  // ========================================
  // CRUD BÁSICO
  // ========================================

  createUser(request: CreateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(this.apiUrl, request);
  }

  updateUser(id: number, request: UpdateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}`, request);
  }

  getUserById(id: number): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}`);
  }

  deleteUser(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getMe(): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.apiUrl}/me`);
  }

  updateMe(request: UpdateMyProfileRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.apiUrl}/me`, request);
  }

  // ========================================
  // LISTADOS PAGINADOS Y BÚSQUEDA
  // ========================================

  private getPaginationParams(page: number, size: number, sort: string): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
  }

  getAllUsers(
    page = 0,
    size = 10,
    sort = 'nombre'
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, sort);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(this.apiUrl, { params });
  }

  getActiveUsers(page = 0, size = 10): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre');
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/active`, {
      params,
    });
  }

  getUsersByRole(
    roleName: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre');
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(
      `${this.apiUrl}/role/${roleName}`,
      { params }
    );
  }

  getUsersByModulo(
    moduloNombre: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre');
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(
      `${this.apiUrl}/modulo/${moduloNombre}`,
      { params }
    );
  }

  searchUsers(q: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre').set('q', q);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/search`, {
      params,
    });
  }

  searchByFilter(
    q: string,
    type: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre').set('query', q).set('type', type);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/filter`, {
      params,
    });
  }

  // ========================================
  // GESTIÓN DE SEGURIDAD Y ESTADO
  // ========================================

  /**
   * Activar o desactivar un usuario mediante PATCH
   */
  toggleUserStatus(id: number, active: boolean): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('active', active.toString());
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/status`, {}, { params });
  }

  /**
   * Cambiar contraseña (puede ser llamado por el admin o por el propio usuario)
   */
  changePassword(id: number, request: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/change-password`, request);
  }

  // ========================================
  // GESTIÓN DE ROLES
  // ========================================

  assignRoles(id: number, roleIds: number[]): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/roles`, roleIds);
  }

  assignModulosByName(id: number, moduloNames: string[]): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/modulos-by-name`, moduloNames);
  }

  /**
   * Remueve roles específicos (Envía los IDs en el cuerpo de la petición DELETE)
   */
  removeRoles(id: number, roleIds: number[]): Observable<ApiResponse<UserResponse>> {
    return this.http.delete<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/roles`, {
      body: roleIds,
    });
  }
  /** Obtener sucursales asignadas a un usuario */
  getSucursalesAsignadas(userId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/${userId}/sucursales`);
  }

  /** Asignar sucursales a un usuario */
  assignSucursales(userId: number, sucursalIds: number[]): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${userId}/sucursales`, sucursalIds);
  }

  /** Remover sucursales de un usuario */
  removeSucursales(userId: number, sucursalIds: number[]): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${userId}/sucursales`, {
      body: sucursalIds,
    });
  }

  /** Asignar puntos de venta a un usuario */
  assignPuntos(userId: number, puntoIds: number[]): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${userId}/puntos`, puntoIds);
  }

  /** Remover puntos de venta de un usuario */
  removePuntos(userId: number, puntoIds: number[]): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${userId}/puntos`, {
      body: puntoIds,
    });
  }

  /** Obtener puntos de venta asignados a un usuario */
  getPuntosAsignados(userId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/${userId}/puntos`);
  }

  private buildPageParams(page: number, size: number, sort: string, direction: string): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort},${direction}`); // Spring prefiere este formato para el Pageable
  }
}
