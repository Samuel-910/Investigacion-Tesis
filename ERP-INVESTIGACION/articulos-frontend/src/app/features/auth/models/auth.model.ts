export interface LoginRequest {
    username: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    message: string;
    role: string;
}

export interface Usuario {
    id: string;
    nombre: string;
    usuario: string;
    rol: string;
    sucursalActual?: Sucursal;
}

export interface Sucursal {
    id: string;
    nombre: string;
    codigo: string;
}

export interface CambioSucursalRequest {
    usuario: string;
    contrasena: string;
    sucursalId: string;
}
