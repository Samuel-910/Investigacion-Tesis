import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Permission } from '../../models/permission.model';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../../shared/components/reusable-search-selector/reusable-search-selector';
import { SearchableSelectComponent } from '../../../../shared/components/searchable-select/searchable-select.component';
import { PermissionService } from '../../services/permission.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';

interface PermissionGroup {
  module: string;
  displayName: string;
  icon: string;
  permissions: Permission[];
  expanded: boolean;
}

@Component({
  selector: 'app-permissions-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderComponent,
    SidebarComponent,
    BreadcrumbComponent,
    PageHeaderComponent,
    PaginationComponent,
    SearchGenericComponent,
    SearchableSelectComponent,
  ],
  templateUrl: './permissions-list.component.html',
  styleUrls: ['./permissions-list.component.css'],
})
export class PermissionsListComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Permisos del Sistema' },
  ];

  permissionGroups: PermissionGroup[] = [];
  loading = false;
  searchTerm = '';
  searchType = 'ALL';
  selectedModule: string = 'ALL';
  moduleList: string[] = [];
  moduleOptions: any[] = [{ value: 'ALL', label: '📦 Todos los módulos' }];
  
  totalPermissions = 0;
  totalModules = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;

  constructor(
    private permissionService: PermissionService,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadModules();
    this.loadPermissions();
  }

  loadModules(): void {
    this.permissionService.getAllModules().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.moduleList = response.data;
          this.totalModules = this.moduleList.length;
          
          const options = this.moduleList.map(module => ({
            value: module,
            label: `${this.getModuleIcon(module)} ${this.getModuleDisplayName(module)}`
          }));
          this.moduleOptions = [{ value: 'ALL', label: '📦 Todos los módulos' }, ...options];
          
          this.cdRef.detectChanges();
        }
      }
    });
  }

  onModuleChange(): void {
    this.currentPage = 0;
    this.loadPermissions();
  }

  onModuleSelected(event: any): void {
    if (event) {
      this.selectedModule = event.value;
    } else {
      this.selectedModule = 'ALL';
    }
    this.currentPage = 0;
    this.loadPermissions();
  }

  handleSearch(event: { q: string; type: string }): void {
    this.searchTerm = event.q;
    this.searchType = event.type;
    this.currentPage = 0;
    this.loadPermissions();
  }

  loadPermissions(): void {
    this.loading = true;
    let permissionsRequest$;

    if (this.searchTerm && this.searchTerm.trim().length > 0) {
      permissionsRequest$ = this.permissionService.searchByFilter(
        this.searchTerm,
        this.searchType,
        this.currentPage,
        this.pageSize
      );
    } else if (this.selectedModule && this.selectedModule !== 'ALL') {
      permissionsRequest$ = this.permissionService.getPermissionsByModule(
        this.selectedModule,
        this.currentPage,
        this.pageSize
      );
    } else {
      permissionsRequest$ = this.permissionService.getAllPermissions(
        this.currentPage,
        this.pageSize
      );
    }

    permissionsRequest$.subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.groupPermissions(response.data.content);
          this.totalPermissions = response.data.totalElements;
          this.totalPages = response.data.totalPages;
          this.currentPage = response.data.number;
        }
        this.cdRef.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdRef.detectChanges();
      },
    });
  }

  groupPermissions(permissions: Permission[]): void {
    const grouped = new Map<string, Permission[]>();

    permissions.forEach((permission) => {
      const moduleName = permission.module || 'general';
      if (!grouped.has(moduleName)) {
        grouped.set(moduleName, []);
      }
      grouped.get(moduleName)!.push(permission);
    });

    this.permissionGroups = Array.from(grouped.entries())
      .map(([module, perms]) => ({
        module,
        displayName: this.getModuleDisplayName(module),
        icon: this.getModuleIcon(module),
        permissions: perms.sort((a, b) => a.name.localeCompare(b.name)),
        expanded: true,
      }))
      .sort((a, b) => a.displayName.localeCompare(b.displayName));
  }

  toggleModule(group: PermissionGroup): void {
    group.expanded = !group.expanded;
  }

  expandAll(): void {
    this.permissionGroups.forEach((g) => (g.expanded = true));
  }

  collapseAll(): void {
    this.permissionGroups.forEach((g) => (g.expanded = false));
  }

  get filteredGroups(): PermissionGroup[] {
    return this.permissionGroups;
  }

  getPermissionActionLabel(permissionName: string): string {
    const type = this.getPermissionActionType(permissionName);
    const labels: Record<string, string> = {
      read: 'Lectura',
      create: 'Creación',
      update: 'Edición',
      delete: 'Eliminación',
      validate: 'Validación',
      entry: 'Ingreso',
      other: 'Operación'
    };
    return labels[type] || 'Operación';
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

  getActionColor(type: string): string {
    const colors: Record<string, string> = {
      read: 'bg-blue-500 text-white',
      create: 'bg-emerald-500 text-white',
      update: 'bg-yellow-500 text-white',
      delete: 'bg-red-500 text-white',
      validate: 'bg-purple-500 text-white',
      entry: 'bg-indigo-500 text-white',
      other: 'bg-slate-500 text-white',
    };
    return colors[type] || colors['other'];
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
    return icons[module] || '📦';
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadPermissions();
  }

  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0;
    this.loadPermissions();
  }
}