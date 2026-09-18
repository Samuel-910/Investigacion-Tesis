export interface TiempoAtencionReporte {
    metricas: Metricas;
    resumenPorArea: ResumenArea[];
    listadoDetallado: DetalleAtencion[];
}

export interface Metricas {
    totalSolicitudes: number;
    cumpleMeta: number;
    noCumpleMeta: number;
    tiempoPromedioMinutos: number;
    tiempoPromedioFormato: string; // HH:MM:SS
}

export interface ResumenArea {
    areaId: number;
    areaNombre: string;
    cantidad: number;
    tiempoTotalMinutos: number;
    tiempoTotalFormato: string;
}

export interface DetalleAtencion {
    numeroOrden: string;
    fechaSolicitud: string | null;
    medicoNombre: string;
    pacienteNombre: string;
    areaProceso: string;
    examenNombre: string;
    tiempoAtencionMinutos: number;
    tiempoAtencionFormato: string;
    metaMinutos: number;
    metaFormato: string;
    cumple: boolean;
}
