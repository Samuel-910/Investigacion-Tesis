import { Plantilla } from './documento.model';

export interface PuntoDocumento {
    id: number;
    punto: {
        punto: number;
        nombre: string;
    };
    tipoDoc: string;
    serie: string;
    numero: number;
}

export interface TipoDocumento {
    tipoDoc: string;
    nombre: string;
}

export interface PlantillaAsignacion {
    id?: number;
    modulo: 'VENTAS' | 'COMPRAS' | 'CAJA';
    plantilla: Plantilla;
    puntoDocumento?: PuntoDocumento;
    tipoDocumento?: TipoDocumento;
    activo: boolean;
    descripcion?: string;
}
