export interface Punto {
    punto: number;
    nombre: string;
    idSucursal: number;
    nombreSucursal?: string;
    tipo: string;
    tippro: string;
    valido: string;
    idAlmacen?: number;
    nombreAlmacen?: string;
    ipAccesoModulo?: string;
    fecini?: string;
    autorizado?: string;
    cobra?: string;
}

export interface PuntoRequest {
    nombre: string;
    idSucursal: number;
    tipo: string;
    tippro: string;
    valido: string;
    idAlmacen?: number;
    ipAccesoModulo?: string;
}
