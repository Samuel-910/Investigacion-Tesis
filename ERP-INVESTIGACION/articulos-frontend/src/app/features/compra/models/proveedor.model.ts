export interface Proveedor {
    id?: number;
    tipoDocIdent: string;
    numDocIdent: string;
    razonSocial: string;
    nombreComercial?: string;
    direccion?: string;
    email?: string;
    telefono?: string;
    departamento?: string;
    provincia?: string;
    distrito?: string;
    estado: number;
    fechaRegistro?: string;
    saldo?: number;
    plazoDias?: number;
    plazoPago?: number;
}

export type ProveedorResponse = Proveedor;
