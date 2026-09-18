import { Routes } from '@angular/router';
import { UserPermissionManagementComponent } from './user-with-permissions/user-with-permissions.component';
import { RoleManagementComponent } from './role-management/role-management.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { PermissionsListComponent } from './permissions-list/permissions-list.component';
import { CambioClaveComponent } from './cambio-clave/cambio-clave.component';

export const SEGURIDAD_ROUTES: Routes = [

    { path: 'roles', component: RoleManagementComponent },
    { path: 'users', component: UserManagementComponent },
    { path: 'permisos', component: PermissionsListComponent },
    { path: 'admin', component: UserPermissionManagementComponent },
    { path: 'cambio-clave', component: CambioClaveComponent },
];