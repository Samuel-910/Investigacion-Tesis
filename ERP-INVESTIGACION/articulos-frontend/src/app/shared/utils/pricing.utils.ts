export interface PrecioVentaConIgvResult {
    precioVenta: number;
    ganancia: number;
    margen: number;
}

export interface PrecioVentaSinIgvResult {
    costoNeto: number;
    valorVenta: number;
    igvAmount: number;
    precioFinal: number;
    gananciaReal: number;
    margen: number;
}

export class PricingUtils {
    private static readonly IGV_RATE = 0.18;

    private static round2(num: number): number {
        return Math.round((num + Number.EPSILON) * 100) / 100;
    }

    static calcularPrecioVentaConIgvPorPorcentaje(precioCompraConIgv: number, margenPorcentaje: number): PrecioVentaConIgvResult {
        if (margenPorcentaje >= 100) {
            throw new Error("El margen debe ser menor al 100%");
        }

        const margenDecimal = margenPorcentaje / 100;
        // PrecioVenta = Costo / (1 - Margen)
        const precioVenta = this.round2(precioCompraConIgv / (1 - margenDecimal));
        const ganancia = this.round2(precioVenta - precioCompraConIgv);

        return {
            precioVenta,
            ganancia,
            margen: margenPorcentaje
        };
    }

    static calcularMargenConIgvPorPrecioFijo(precioCompraConIgv: number, precioVentaFijo: number): PrecioVentaConIgvResult {
        if (precioVentaFijo <= 0) {
            return { precioVenta: 0, ganancia: 0, margen: 0 };
        }

        const ganancia = this.round2(precioVentaFijo - precioCompraConIgv);
        // Margen = (Ganancia / PrecioVenta) * 100
        const margen = this.round2((ganancia / precioVentaFijo) * 100);

        return {
            precioVenta: precioVentaFijo,
            ganancia,
            margen
        };
    }

    static calcularPrecioVentaSinIgvPorPorcentaje(precioCompraConIgv: number, margenPorcentaje: number): PrecioVentaSinIgvResult {
        if (margenPorcentaje >= 100) {
            throw new Error("El margen debe ser menor al 100%");
        }

        const igvFactor = 1 + this.IGV_RATE;
        const costoNeto = this.round2(precioCompraConIgv / igvFactor);

        const margenDecimal = margenPorcentaje / 100;

        // Valor Venta (Sin IGV) = CostoNeto / (1 - Margen)
        const valorVenta = this.round2(costoNeto / (1 - margenDecimal));

        const igvAmount = this.round2(valorVenta * this.IGV_RATE);
        const precioFinal = this.round2(valorVenta + igvAmount);
        const gananciaReal = this.round2(valorVenta - costoNeto);

        return {
            costoNeto,
            valorVenta,
            igvAmount,
            precioFinal,
            gananciaReal,
            margen: margenPorcentaje
        };
    }

    static calcularMargenSinIgvPorPrecioFijo(precioCompraConIgv: number, precioVentaFinalFijo: number): PrecioVentaSinIgvResult {
        const igvFactor = 1 + this.IGV_RATE;

        const costoNeto = this.round2(precioCompraConIgv / igvFactor);
        const valorVenta = this.round2(precioVentaFinalFijo / igvFactor);

        const igvAmount = this.round2(valorVenta * this.IGV_RATE);
        const gananciaReal = this.round2(valorVenta - costoNeto);

        let margen = 0;
        if (valorVenta > 0) {
            // Margen = (GananciaReal / ValorVenta) * 100
            margen = this.round2((gananciaReal / valorVenta) * 100);
        }

        return {
            costoNeto,
            valorVenta,
            igvAmount,
            precioFinal: precioVentaFinalFijo,
            gananciaReal,
            margen
        };
    }
}
