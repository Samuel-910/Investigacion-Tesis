export interface VentaBeneficio {
    idVentaBeneficio?: number;
    parentesco: string;
    nombrePaciente: string;
    idVenta?: number;
    idDescuentoDetalle: number;
    nombreBeneficio: string;
}

export interface VentaRegistro {
    idVenta?: number;
    td: string;
    serie: string;
    correlativo: number;
    totalPrecioServicio: number;
    igvPrecioServicio: number;
    valorVentaSinIgv: number;
    totalPrecioSinIgv: number;
    totalPrecioFinal: number;
    estado: 'VENTA' | 'COTIZACION';
    idPaciente?: number;
    tipoVenta: 'PARTICULAR' | 'ESPECIAL';
    detalles: VentaRegistroDetalle[];
    beneficios: VentaBeneficio[];
}

export interface VentaRegistroDetalle {
    idCatalogo: number;
    cantidad: number;
    unidadMedida: string;
    precioUnitario: number;
    subtotal: number;
}
