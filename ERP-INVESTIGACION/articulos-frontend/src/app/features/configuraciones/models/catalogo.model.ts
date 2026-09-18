import { EstadoGeneral } from '../../../core/interfaces/estado-general.interface';

export interface CatalogoRequest {
    codigo?: string;
    codigoBarra?: string;
    codigoSeus?: string;
    nombre: string;
    detalle?: string;
    tipo: string;
    tipoMedicamento?: string;
    idPrincipioActivo?: number;
    idAccionTerapeutica?: number;
    regSanitario?: string;
    codDigemid?: string;
    esGenerico?: boolean;
    esControlado?: boolean;
    ventaConReceta?: boolean;
    manejaLotes?: boolean;
    idCategoria?: number;
    idLaboratorio?: number;
    idNivel: number;
    presentacion?: string;
    tiempoEntregaMinutos?: number;
    // Unidades
    manejaUnidad?: boolean;
    manejaBlister?: boolean;
    factorBlister?: number;
    manejaCaja?: boolean;
    factorCaja?: number;
    tipoAfectacion?: string;
    foto?: string;
    estado?: boolean | string | EstadoGeneral;
    precioKairos?: number;
    usuarioCreacion?: string;
}

export interface CatalogoResponse {
    id: number;
    codigo?: string;
    codigoBarra?: string;
    codigoSeus?: string;
    nombre: string;
    detalle?: string;
    tipo: string;
    tipoMedicamento?: string;
    principioActivo?: string;
    idPrincipioActivo?: number;
    accionTerapeutica?: string;
    idAccionTerapeutica?: number;
    regSanitario?: string;
    codDigemid?: string;
    esGenerico?: boolean;
    esControlado?: boolean;
    ventaConReceta?: boolean;
    manejaLotes?: boolean;
    idCategoria?: number;
    categoria?: string;
    idLaboratorio?: number;
    laboratorio?: string;
    idNivel: number;
    nivel?: string;
    presentacion?: string;
    tiempoEntregaMinutos?: number;
    // Unidades
    manejaUnidad?: boolean;
    manejaBlister?: boolean;
    factorBlister?: number;
    idUnidadIntermedia?: number;
    unidadIntermedia?: string;
    manejaCaja?: boolean;
    factorCaja?: number;
    idUnidadMayor?: number;
    unidadMayor?: string;
    idUnidadBase?: number;
    unidadBase?: string;
    tipoAfectacion?: string;
    foto?: string;
    estado?: boolean | string | EstadoGeneral;
    precioKairos?: number;
    usuarioCreacion?: string;
    createdAt?: string;
    updatedAt?: string;
}