export interface Clinica {
    id?: number;
    razonSocial: string;
    ruc: string;
    direccion?: string;
    telefono?: string;
    celular1?: string;
    celular2?: string;
    email?: string;
    representante?: string;
    auditor?: string;
    liquidador?: string;
    financiero?: string;
    web?: string;
    ctacte?: string;
    codigo?: string;
    abrev?: string;
    logoCuadrado?: string;
    logoRectangular?: string;
    logoPrincipal?: string;
    estado?: string;
    fechaCreacion?: string;
}

export interface ClinicaAuditoria {
    id: number;
    idClinica: number;
    usuario: string;
    fechaCambio: string;
    operacion: string;
    datosAnteriores: string;
}

// Mantener Empresa por compatibilidad con otros módulos si existen
export interface Empresa {
    id?: number;
    razonSocial: string;
    abrev?: string;
    nombreComercial?: string;
    ruc: string;
    direccion?: string;
    telefono?: string;
    email?: string;
    web?: string;
    estado?: string;
}

export interface EmpresaPersonaVinculo {
    id?: number;
    idEmpresa: number;
    idPersona: string;
    cargo?: string;
    estado?: string;
}

export interface VinculoDTO {
    idEmpresa: number;
    idPersonal: string;
    cargo?: string;
}
