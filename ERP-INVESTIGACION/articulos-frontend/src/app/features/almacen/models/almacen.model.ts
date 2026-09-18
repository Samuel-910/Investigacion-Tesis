export interface Almacen {
    id: number;
    nombre: string;
    codigo?: string;
    ubicacion?: string;
    esPrincipal?: boolean;
    responsable?: string;
    estado: boolean;
    idSucursal: number;
    nombreSucursal?: string;
    createdAt?: string; // ISO date string
    updatedAt?: string; // ISO date string
}

export interface AlmacenRequest {
    nombre: string;
    codigo?: string;
    ubicacion?: string;
    esPrincipal?: boolean;
    responsable?: string;
    estado: boolean;
    idSucursal: number;
}
