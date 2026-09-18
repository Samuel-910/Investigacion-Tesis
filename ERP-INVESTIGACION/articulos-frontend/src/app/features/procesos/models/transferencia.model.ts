import { CatalogoResponse } from '../../configuraciones/models/catalogo.model';
import { EstadoGeneral } from '../../../core/interfaces/estado-general.interface';

export type EstadoTransferencia = 'SOLICITADO' | 'ENVIADO' | 'RECIBIDO' | 'CANCELADO' | EstadoGeneral;

export interface TransferenciaSucursal {
    id?: number;
    idSucursalOrigen: number;
    sucursalOrigenNombre?: string;
    idSucursalDestino: number;
    sucursalDestinoNombre?: string;
    estado: EstadoTransferencia;
    idUsuarioSolicita: number;
    usuarioSolicitaNombre?: string;
    idUsuarioEnvia?: number;
    usuarioEnviaNombre?: string;
    idUsuarioRecibe?: number;
    usuarioRecibeNombre?: string;
    idUsuarioCancela?: number;
    usuarioCancelaNombre?: string;
    motivo?: string;
    fechaSolicitud?: string;
    fechaEnvio?: string;
    fechaRecepcion?: string;
    fechaCancelacion?: string;
    detalles: TransferenciaDetalle[];
}

export interface TransferenciaDetalle {
    id?: number;
    idCatalogo: number;
    productoNombre: string;
    productoCodigo: string;
    cantidadSolicitada: number;
    cantidadEnviada?: number;
    cantidadRecibida?: number;
    nroLote?: string;
    fechaVenc?: string;
}

export interface TransferenciaRequest {
    idSucursalOrigen: number;
    idSucursalDestino: number;
    motivo: string;
    idUsuario: number;
    detalles: {
        idCatalogo: number;
        cantidad: number;
    }[];
}
