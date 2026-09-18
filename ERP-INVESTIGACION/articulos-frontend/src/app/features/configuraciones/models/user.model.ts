import { Permission } from './permission.model';
import { UserDirectPermissionResponse } from './user-permission.model';


export interface UserResponse {
    id: number;
    idPersonal: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    phone: string;
    active: boolean;
    roles: string[];
    permissions: string[];
    createdAt: string | Date;
    updatedAt: string | Date;
    lastLogin?: string | Date;
    roleId?: number;
    roleName?: string;
    useDirectPermissions: boolean;
    rolePermissions: Permission[];
    directPermissions: UserDirectPermissionResponse[];
    effectivePermissions: Permission[];
}
export type User = UserResponse;

export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    roleIds: number[];
    active: boolean;
}

export interface UpdateUserRequest {
    username: string;
    email: string;
    password?: string;
    firstName: string;
    lastName: string;
    phone?: string;
    roleIds: number[];
    active: boolean;
}

export interface UpdateMyProfileRequest {
    firstName: string;
    lastName: string;
    apemat?: string;
    email: string;
    phone?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

