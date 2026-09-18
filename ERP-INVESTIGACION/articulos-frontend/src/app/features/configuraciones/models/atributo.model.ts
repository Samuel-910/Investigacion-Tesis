export interface BaseAtributo {
    id: number;
    descripcion: string;
    estado: string;
    fechaCreacion?: string;
    fechaModificacion?: string;
}

export interface Categoria extends BaseAtributo { }
export interface Laboratorio extends BaseAtributo { }
export interface PrincipioActivo extends BaseAtributo { }
export interface AccionTerapeutica extends BaseAtributo { }
export interface Ubicacion extends BaseAtributo { }

export interface Tipo extends BaseAtributo { }
export interface Proceso extends BaseAtributo { }

export interface AtributoRequest {
    descripcion: string;
    estado: string;
}
