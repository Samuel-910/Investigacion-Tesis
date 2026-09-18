export type MetodoPago = string;

export interface CajaChica {
    id?: number;
    nombre: string;
    saldoActual: number;
    saldoInicial: number;
    saldoCierreReal?: number;
    idPuntoVenta?: number;
    idUsuarioCajero?: string;
    fechaCierre?: string;
    estado: 'ABIERTA' | 'CERRADA';
    idSucursal?: number;
    createdAt?: string;
}

export interface Movimiento {
    id?: number;
    cajaChicaId: number;
    tipo: 'INGRESO' | 'EGRESO';
    monto: number;
    descripcion: string;
    metodoPago: MetodoPago;
    referencia?: string;
    fecha?: string;
    usuario?: string;
}

export interface MetodoPagoResumen {
    metodoPago: string;
    monto: number;
}

export interface CajaResumen {
    cajaId: number;
    nombre: string;
    saldoInicial: number;
    ingresos: MetodoPagoResumen[];
    egresos: MetodoPagoResumen[];
    totalIngresos: number;
    totalEgresos: number;
    saldoFinalTeorico: number;
}
