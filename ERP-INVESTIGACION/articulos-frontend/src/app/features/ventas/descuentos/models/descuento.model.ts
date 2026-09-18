export enum TipoDescuento {
    PORCENTAJE = 'PORCENTAJE',
    MONTO_FIJO = 'MONTO_FIJO',
    CANTIDAD = 'CANTIDAD'
}

export enum TipoAlcance {
    GLOBAL = 'GLOBAL',
    POR_PRODUCTO = 'POR_PRODUCTO',
    POR_CANTIDAD = 'POR_CANTIDAD'
}

export interface DescuentoDetalle {
    id?: number;
    idProducto?: number;
    nombreProducto?: string;
    tipoDescuento: string;
    valorDescuento: number;
    cantidad: number;
}

export interface Descuento {
    id?: number;
    nombre: string;
    descripcion?: string;
    idCompania?: number;
    nombreCompania?: string;
    usuariosAfectados?: string;
    tipoAlcance: string;
    fechaInicio: string;
    fechaFin: string;
    activo: boolean;
    detalles: DescuentoDetalle[];
    createdAt?: string;
    updatedAt?: string;
}

export interface DescuentoRequest {
    id?: number;
    nombre: string;
    descripcion?: string;
    idCompania?: number;
    usuariosAfectados?: string;
    tipoAlcance: string;
    fechaInicio: string;
    fechaFin: string;
    activo: boolean;
    detalles: DescuentoDetalle[];
}
