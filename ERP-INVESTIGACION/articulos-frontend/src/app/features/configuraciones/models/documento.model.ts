export enum FormatoDocumento {
    TICKET_80MM = 'TICKET_80MM',
    TICKET_58MM = 'TICKET_58MM',
    HOJA_A4 = 'HOJA_A4',
    MEDIA_A5 = 'MEDIA_A5'
}

export interface DocumentoPlantilla {
    id?: number;
    codigo: string;
    nombre: string;
    formato: FormatoDocumento;
    contenidoHtml: string;
    estilosCss: string;
    camposDinamicos: string;
    activo: boolean;
}

export const FORMATO_DIMENSIONES = {
    [FormatoDocumento.TICKET_80MM]: { width: '80mm', height: 'auto' },
    [FormatoDocumento.TICKET_58MM]: { width: '58mm', height: 'auto' },
    [FormatoDocumento.HOJA_A4]: { width: '210mm', height: '297mm' },
    [FormatoDocumento.MEDIA_A5]: { width: '148mm', height: '210mm' }
};
