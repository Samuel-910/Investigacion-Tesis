export interface DesglosePrecio {
    bruto: number;
    descuento1: number;
    valorTrasD1: number;
    descuento2: number;
    neto: number;
    igv: number;
    total: number;
}

export class RowCalculationUtils {
    private static readonly IGV_RATE = 0.18;

    /**
     * Calcula el precio unitario base revirtiendo descuentos e impuestos.
     * @param montoIngresado El monto total final (o neto según incluyeIgv)
     * @param cantidad Cantidad de productos
     * @param d1 Porcentaje de descuento 1
     * @param d2 Porcentaje de descuento 2
     * @param tipoAfectacion 'GRAVADO', 'EXONERADO' o 'INAFECTO'
     * @param incluyeIgv Si el monto ingresado ya contiene el IGV
     */
    static calcularPrecioUnitarioInverso(
        montoIngresado: number,
        cantidad: number,
        d1: number = 0,
        d2: number = 0,
        tipoAfectacion: string = 'GRAVADO',
        incluyeIgv: boolean = true
    ): number {
        if (cantidad <= 0) return 0;

        let montoNeto = montoIngresado;

        // 1. Extraer IGV si corresponde y el monto lo incluye
        if (incluyeIgv && tipoAfectacion === 'GRAVADO') {
            montoNeto = montoIngresado / (1 + this.IGV_RATE);
        }

        // 2. Revertir Descuento 2
        // Neto = TrasD1 * (1 - d2/100) => TrasD1 = Neto / (1 - d2/100)
        let valorTrasD1 = montoNeto;
        if (d2 > 0 && d2 < 100) {
            valorTrasD1 = montoNeto / (1 - d2 / 100);
        }

        // 3. Revertir Descuento 1
        // TrasD1 = Bruto * (1 - d1/100) => Bruto = TrasD1 / (1 - d1/100)
        let valorBruto = valorTrasD1;
        if (d1 > 0 && d1 < 100) {
            valorBruto = valorTrasD1 / (1 - d1 / 100);
        }

        // 4. Hallar precio unitario
        const precioUnitario = valorBruto / cantidad;

        // Retornar con 3 decimales según requerimiento
        return Number(precioUnitario.toFixed(3));
    }

    /**
     * Genera un desglose detallado (histórico) del precio.
     */
    static generarDesglosePrecios(
        cantidad: number,
        precioUnitarioBase: number,
        d1: number = 0,
        d2: number = 0,
        tipoAfectacion: string = 'GRAVADO',
        sumarIgv: boolean = true
    ): DesglosePrecio {
        const bruto = Number((cantidad * precioUnitarioBase).toFixed(2));
        
        const d1_monto = Number((bruto * (d1 / 100)).toFixed(2));
        const valorTrasD1 = Number((bruto - d1_monto).toFixed(2));
        
        const d2_monto = Number((valorTrasD1 * (d2 / 100)).toFixed(2));
        const neto = Number((valorTrasD1 - d2_monto).toFixed(2));

        let igv = 0;
        if (tipoAfectacion === 'GRAVADO' && sumarIgv) {
            igv = Number((neto * this.IGV_RATE).toFixed(2));
        }

        const total = Number((neto + igv).toFixed(2));

        return {
            bruto,
            descuento1: d1_monto,
            valorTrasD1,
            descuento2: d2_monto,
            neto,
            igv,
            total
        };
    }
}
