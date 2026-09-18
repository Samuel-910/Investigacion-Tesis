// src/app/components/user-permission-management/user-permission-management.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Permission } from '../../models/permission.model';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { AlertService } from '../../../../core/services/alert.service';
import { UserResponse } from '../../models/user.model';
import { AssignDirectPermissionsRequest, UserWithPermissionsResponse } from '../../models/user-permission.model';
import { PermissionService } from '../../services/permission.service';
import { UserPermissionService } from '../../services/user-permission.service';
import { UserService } from '../../services/user.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';

interface ModulePermissionsGroup {
  module: string;
  displayName: string;
  icon: string;
  permissions: Permission[];
}

@Component({
  selector: 'app-user-permission-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderComponent,
    SidebarComponent,
    BreadcrumbComponent,
    PageHeaderComponent,
    FormInputComponent
  ],
  templateUrl: './user-with-permissions.component.html',
  styleUrls: ['./user-with-permissions.component.css'],
})
export class UserPermissionManagementComponent implements OnInit {
  // #region Breadcrumb
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Gestión de Permisos Directos' },
  ];
  // #endregion
  // #region header
  totalElements: number = 0;
  tituloPagina: string = 'Permisos Directos por Usuario ';
  nombre: string = '';

  // #endregion
  // Listas
  users: UserResponse[] = [];
  allPermissions: Permission[] = [];
  moduleGroups: ModulePermissionsGroup[] = [];
  selectedUser: UserWithPermissionsResponse | null = null;
  selectedPermissions: Set<number> = new Set();
  originalPermissions: Set<number> = new Set();
  loading = false;
  saving = false;
  loadingUser = false;
  useDirectPermissions = false;
  searchTerm = '';
  showActiveOnly = true;
  permissionReason = '';
  expirationDate: string | null = null;
  message = '';
  messageType: 'success' | 'error' | 'info' | 'warning' = 'info';
  showClearConfirm = false;
  showModeToggleConfirm = false;

  constructor(
    private userPermissionService: UserPermissionService,
    private userService: UserService,
    private permissionService: PermissionService,
    private cdRef: ChangeDetectorRef,
    public sidebarService: SidebarService,
    private alertService: AlertService
  ) { }

  ngOnInit(): void {
    this.loadUsers();
    this.loadPermissions();
  }

  // ========== CARGA DE DATOS ==========

  loadUsers(): void {
    this.loading = true;
    const request = this.showActiveOnly
      ? this.userService.getActiveUsers(0, 100) // Ajustado a 100 por restricción de API
      : this.userService.getAllUsers(0, 100);

    request.subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.users = response.data.content;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
        this.cdRef.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.toastError(err.error?.message || 'Error al cargar usuarios');
      },
    });
  }

  loadPermissions(): void {
    this.permissionService.getAllPermissions(0, 100).subscribe({
      next: (response) => {
        this.allPermissions = response.data.content;
        this.groupPermissionsByModule();
      },
      error: (err) =>
        this.toastError(err.error?.message || 'No se pudieron cargar los permisos base'),
    });
  }

  loadUserDetails(id: number): void {
    this.loadingUser = true;
    this.userPermissionService.getUserWithPermissions(id).subscribe({
      next: (response) => {
        this.loadingUser = false;
        if (response.success && response.data) {
          this.selectedUser = response.data;
          this.useDirectPermissions = this.selectedUser.useDirectPermissions;

          // Mapeamos los permisos directos actuales
          const permissionsArray = this.selectedUser.directPermissions || [];
          this.selectedPermissions = new Set(permissionsArray.map((dp) => dp.permission.id));
          this.originalPermissions = new Set(this.selectedPermissions);

          this.cdRef.detectChanges();
        } else {
          this.alertService.warning('Atención', response.message || 'No se pudo obtener la información detallada del usuario');
        }
      },
      error: (err) => {
        this.loadingUser = false;
        console.error('Error al cargar detalles:', err);
        this.alertService.error('Error', err.error?.message || 'Error de conexión al intentar cargar los detalles del usuario');
      },
    });
  }

  groupPermissionsByModule(): void {
    // 1. Verificación de seguridad: si no hay permisos, limpiar grupos y salir
    if (!this.allPermissions) {
      this.moduleGroups = [];
      return;
    }

    const grouped = new Map<string, Permission[]>();

    this.allPermissions.forEach((permission) => {
      // Aseguramos que el módulo siempre sea un string, incluso si viene null de la DB
      const moduleName = permission.module || 'general';

      if (!grouped.has(moduleName)) {
        grouped.set(moduleName, []);
      }
      grouped.get(moduleName)!.push(permission);
    });

    this.moduleGroups = Array.from(grouped.entries())
      .map(([module, permissions]) => ({
        module: module,
        displayName: this.getModuleDisplayName(module),
        icon: this.getModuleIcon(module),
        // Verificación extra para evitar errores en localeCompare
        permissions: (permissions || []).sort((a, b) => (a.name || '').localeCompare(b.name || '')),
      }))
      .sort((a, b) => (a.displayName || '').localeCompare(b.displayName || ''));
  }

  // ========== SELECCIÓN DE USUARIO ==========

  selectUser(user: UserResponse): void {
    console.log('Usuario seleccionado:', user);

    // Limpiamos estados previos antes de cargar el nuevo
    this.selectedUser = null;
    this.selectedPermissions = new Set();
    this.originalPermissions = new Set();
    this.permissionReason = '';
    this.expirationDate = null;

    // Validación preventiva
    if (!user || !user.id) {
      this.alertService.warning('Atención', 'El usuario seleccionado no tiene un identificador válido.');
      return;
    }

    // Llamamos a la carga de detalles (que ya tiene sus propios Swal configurados)
    this.loadUserDetails(user.id);
  }

  // ========== GESTIÓN DE PERMISOS ==========

  hasPermission(permissionId: number): boolean {
    return this.selectedPermissions.has(permissionId);
  }

  areAllModulePermissionsSelected(module: ModulePermissionsGroup): boolean {
    return module.permissions.every((p) => this.selectedPermissions.has(p.id));
  }

  areSomeModulePermissionsSelected(module: ModulePermissionsGroup): boolean {
    const selectedCount = module.permissions.filter((p) =>
      this.selectedPermissions.has(p.id)
    ).length;
    return selectedCount > 0 && selectedCount < module.permissions.length;
  }

  hasChanges(): boolean {
    if (this.selectedPermissions.size !== this.originalPermissions.size) {
      return true;
    }

    for (const id of this.selectedPermissions) {
      if (!this.originalPermissions.has(id)) {
        return true;
      }
    }

    return false;
  }

  // ========== GUARDAR PERMISOS ==========

  savePermissions(form: any): void {
    if (!this.selectedUser || !this.useDirectPermissions) return;

    if (form.invalid) {
      this.alertService.error('Error', 'Error de validación en los campos');
      return;
    }

    // Validación de motivo con formato Atención
    if (!this.permissionReason.trim()) {
      this.alertService.warning('Atención', 'Debes proporcionar un motivo para asignar permisos especiales');
      return;
    }

    this.alertService.confirm(
      '¿Guardar cambios?',
      'Se actualizarán los permisos directos del usuario.',
      'Sí, guardar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) {
        this.executeSave();
      }
    });
  }

  private executeSave(): void {
    this.alertService.loading('Procesando...', 'Actualizando privilegios del usuario');

    this.saving = true;
    const request: AssignDirectPermissionsRequest = {
      userId: this.selectedUser!.id,
      permissionIds: Array.from(this.selectedPermissions),
      reason: this.permissionReason,
      expiresAt: this.expirationDate || undefined,
    };

    this.userPermissionService.assignDirectPermissions(request).subscribe({
      next: (response) => {
        this.saving = false;

        if (response.success) {
          this.alertService.success('¡Actualizado!', response.message || 'Permisos actualizados correctamente');
          this.selectedUser = response.data;
          this.originalPermissions = new Set(this.selectedPermissions);
          this.loadUsers(); // Refrescar lista general
        } else {
          this.alertService.warning('Atención', response.message || 'El servidor no pudo procesar la actualización');
        }
      },
      error: (err) => {
        this.saving = false;
        console.error('Error al guardar permisos:', err);
        this.alertService.error('Error en la operación', err.error?.message || 'Error de conexión al intentar guardar los cambios');
      },
    });
  }
  cancelChanges(): void {
    this.selectedPermissions = new Set(this.originalPermissions);
  }

  // ========== CAMBIAR MODO ==========

  toggleMode(): void {
    if (!this.selectedUser) return;

    const willUseDirect = !this.useDirectPermissions;

    // Validación: No permitir modo ROL si el usuario no tiene roles asignados
    if (!willUseDirect && (!this.selectedUser.roles || this.selectedUser.roles.length === 0)) {
      this.alertService.warning('Atención', 'El usuario no tiene roles asignados. No se puede activar el modo "POR ROL" sin una base de permisos.');
      return;
    }

    this.alertService.confirm(
      'Cambiar modo de permisos',
      `¿Deseas cambiar al modo ${willUseDirect ? 'DIRECTO' : 'POR ROL'}?`,
      'Sí, cambiar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) {
        this.confirmToggleMode(willUseDirect);
      }
    });
  }

  private confirmToggleMode(newMode: boolean): void {
    this.alertService.loading('Cambiando modo...', 'Espere un momento por favor');

    this.saving = true;
    this.userPermissionService
      .togglePermissionMode({
        userId: this.selectedUser!.id,
        useDirectPermissions: newMode,
      })
      .subscribe({
        next: (response) => {
          this.saving = false;

          if (response.success) {
            this.alertService.success('Modo Actualizado', `El modo se cambió exitosamente a: ${newMode ? 'DIRECTO' : 'ROL'}`);

            this.selectedUser = response.data;
            this.useDirectPermissions = newMode;
            this.loadUserDetails(this.selectedUser.id);
          } else {
            this.alertService.warning('Atención', response.message || 'No se pudo completar el cambio de modo');
          }
        },
        error: (err) => {
          this.saving = false;
          console.error('Error al cambiar modo:', err);
          this.alertService.error('Error en la operación', err.error?.message || 'Error de conexión al intentar cambiar el modo de permisos');
        },
      });
  }

  cancelToggleMode(): void {
    this.showModeToggleConfirm = false;
  }

  // ========== LIMPIAR PERMISOS ==========

  openClearConfirm(): void {
    this.showClearConfirm = true;
  }

  confirmClearPermissions(): void {
    if (!this.selectedUser) return;

    this.alertService.confirm(
      '¿Limpiar todos los permisos?',
      `Se eliminarán permanentemente todos los permisos directos asignados a "${this.selectedUser.fullName}". El usuario volverá a depender únicamente de sus roles.`,
      'Sí, limpiar todo',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) {
        this.executeClearPermissions();
      }
    });
  }

  private executeClearPermissions(): void {
    this.alertService.loading('Limpiando...', 'Eliminando privilegios directos');

    this.saving = true;
    this.userPermissionService.clearAllDirectPermissions(this.selectedUser!.id).subscribe({
      next: (response) => {
        this.saving = false;

        if (response.success) {
          this.alertService.success('¡Limpieza completada!', response.message || 'Todos los permisos directos han sido eliminados');

          this.loadUserDetails(this.selectedUser!.id);
          this.showClearConfirm = false; // Solo si usas un modal adicional
          this.loadUsers(); // Refrescar lista general
        } else {
          this.alertService.warning('Atención', response.message || 'No se pudo completar la limpieza de permisos');
        }
      },
      error: (error) => {
        this.saving = false;
        console.error('Error al limpiar permisos:', error);
        this.alertService.error('Error en la operación', error.error?.message || 'Error de conexión al intentar limpiar los permisos');
      },
    });
  }

  cancelClearPermissions(): void {
    this.showClearConfirm = false;
  }

  // ========== UTILIDADES ==========

  get filteredUsers(): UserResponse[] {
    return this.users.filter(
      (user) =>
        user.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  getPermissionShortName(permissionName: string): string {
    const parts = permissionName.split('_');
    return parts.length > 1 ? parts.slice(1).join(' ') : permissionName;
  }

  getPermissionActionType(permissionName: string): string {
    if (permissionName.includes('READ') || permissionName.includes('VIEW')) return 'read';
    if (permissionName.includes('CREATE')) return 'create';
    if (permissionName.includes('UPDATE') || permissionName.includes('EDIT')) return 'update';
    if (permissionName.includes('DELETE')) return 'delete';
    if (permissionName.includes('VALIDATE')) return 'validate';
    if (permissionName.includes('ENTRY')) return 'entry';
    return 'other';
  }

  getModuleDisplayName(module: string): string {
    const names: Record<string, string> = {
      patient: 'Pacientes',
      order: 'Órdenes',
      result: 'Resultados',
      lab_area: 'Áreas',
      exam: 'Exámenes',
      equipment: 'Equipos',
      user: 'Usuarios',
      role: 'Roles',
      permission: 'Permisos',
      report: 'Reportes',
      audit: 'Auditoría',
      config: 'Configuración',
      unit: 'Unidades',
    };
    return names[module] || module;
  }

  getModuleIcon(module: string): string {
    const icons: Record<string, string> = {
      patient: '👤',
      order: '📝',
      result: '🧪',
      lab_area: '🏢',
      exam: '🔬',
      equipment: '⚙️',
      user: '👥',
      role: '🔐',
      permission: '🔑',
      report: '📊',
      audit: '📋',
      config: '⚡',
      unit: '📏',
    };
    return icons[module] || '⚡';
  }

  showMessage(text: string, type: 'success' | 'error' | 'info' | 'warning'): void {
    this.message = text;
    this.messageType = type;

    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  toggleActiveFilter(): void {
    // La variable showActiveOnly ya se actualizó mediante ngModel
    this.loadUsers();
  }

  getStatusBadgeClass(user: UserResponse): string {
    if (user.useDirectPermissions) {
      return 'bg-purple-100 text-purple-800';
    }
    return 'bg-blue-100 text-blue-800';
  }

  getStatusText(user: UserResponse): string {
    return user.useDirectPermissions ? 'Permisos Directos' : 'Por Rol';
  }
  /**
   * Verifica si un permiso viene del rol del usuario
   */
  isPermissionFromRole(permissionId: number): boolean {
    return this.selectedUser?.rolePermissions?.some((rp) => rp.id === permissionId) || false;
  }

  /**
   * Verifica si un permiso es solo directo (no viene del rol)
   */
  isPermissionOnlyDirect(permissionId: number): boolean {
    return this.hasPermission(permissionId) && !this.isPermissionFromRole(permissionId);
  }

  /**
   * Obtiene el origen del permiso (ROL, DIRECTO, o AMBOS)
   */
  getPermissionOrigin(permissionId: number): 'ROL' | 'DIRECTO' | 'AMBOS' | 'NINGUNO' {
    const hasFromRole = this.isPermissionFromRole(permissionId);
    const hasDirectPermission = this.selectedPermissions.has(permissionId);

    if (hasFromRole && hasDirectPermission) return 'AMBOS';
    if (hasFromRole) return 'ROL';
    if (hasDirectPermission) return 'DIRECTO';
    return 'NINGUNO';
  }

  /**
   * Verifica si el checkbox debe estar deshabilitado
   * Se deshabilita si el permiso viene del rol y NO estamos en modo híbrido
   */
  isPermissionDisabled(permissionId: number): boolean {
    // Si viene del rol, el checkbox se deshabilita (solo lectura)
    return this.isPermissionFromRole(permissionId);
  }

  /**
   * Toggle de permiso modificado para no permitir desmarcar permisos de rol
   */
  togglePermission(permissionId: number): void {
    if (this.isPermissionFromRole(permissionId)) return;

    if (this.selectedPermissions.has(permissionId)) {
      this.selectedPermissions.delete(permissionId);
    } else {
      this.selectedPermissions.add(permissionId);
    }
  }

  /**
   * Toggle de todos los permisos de un módulo (modificado)
   */
  toggleAllPermissionsInModule(module: ModulePermissionsGroup, event: any): void {
    const checked = event.target.checked;

    module.permissions.forEach((permission) => {
      // Solo afectar permisos que NO vienen del rol
      if (!this.isPermissionFromRole(permission.id)) {
        if (checked) {
          this.selectedPermissions.add(permission.id);
        } else {
          this.selectedPermissions.delete(permission.id);
        }
      }
    });
  }

  /**
   * Verifica si hay permisos del rol en el módulo
   */
  hasRolePermissionsInModule(module: ModulePermissionsGroup): boolean {
    return module.permissions.some((p) => this.isPermissionFromRole(p.id));
  }

  /**
   * Cuenta cuántos permisos del módulo vienen del rol
   */
  getRolePermissionsCount(module: ModulePermissionsGroup): number {
    return module.permissions.filter((p) => this.isPermissionFromRole(p.id)).length;
  }

  /**
   * Cuenta cuántos permisos directos tiene el módulo (sin contar los del rol)
   */
  getDirectPermissionsCount(module: ModulePermissionsGroup): number {
    return module.permissions.filter(
      (p) => this.selectedPermissions.has(p.id) && !this.isPermissionFromRole(p.id)
    ).length;
  }

  private toastError(msg: string) {
    this.alertService.error('Error', msg);
  }

}
