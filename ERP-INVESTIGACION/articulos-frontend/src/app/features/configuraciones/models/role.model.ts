import { Permission, PermissionResponse } from "./permission.model";


export type Role = RoleResponse;

export interface RoleRequest {
    name: string;
    description?: string;
    permissionIds?: number[];
    active: boolean;
}
export interface RoleResponse {
    id: number;
    name: string;
    description: string;
    active: boolean;
    userCount: number;
    permissions: PermissionResponse[];
    createdAt: string | Date;
    updatedAt: string | Date;
}
// Para la matriz de permisos agrupada por módulo
export interface ModulePermissions {
    module: string;
    permissions: {
        [key: string]: Permission; // 'READ', 'CREATE', 'UPDATE', 'DELETE'
    };
}

// Para el estado del checkbox de permisos
export interface PermissionCheckState {
    permissionId: number;
    checked: boolean;
}