import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export class ProductoFormFactory {

    static create(fb: FormBuilder): FormGroup {
        return fb.group({
            idProducto: [null],
            idCatalogo: [null, Validators.required],
            unidadBaseNombre: [''],
            unidadIntermediaNombre: [''],
            unidadMayorNombre: [''],
            nombre: [''],
            codigo: [''],
            descripcion: [''],
            tipo: ['PRODUCTO'],
            codigoBarra: [''],
            codDigemid: [''],
            presentacion: [''],
            idLaboratorio: [null, Validators.required],
            idUbicacion: [null, Validators.required],
            idAlmacen: [null, Validators.required],
            cantidadCompra: [0],
            unidadCompra: [''],
            totalUnidades: [0],
            factorCompra: [1],
            estado: [true],
            usuarioCreacion: ['SYSTEM'],
            idProveedor: [null],
            proveedorRazonSocial: [''],
            precioCompra: [null, [Validators.min(0)]],
            fechaVencimiento: [null],
            diasAlertaVencimiento: [null, Validators.required],
            manejaUnidad: [true],
            stock: [null, [Validators.min(0)]],
            precioVentaUnitario: [null, [Validators.required, Validators.min(0)]],
            precioUnitarioMin: [null, Validators.required],
            tipoGananciaUnidad: ['PORCENTAJE'],
            gananciaUnidad: [null, Validators.required],
            gananciaUnidadMin: [null, Validators.required],
            nroLote: [''],
            manejaBlister: [false],
            precioBlisterMin: [null],
            precioVentaBlister: [null],
            factorBlister: [1, [Validators.required, Validators.min(1)]],
            tipoGananciaBlister: ['PORCENTAJE'],
            gananciaBlister: [null],
            gananciaBlisterMin: [null],
            manejaCaja: [false],
            precioCajaMin: [null],
            precioVentaCaja: [null],
            factorCaja: [1, [Validators.required, Validators.min(1)]],
            tipoGananciaCaja: ['PORCENTAJE'],
            gananciaCaja: [null],
            gananciaCajaMin: [null],
        });
    }

    static setupPriceValidators(targetForm: FormGroup, validateCallback: (form: FormGroup) => void): void {
        const fields = [
            'precioCompra',
            'precioVentaUnitario', 'precioUnitarioMin',
            'manejaBlister', 'factorBlister', 'precioVentaBlister', 'precioBlisterMin',
            'manejaCaja', 'factorCaja', 'precioVentaCaja', 'precioCajaMin'
        ];

        fields.forEach(f => {
            targetForm.get(f)?.valueChanges.subscribe(() => validateCallback(targetForm));
        });
    }

    static validatePrices(targetForm: FormGroup): void {
        const compra = targetForm.get('precioCompra')?.value || 0;

        const setErr = (ctrlName: string, errKey: string, condition: boolean) => {
            const ctrl = targetForm.get(ctrlName);
            if (!ctrl) return;
            const errs = ctrl.errors || {};
            if (condition) {
                ctrl.setErrors({ ...errs, [errKey]: true });
            } else {
                const { [errKey]: removed, ...rest } = errs;
                ctrl.setErrors(Object.keys(rest).length ? rest : null);
            }
        };

        const venta = targetForm.get('precioVentaUnitario')?.value || 0;
        const min = targetForm.get('precioUnitarioMin')?.value || 0;

        setErr('precioVentaUnitario', 'minPriceBase', venta < compra);
        setErr('precioUnitarioMin', 'minPriceBase', min < compra);

        if (targetForm.get('manejaBlister')?.value) {
            const factorBlister = targetForm.get('factorBlister')?.value || 1;
            const costoBlister = compra * factorBlister;

            const ventaBlister = targetForm.get('precioVentaBlister')?.value || 0;
            const minBlister = targetForm.get('precioBlisterMin')?.value || 0;

            setErr('precioVentaBlister', 'minPriceBase', ventaBlister < costoBlister);
            setErr('precioBlisterMin', 'minPriceBase', minBlister < costoBlister);

            const ventaBlisterFuera = (minBlister > 0 && ventaBlister < minBlister);
            setErr('precioVentaBlister', 'outOfRange', ventaBlisterFuera);
            setErr('precioBlisterMin', 'maxPriceSale', minBlister > ventaBlister);
        }

        if (targetForm.get('manejaCaja')?.value) {
            let factorRealCaja = targetForm.get('factorCaja')?.value || 1;
            if (targetForm.get('manejaBlister')?.value) {
                factorRealCaja *= (targetForm.get('factorBlister')?.value || 1);
            }
            const costoCaja = compra * factorRealCaja;

            const ventaCaja = targetForm.get('precioVentaCaja')?.value || 0;
            const minCaja = targetForm.get('precioCajaMin')?.value || 0;

            setErr('precioVentaCaja', 'minPriceBase', ventaCaja < costoCaja);
            setErr('precioCajaMin', 'minPriceBase', minCaja < costoCaja);

            const ventaCajaFuera = (minCaja > 0 && ventaCaja < minCaja);
            setErr('precioVentaCaja', 'outOfRange', ventaCajaFuera);
            setErr('precioCajaMin', 'maxPriceSale', minCaja > ventaCaja);
        }
    }
}
