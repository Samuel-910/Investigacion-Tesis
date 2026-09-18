import { Permission } from "./permission.model";

export interface UserDirectPermissionResponse {
    id: number;
    permission: Permission;
    grantedBy: number;
    grantedByLogin: string;
    grantedByFullName: string;
    grantedAt: string | Date;
    reason: string;
    expiresAt?: string | Date;
    active: boolean;
    isExpired: boolean;
    daysUntilExpiration?: number;
}

export interface UserWithPermissionsResponse {
    id: number;
    login: string;
    nombre: string;
    apepat: string;
    apemat: string;
    fullName: string;
    email: string;
    active: boolean;
    roles: string[];
    rolePermissions: Permission[];
    useDirectPermissions: boolean;
    permissionMode: 'ROL' | 'DIRECTO';
    directPermissions: UserDirectPermissionResponse[];
    activeDirectPermissionsCount: number;
    effectivePermissions: Permission[];
    effectivePermissionsCount: number;
    createdAt: string | Date;
    lastLogin?: string | Date;
}

export interface AssignDirectPermissionsRequest {
    userId: number;
    permissionIds: number[];
    reason?: string;
    expiresAt?: string | Date;
}

export interface GrantDirectPermissionRequest {
    userId: number;
    permissionId: number;
    reason?: string;
    expiresAt?: string | Date;
}
export interface TogglePermissionModeRequest {
    userId: number;
    useDirectPermissions: boolean;
}