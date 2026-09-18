export interface MovimientoLocal {
    idInterno: number;
    tipo: 'INGRESO' | 'SALIDA';
    idCatalogo: number;
    nombreProducto: string;
    cantidad: number;
    costoUnitario: number;
    nroLote: string;
    fechaVenc?: string;
    idClasificacion: number;
    nombreClasificacion?: string;
    idAlmacen: number;
    idProducto?: number;
    observacion?: string;
}