import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { AlertService } from '../../../../core/services/alert.service';
import { Role, RoleRequest } from '../../models/role.model';
import { Permission } from '../../models/permission.model';
import { RoleService } from '../../services/role.service';
import { PermissionService } from '../../services/permission.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderComponent,
    SidebarComponent,
    BreadcrumbComponent,
    PageHeaderComponent,
    ModalComponent,
    PaginationComponent,
    SearchGenericComponent,
    FormInputComponent,
  ],
  templateUrl: './role-management.component.html'
})
export class RoleManagementComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Gestión de Roles' },
  ];

  roles: Role[] = [];
  permissions: Permission[] = [];

  showModal = false;
  isEditMode = false;
  selectedRole: Role | null = null;
  formData: RoleRequest = this.getEmptyForm();
  selectedPermissions: Set<number> = new Set();
  loading = false;
  saving = false;

  searchQuery = '';
  searchType = 'ALL';
  showActiveOnly = true;
  currentRolePage = 0;
  rolePageSize = 10;
  totalRoleElements = 0;
  totalRolePages = 0;

  searchPermissionQuery = '';
  searchPermissionType = 'ALL';
  currentPermissionPage = 0;
  permissionPageSize = 10;
  totalPermissionElements = 0;
  totalPermissionPages = 0;

  constructor(
    private roleService: RoleService,
    private permissionService: PermissionService,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef,
    private alertService: AlertService
  ) { }

  ngOnInit(): void {
    this.executeSearch(0, this.rolePageSize);
  }

  handleSearch(event: { q: string; type: string }) {
    this.searchQuery = event.q;
    this.searchType = event.type;
    this.currentRolePage = 0;
    this.executeSearch(0, this.rolePageSize);
  }

  executeSearch(page: number = 0, size: number = 10): void {
    this.loading = true;
    let request;

    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      request = this.roleService.searchByFilter(this.searchQuery, this.searchType, page, size);
    } else {
      request = this.showActiveOnly
        ? this.roleService.getActiveRoles(page, size)
        : this.roleService.getAllRoles(page, size);
    }

    request.subscribe({
      next: (response) => {

          if (response.success && response.data) {
            this.roles = response.data.content;
            this.totalRoleElements = response.data.totalElements;
            this.totalRolePages = response.data.totalPages;
            this.currentRolePage = response.data.number;
          } else {
            this.showSwalAlert('warning', 'Atención', response.message || 'No se pudieron cargar los roles');
          }
          this.loading = false;
          this.cdRef.detectChanges();
        ;
      },
      error: (error) => {
        this.loading = false;
        this.cdRef.detectChanges();
        this.showErrorAlertWithRetry('Error al cargar roles', error, () => this.executeSearch(page, size));
      },
    });
  }

  handlePermissionSearch(event: { q: string; type: string }) {
    this.searchPermissionQuery = event.q;
    this.searchPermissionType = event.type;
    this.currentPermissionPage = 0;
    this.executePermissionSearch(0, this.permissionPageSize);
  }

  executePermissionSearch(page: number = 0, size: number = 10): void {
    let request;
    if (this.searchPermissionQuery && this.searchPermissionQuery.trim().length > 0) {
      request = this.permissionService.searchByFilter(this.searchPermissionQuery, this.searchPermissionType, page, size);
    } else {
      request = this.permissionService.getAllPermissions(page, size, 'name');
    }

    request.subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.permissions = response.data.content;
          this.totalPermissionElements = response.data.totalElements;
          this.totalPermissionPages = response.data.totalPages;
          this.currentPermissionPage = response.data.number;
          this.cdRef.detectChanges();
        }
      },
      error: (error) => {
        this.showErrorAlertWithRetry('Error al cargar permisos', error, () => this.executePermissionSearch(page, size));
      },
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.formData = this.getEmptyForm();
    this.selectedPermissions.clear();
    this.showModal = true;
    this.executePermissionSearch(0, this.permissionPageSize);
  }

  openEditModal(role: Role): void {
    this.isEditMode = true;
    this.selectedRole = role;
    this.formData = {
      name: role.name,
      description: role.description,
      permissionIds: role.permissions.map((p) => p.id),
      active: role.active,
    };
    this.selectedPermissions = new Set(role.permissions.map((p) => p.id));
    this.showModal = true;
    this.executePermissionSearch(0, this.permissionPageSize);
  }

  closeModal(): void {
    this.showModal = false;
    this.formData = this.getEmptyForm();
    this.selectedPermissions.clear();
    this.permissions = [];
  }

  saveRole(roleForm: any): void {
    if (roleForm.invalid) {
      this.alertService.error('Error', 'Error de validación en los campos');
      return;
    }

    if (!this.formData.name.trim()) {
      this.showSwalAlert('warning', 'Atención', 'El nombre del rol es obligatorio');
      return;
    }

    if (this.selectedPermissions.size === 0) {
      this.showSwalAlert('warning', 'Atención', 'Debe seleccionar al menos un permiso');
      return;
    }

    this.formData.permissionIds = Array.from(this.selectedPermissions);
    this.saving = true;

    this.alertService.loading('Guardando...', 'Procesando la solicitud');

    const request = this.isEditMode && this.selectedRole
      ? this.roleService.updateRole(this.selectedRole.id, this.formData)
      : this.roleService.createRole(this.formData);

    request.subscribe({
      next: (response) => {
        this.saving = false;
        if (response.success) {
          this.closeModal();
          this.executeSearch(this.currentRolePage, this.rolePageSize);
          this.showSwalAlert('success', '¡Éxito!', response.message || 'Operación exitosa');
        } else {
          this.showSwalAlert('error', 'Error', response.message || 'No se pudo guardar');
        }
      },
      error: (error) => {
        this.saving = false;
        this.showSwalAlert('error', 'Error al guardar', error.error?.message || 'Error de conexión');
      },
    });
  }

  openDeleteConfirm(role: Role): void {
    this.alertService.confirm(
      '¿Estás seguro?',
      `Vas a eliminar el rol: ${role.name}`,
      'Sí, eliminar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) this.confirmDelete(role.id);
    });
  }

  confirmDelete(roleId: number): void {
    this.saving = true;
    this.alertService.loading('Eliminando...', 'Procesando solicitud');

    this.roleService.deleteRole(roleId).subscribe({
      next: (response) => {
        this.saving = false;
        if (response.success) {
          this.showSwalAlert('success', '¡Éxito!', response.message || 'Rol eliminado');
          this.executeSearch(this.currentRolePage, this.rolePageSize);
        } else {
          this.showSwalAlert('error', 'Error', response.message || 'No se pudo eliminar');
        }
      },
      error: (error) => {
        this.saving = false;
        this.showSwalAlert('error', 'Error', error.error?.message || 'No se pudo eliminar');
      },
    });
  }

  togglePermission(permissionId: number): void {
    this.selectedPermissions.has(permissionId) 
      ? this.selectedPermissions.delete(permissionId) 
      : this.selectedPermissions.add(permissionId);
  }

  hasPermission(permissionId: number): boolean {
    return this.selectedPermissions.has(permissionId);
  }

  getEmptyForm(): RoleRequest {
    return { name: '', description: '', permissionIds: [], active: true };
  }

  getPermissionShortName(permissionName: string): string {
    return permissionName.replace(/_/g, ' ');
  }

  getPermissionActionType(permissionName: string): string {
    const name = permissionName.toUpperCase();
    if (name.includes('READ') || name.includes('VIEW') || name.includes('LEER')) return 'read';
    if (name.includes('CREATE') || name.includes('CREAR')) return 'create';
    if (name.includes('UPDATE') || name.includes('EDIT') || name.includes('ACTUALIZAR')) return 'update';
    if (name.includes('DELETE') || name.includes('ELIMINAR')) return 'delete';
    if (name.includes('BLOQUEAR')) return 'block';
    return 'other';
  }

  onRolePageChange(page: number): void {
    this.currentRolePage = page;
    this.executeSearch(page, this.rolePageSize);
  }

  onRolePageSizeChange(size: number): void {
    this.rolePageSize = size;
    this.currentRolePage = 0;
    this.executeSearch(0, size);
  }

  onPermissionPageChange(page: number): void {
    this.currentPermissionPage = page;
    this.executePermissionSearch(page, this.permissionPageSize);
  }

  onPermissionPageSizeChange(size: number): void {
    this.permissionPageSize = size;
    this.currentPermissionPage = 0;
    this.executePermissionSearch(0, size);
  }

  private showSwalAlert(icon: any, title: string, text: string) {
    if (icon === 'success') this.alertService.success(title, text);
    else if (icon === 'warning') this.alertService.warning(title, text);
    else if (icon === 'error') this.alertService.error(title, text);
    else if (icon === 'info') this.alertService.info(title, text);
  }

  private showErrorAlertWithRetry(title: string, error: any, retryFn: Function) {
    this.alertService.confirm(
      title,
      error.error?.message || 'No se pudo conectar con el servidor',
      'Reintentar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) retryFn();
    });
  }
}