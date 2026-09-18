export interface UnidadMedidaRequest {
    nombre: string;
    simbolo: string;
    codigoSunat?: string;
    estado: boolean;
    esAgrupador?: boolean;
}

export interface UnidadMedidaResponse {
    id: number;
    nombre: string;
    simbolo: string;
    codigoSunat?: string;
    estado: boolean;
    esAgrupador?: boolean;
}
