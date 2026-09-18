import { ProveedorResponse } from "./proveedor.model";
import { CatalogoResponse } from "../../configuraciones/models/catalogo.model";

export interface CronogramaPago {
    idCronograma: number;
    numeroCuota: number;
    fechaVencimiento: string;
    montoCuota: number;
    estado: any; // Puede venir como objeto o número según el backend
    fechaPagoReal?: string;
    comprobantePago?: string;
}

export interface CompraRequest {
    idProveedor: number;
    tipoComprobante: string;
    serie: string;
    correlativo: string;
    fechaEmision: string;
    fechaVencimiento?: string;
    condicionPago: string;
    moneda: string;
    percepcion?: number;
    ajusteRedondeo?: number;
    valorVentaGravado?: number;
    valorVentaExonerado?: number;
    valorVentaInafecto?: number;
    subtotal?: number;
    igv?: number;
    total?: number;
    isAjusteManual?: boolean;
    nombreGrupo?: string;
    detalles: DetalleCompraRequest[];
}

export interface OrdenCreateRequest {
    idProveedor: number;
    idSucursal: number;
    nombreGrupo?: string;
    valorVentaGravado?: number;
    valorVentaExonerado?: number;
    valorVentaInafecto?: number;
    igv?: number;
    total?: number;
    detalles: DetalleCompraRequest[];
}

export interface DetalleCompraRequest {
    idProducto: number;
    cantidad: number;
    idUnidadMedida?: number;
    factorConversion?: number;
    unidad?: string;
    descripcion?: string;
    lote?: string;
    presentacion?: string;
    fechaVencimiento?: string;
    precioUnitario: number;
    porcentajeDescuento?: number;
    porcentajeDescuento2?: number;
    esBonificacion?: boolean;
    tipoAfectacion: string;
}

export interface CompraResponse {
    id: number;
    proveedor: ProveedorResponse;
    tipoComprobante: string;
    serie: string;
    correlativo: string;
    fechaEmision: string;
    fechaVencimiento?: string;
    condicionPago: string;
    moneda: string;
    valorVentaGravado: number;
    valorVentaExonerado: number;
    valorVentaInafecto: number;
    subtotal: number;
    igv: number;
    total: number;
    percepcion: number;
    ajusteRedondeo: number;
    totalPagar: number;
    estado: any;
    fechaRegistro: string;
    nombreGrupo?: string;
    solicitarFondo?: boolean;
    detalles: DetalleCompraResponse[];
    cronograma?: CronogramaPago[];
}

export interface DetalleCompraResponse {
    id: number;
    producto: CatalogoResponse;
    cantidad: number;
    idUnidadMedida?: number;
    factorConversion?: number;
    unidad: string;
    descripcion: string;
    lote?: string;
    fechaVencimiento?: string;
    precioUnitario: number;
    porcentajeDescuento: number;
    porcentajeDescuento2: number;
    esBonificacion: boolean;
    tipoAfectacion: string;
    valorVenta: number;
}
