export type Modulo = 'VENTA' | 'COMPRA' | 'INVENTARIO' | 'DIVERSOS' | 'INGRESOS_DIVERSOS' | 'SALIDAS_DIVERSAS' | 'COTIZACION' | 'NOTA_VENTA';
export type TipoDocumentoPlantilla = 'FACTURA' | 'BOLETA' | 'NOTA_CREDITO' | 'GUIA_REMISION' | 'RECIBO' | 'PRODUCTO' | 'DIVERSOS' | 'INGRESOS_DIVERSOS' | 'SALIDAS_DIVERSAS' | 'CT' | 'NV';

export interface Bloque {
    id?: number;
    nombre: string;
    htmlContenido: string;
    cssEstilo: string;
    categoria: string;
    imagenUrl?: string;
    modulo?: Modulo; // Mantener por compatibilidad si es necesario, pero preferir modulos
    modulos?: Modulo[];
    tipoDocumento?: TipoDocumentoPlantilla;
}

export interface DocumentoFormato {
    id: number;
    nombre: string;
    anchoPx: number;
    altoPx?: number;
    descripcion?: string;
}

export interface TipoDocumentoBase {
    tipoDoc: string;
    nombre: string;
}

export interface Plantilla {
    id?: number;
    nombre: string;
    htmlContenido: string;
    htmlTraducido?: string;
    cssEstilo: string;
    formato?: DocumentoFormato;
    formatoId?: number; // Para el envío al backend
    orientacion: string;
    modulo: Modulo;
    tipoDocumento: TipoDocumentoPlantilla | TipoDocumentoBase;
    isDefault: boolean;
}

export type FormatoHoja = 'A4' | 'A5' | 'Letter';
export type Orientacion = 'Vertical' | 'Horizontal';
