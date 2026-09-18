import { ProductoRequest } from './producto.model';

export class ProductoMapper {

    static toRequest(formValue: any, idProducto: number, currentSucursalId: number | null): ProductoRequest {
        return {
            idProducto: idProducto,
            idSucursal: currentSucursalId || 1,
            idCatalogo: formValue.idCatalogo,
            idDetalleCompra: formValue.idDetalleCompra,
            idProveedor: formValue.idProveedor,

            // Basic Info
            codigoBarra: formValue.codigoBarra,
            codDigemid: formValue.codDigemid,
            presentacion: formValue.presentacion,
            idLaboratorio: formValue.idLaboratorio,

            // Location
            idUbicacion: formValue.idUbicacion,
            idAlmacen: formValue.idAlmacen,

            // Inventory
            stock: formValue.stock,
            nroLote: formValue.nroLote,
            fechaVencimiento: formValue.fechaVencimiento,
            diasAlertaVencimiento: formValue.diasAlertaVencimiento,

            // Costs
            precioCompra: formValue.precioCompra,

            // Unit Prices
            manejaUnidad: formValue.manejaUnidad,
            precioVentaUnitario: formValue.precioVentaUnitario,
            precioUnitarioMin: formValue.precioUnitarioMin,
            tipoGananciaUnidad: formValue.tipoGananciaUnidad,
            gananciaUnidad: formValue.gananciaUnidad,
            gananciaUnidadMin: formValue.gananciaUnidadMin,

            // Blister Prices
            manejaBlister: formValue.manejaBlister,
            factorBlister: formValue.factorBlister,
            precioVentaBlister: formValue.precioVentaBlister,
            precioBlisterMin: formValue.precioBlisterMin,
            tipoGananciaBlister: formValue.tipoGananciaBlister,
            gananciaBlister: formValue.gananciaBlister,
            gananciaBlisterMin: formValue.gananciaBlisterMin,

            // Box Prices
            manejaCaja: formValue.manejaCaja,
            factorCaja: formValue.factorCaja,
            precioVentaCaja: formValue.precioVentaCaja,
            precioCajaMin: formValue.precioCajaMin,
            tipoGananciaCaja: formValue.tipoGananciaCaja,
            gananciaCaja: formValue.gananciaCaja,
            gananciaCajaMin: formValue.gananciaCajaMin,

            // Flags
            manejaLote: !!formValue.nroLote
        } as ProductoRequest;
    }

    static parsePresentation(presentacion: string): { nivel: 'MENOR' | 'INTERMEDIO' | 'MAYOR', factores: number[] } {
        const config: any = {
            nivel: 'MENOR',
            factores: []
        };

        if (!presentacion) return config;

        const p = presentacion.toUpperCase();
        const numbers = p.match(/(\d+)/g);

        if (numbers && numbers.length == 0) {
            config.nivel = 'MENOR';
            config.factores = [];
        } else if (numbers && numbers.length == 1) {
            config.nivel = 'INTERMEDIO';
            config.factores = [parseInt(numbers[0], 10)];
        } else if (numbers && numbers.length >= 2) {
            config.nivel = 'MAYOR';
            config.factores = [parseInt(numbers[0], 10), parseInt(numbers[1], 10)];
        }

        return config;
    }
}
