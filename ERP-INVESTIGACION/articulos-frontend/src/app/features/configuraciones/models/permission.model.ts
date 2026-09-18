// ========================================
// PERMISOS (PERMISSIONS)
// ========================================

export interface PermissionRequest {
    name: string;
    description: string;
    module: string;
    active?: boolean;
}

export interface PermissionResponse {
    id: number;
    name: string;
    description: string;
    module: string;
    active: boolean;
    createdAt?: string | Date;
}

export type Permission = PermissionResponse;
