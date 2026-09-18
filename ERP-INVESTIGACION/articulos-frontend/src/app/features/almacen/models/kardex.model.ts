export interface RegArticuloKardex {
    idArticuloKardex: string;
    idAlmArticulo: number;
    idSucursal: number;
    idUsuario: number;
    usuario?: { id: number, nombreCompleto: string };
    producto?: { id: number, nombre: string };
    idDocumento: string;
    numDoc: string;
    fecha: string;
    fechaVenc?: string;
    nroLote?: string;
    operacion: string;
    detalle: string;
    observacion: string;
    signo: string;
    cantidad: number;
    costoUnitario: number;
    costoTotal: number;
    saldoCantidad: number;
    saldoCostoUnitario: number;
    saldoCostoTotal: number;
    origenId: string;
    origenTipo: string;
    presentacion: string;
}
