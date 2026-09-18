export interface Clasificacion {
    id?: number;
    nombre: string;
    tipo: 'INGRESO' | 'SALIDA' | 'AMBOS';
    estado?: string | any;
}

export interface ClasificacionRequest {
    nombre: string;
    tipo: string;
    estado?: string;
}
