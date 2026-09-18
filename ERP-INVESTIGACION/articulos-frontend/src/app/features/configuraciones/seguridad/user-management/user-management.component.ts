import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../shared/components/forms/form-select/form-select.component';
import { AlertService } from '../../../../core/services/alert.service';
import { CreateUserRequest, User } from '../../models/user.model';
import { Role } from '../../models/role.model';
import { UserService } from '../../services/user.service';
import { RoleService } from '../../services/role.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { SucursalService } from '../../../../core/services/sucursal.service';
import { PuntoService } from '../../services/punto.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-management',
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
    FormSelectComponent,
  ],
  templateUrl: './user-management.component.html'
})
export class UserManagementComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Gestión de Usuarios' },
  ];

  users: User[] = [];
  availableRoles: Role[] = [];
  availableSucursales: any[] = [];
  availablePuntos: any[] = [];
  allSystemUsers: User[] = [];
  selectedSystemUserId: number | null = null;

  get systemUserOptions() {
    return this.allSystemUsers.map(user => ({
      label: `${user.firstName} ${user.lastName} (${user.username})`,
      value: user.id
    }));
  }

  showModal = false;
  isEditMode = false;
  selectedUser: User | null = null;

  formData: CreateUserRequest = this.getEmptyForm();
  confirmPassword = '';
  
  loading = false;
  saving = false;

  searchQuery = '';
  searchType = 'ALL';
  showActiveOnly = true;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  message = '';
  messageType: 'success' | 'error' | 'info' | 'warning' = 'info';

  showDeleteConfirm = false;
  userToDelete: User | null = null;

  selectedRoleIds: Set<number> = new Set();
  selectedSucursalIds: Set<number> = new Set();
  originalSucursalIds: Set<number> = new Set();
  selectedPuntoIds: Set<number> = new Set();
  originalPuntoIds: Set<number> = new Set();

  constructor(
    private userService: UserService,
    private roleService: RoleService,
    private cdRef: ChangeDetectorRef,
    public sidebarService: SidebarService,
    private sucursalService: SucursalService,
    private puntoService: PuntoService,
    private alertService: AlertService
  ) { }

  ngOnInit(): void {
    this.executeSearch();
    this.loadRoles();
    this.loadAvailableSucursales();
    this.loadAvailablePuntos();
  }

  handleSearch(event: { q: string; type: string }) {
    this.searchQuery = event.q;
    this.searchType = event.type;
    this.currentPage = 0;
    this.executeSearch();
  }

  executeSearch(): void {
    this.loading = true;
    this.cdRef.detectChanges();

    let request;
    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      request = this.userService.searchByFilter(this.searchQuery, this.searchType, this.currentPage, this.pageSize);
    } else {
      request = this.userService.getUsersByModulo('Articulos', this.currentPage, this.pageSize);
    }

    request.pipe(finalize(() => {
          this.loading = false;
          this.cdRef.detectChanges();
        })
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.users = response.data.content;
            this.totalPages = response.data.totalPages;
            this.totalElements = response.data.totalElements;
            this.currentPage = response.data.number;
          } else {
            this.showErrorAlert(response.message || 'No se pudo cargar la lista de usuarios');
          }
        },
        error: (error) => {
          this.showErrorAlert(error.error?.message || 'Error de conexión al obtener usuarios');
        },
      });
  }

  loadRoles(): void {
    this.roleService.getActiveRoles(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data?.content) {
          this.availableRoles = response.data.content;
          this.cdRef.detectChanges();
        }
      },
      error: (error) => {
        this.showErrorAlert(error.error?.message || 'Error al cargar roles');
      },
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedUser = null;
    this.selectedSystemUserId = null;
    this.formData = this.getEmptyForm();
    this.selectedRoleIds.clear();
    this.selectedSucursalIds.clear();
    this.originalSucursalIds.clear();
    this.selectedPuntoIds.clear();
    this.originalPuntoIds.clear();
    this.confirmPassword = '';
    
    this.userService.getActiveUsers(0, 10).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const existingUserIds = new Set(this.users.map(u => u.id));
          this.allSystemUsers = res.data.content.filter((u: any) => !existingUserIds.has(u.id));
        }
      }
    });
    
    this.showModal = true;
  }

  openEditModal(user: User): void {
    this.isEditMode = true;
    this.selectedUser = user;
    this.formData = {
      username: user.username,
      email: user.email,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      roleIds: [],
      active: user.active,
    };
    this.selectedRoleIds.clear();
    user.roles.forEach((roleName: string) => {
      const roleObj = this.availableRoles.find((r) => r.name === roleName);
      if (roleObj) this.selectedRoleIds.add(roleObj.id);
    });
    this.confirmPassword = '';
    this.selectedSucursalIds.clear();
    this.originalSucursalIds.clear();
    this.selectedPuntoIds.clear();
    this.originalPuntoIds.clear();
    this.loadUserSucursales(user.id);
    this.loadUserPuntos(user.id);
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedUser = null;
    this.formData = this.getEmptyForm();
  }

  saveUser(userForm: any) {
    if (this.isEditMode) {
      if (userForm.invalid) {
        this.alertService.error('Error', 'Error de validación en los campos');
        return;
      }
      this.alertService.loading('Procesando...', 'Actualizando usuario');
      this.saving = true;
      this.formData.roleIds = Array.from(this.selectedRoleIds);
      const userId = this.selectedUser!.id;
      
      this.userService.updateUser(userId, this.formData).subscribe({
        next: (res) => {
          if (res.success) {
            const addedSucursales = Array.from(this.selectedSucursalIds).filter((id) => !this.originalSucursalIds.has(id));
            const removedSucursales = Array.from(this.originalSucursalIds).filter((id) => !this.selectedSucursalIds.has(id));
            
            let sucursalesObs;
            if (addedSucursales.length > 0 || removedSucursales.length > 0) {
              const obs = [];
              if (addedSucursales.length > 0) obs.push(this.userService.assignSucursales(userId, addedSucursales));
              if (removedSucursales.length > 0) obs.push(this.userService.removeSucursales(userId, removedSucursales));
              sucursalesObs = forkJoin(obs);
            }
            
            const addedPuntos = Array.from(this.selectedPuntoIds).filter((id) => !this.originalPuntoIds.has(id));
            const removedPuntos = Array.from(this.originalPuntoIds).filter((id) => !this.selectedPuntoIds.has(id));

            let puntosObs;
            if (addedPuntos.length > 0 || removedPuntos.length > 0) {
              const obs = [];
              if (addedPuntos.length > 0) obs.push(this.userService.assignPuntos(userId, addedPuntos));
              if (removedPuntos.length > 0) obs.push(this.userService.removePuntos(userId, removedPuntos));
              puntosObs = forkJoin(obs);
            }

            if (sucursalesObs || puntosObs) {
              const allObs = [];
              if (sucursalesObs) allObs.push(sucursalesObs);
              if (puntosObs) allObs.push(puntosObs);
              
              forkJoin(allObs).subscribe({
                next: () => {
                  this.saving = false;
                  this.alertService.success('Éxito', 'Asignaciones actualizadas correctamente');
                  this.closeModal();
                  this.executeSearch();
                },
                error: (error) => {
                  this.saving = false;
                  this.showErrorAlert(error.error?.message || 'Error al actualizar asignaciones');
                },
              });
            } else {
              this.saving = false;
              this.alertService.success('¡Logrado!', res.message || 'Actualizado correctamente');
              this.closeModal();
              this.executeSearch();
            }
          } else {
            this.saving = false;
            this.showWarningAlert(res.message || 'No se pudo completar la operación');
          }
        },
        error: (err) => {
          this.saving = false;
          this.showErrorAlert(err.error?.message || 'Error inesperado en el servidor');
        },
      });
    } else {
      if (!this.selectedSystemUserId) {
        this.alertService.error('Error', 'Debe seleccionar un usuario');
        return;
      }

      this.alertService.loading('Procesando...', 'Vinculando usuario al módulo');
      this.saving = true;
      
      const operations: any[] = [
        this.userService.assignModulosByName(this.selectedSystemUserId, ['Articulos'])
      ];

      const roles = Array.from(this.selectedRoleIds);
      if (roles.length > 0) {
        operations.push(this.userService.assignRoles(this.selectedSystemUserId, roles));
      }

      forkJoin(operations).subscribe({
        next: () => {
          this.saving = false;
          this.alertService.success('¡Logrado!', 'Usuario vinculado correctamente al módulo');
          this.closeModal();
          this.executeSearch();
        },
        error: (err) => {
          this.saving = false;
          this.showErrorAlert(err.error?.message || 'Error inesperado al vincular usuario');
        }
      });
    }
  }

  openDeleteConfirm(user: User): void {
    this.userToDelete = user;
    this.showDeleteConfirm = true;
  }

  confirmDelete(): void {
    if (!this.userToDelete) return;

    this.alertService.confirm(
      '¿Estás seguro?',
      `¿Deseas eliminar al usuario "${this.userToDelete.username}"?`,
      'Sí, eliminar',
      'Cancelar'
    ).then((result) => {
      if (result.isConfirmed) {
        this.userService.deleteUser(this.userToDelete!.id).subscribe({
          next: (response) => {
            if (response.success) {
              this.executeSearch();
              this.alertService.success('Eliminado', 'El usuario ha sido eliminado');
            }
            this.showDeleteConfirm = false;
          },
          error: (error) => { this.showErrorAlert(error.error?.message || 'Error al eliminar'); }
        });
      }
    });
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.userToDelete = null;
  }

  toggleRole(roleId: number) {
    this.selectedRoleIds.has(roleId) ? this.selectedRoleIds.delete(roleId) : this.selectedRoleIds.add(roleId);
  }

  loadAvailableSucursales(): void {
    this.sucursalService.getAll().subscribe({
      next: (response: any) => {
        if (response.success && response.data?.content) {
          this.availableSucursales = response.data.content;
        }
      }
    });
  }

  loadAvailablePuntos(): void {
    this.puntoService.buscarTodos().subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.availablePuntos = response.data;
        }
      }
    });
  }

  loadUserSucursales(userId: number): void {
    this.userService.getSucursalesAsignadas(userId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.selectedSucursalIds = new Set(response.data.map((s: any) => s.idSucursal));
          this.originalSucursalIds = new Set(this.selectedSucursalIds);
          this.cdRef.detectChanges();
        }
      }
    });
  }

  loadUserPuntos(userId: number): void {
    this.userService.getPuntosAsignados(userId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.selectedPuntoIds = new Set(response.data.map((p: any) => p.punto));
          this.originalPuntoIds = new Set(this.selectedPuntoIds);
          this.cdRef.detectChanges();
        }
      }
    });
  }

  get filteredPuntos() {
    if (this.selectedSucursalIds.size === 0) return [];
    return this.availablePuntos.filter(p => this.selectedSucursalIds.has(p.idSucursal));
  }

  toggleSucursal(sucursalId: number): void {
    if (this.selectedSucursalIds.has(sucursalId)) {
      // Si ya está seleccionada, la desmarcamos
      this.selectedSucursalIds.clear();
      this.selectedPuntoIds.clear();
    } else {
      // Si es una nueva sucursal, limpiamos todo y seleccionamos la nueva
      this.selectedSucursalIds.clear();
      this.selectedPuntoIds.clear();
      this.selectedSucursalIds.add(sucursalId);
    }
  }

  togglePunto(puntoId: number): void {
    this.selectedPuntoIds.has(puntoId) ? this.selectedPuntoIds.delete(puntoId) : this.selectedPuntoIds.add(puntoId);
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.executeSearch();
  }

  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0;
    this.executeSearch();
  }

  getEmptyForm(): CreateUserRequest {
    return { username: '', email: '', password: '', firstName: '', lastName: '', roleIds: [], active: true };
  }

  showMessage(text: string, type: 'success' | 'error' | 'info' | 'warning'): void {
    this.message = text;
    this.messageType = type;
    setTimeout(() => { this.message = ''; }, 5000);
  }

  private showErrorAlert(text: string) {
    this.alertService.error('Error', text);
  }

  private showWarningAlert(text: string) {
    this.alertService.warning('Atención', text);
  }
}