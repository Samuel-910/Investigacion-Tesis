import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { Permission, PermissionRequest } from '../models/permission.model';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private readonly apiUrl = `${environment.apiUrl}/permissions`;

  constructor(private http: HttpClient) { }

  // ========================================
  // CRUD BÁSICO
  // ========================================

  createPermission(request: PermissionRequest): Observable<ApiResponse<Permission>> {
    return this.http.post<ApiResponse<Permission>>(this.apiUrl, request);
  }

  updatePermission(id: number, request: PermissionRequest): Observable<ApiResponse<Permission>> {
    return this.http.put<ApiResponse<Permission>>(`${this.apiUrl}/${id}`, request);
  }

  deletePermission(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getPermissionById(id: number): Observable<ApiResponse<Permission>> {
    return this.http.get<ApiResponse<Permission>>(`${this.apiUrl}/${id}`);
  }

  getPermissionByName(name: string): Observable<ApiResponse<Permission>> {
    return this.http.get<ApiResponse<Permission>>(`${this.apiUrl}/name/${name}`);
  }

  // ========================================
  // LISTADOS CON PAGINACIÓN
  // ========================================

  private getPaginationParams(page: number, size: number, sort: string): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
  }

  getAllPermissions(
    page = 0,
    size = 10,
    sort = 'name'
  ): Observable<ApiResponse<PageResponse<Permission>>> {
    const params = this.getPaginationParams(page, size, sort);
    return this.http.get<ApiResponse<PageResponse<Permission>>>(this.apiUrl, { params });
  }

  getActivePermissions(
    page = 0,
    size = 20,
    sort = 'name'
  ): Observable<ApiResponse<PageResponse<Permission>>> {
    const params = this.getPaginationParams(page, size, sort);
    return this.http.get<ApiResponse<PageResponse<Permission>>>(`${this.apiUrl}/active`, {
      params,
    });
  }

  searchByFilter(
    query: string,
    type: string = 'ALL',
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PageResponse<Permission>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'name')
      .set('query', query)
      .set('type', type);

    return this.http.get<ApiResponse<PageResponse<Permission>>>(`${this.apiUrl}/filter`, {
      params,
    });
  }

  // ========================================
  // LISTADOS SIN PAGINACIÓN (Dropdowns)
  // ========================================

  getAllPermissionsList(): Observable<ApiResponse<Permission[]>> {
    return this.http.get<ApiResponse<Permission[]>>(`${this.apiUrl}/list`);
  }

  getActivePermissionsList(): Observable<ApiResponse<Permission[]>> {
    return this.http.get<ApiResponse<Permission[]>>(`${this.apiUrl}/active/list`);
  }

  // ========================================
  // OPERACIONES POR MÓDULO
  // ========================================

  getPermissionsByModule(
    module: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<Permission>>> {
    const params = this.getPaginationParams(page, size, 'name');
    return this.http.get<ApiResponse<PageResponse<Permission>>>(`${this.apiUrl}/module/${module}`, {
      params,
    });
  }

  getAllModules(): Observable<ApiResponse<string[]>> {
    return this.http.get<ApiResponse<string[]>>(`${this.apiUrl}/modules`);
  }

  // ========================================
  // UTILIDADES
  // ========================================

  checkPermissionExists(name: string): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/exists/${name}`);
  }
}
