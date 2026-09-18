import { FormGroup } from '@angular/forms';
import { signal, WritableSignal } from '@angular/core';
import { PricingUtils } from '../../../../shared/utils/pricing.utils';

export class ProductoPricingManager {

    // Signals para exponer los beneficios calculados
    unitProfit = signal<{ igv: number, noIgv: number } | null>(null);
    unitProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    unitProfitMax = signal<{ igv: number, noIgv: number } | null>(null);

    blisterProfit = signal<{ igv: number, noIgv: number } | null>(null);
    blisterProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    blisterProfitMax = signal<{ igv: number, noIgv: number } | null>(null);

    cajaProfit = signal<{ igv: number, noIgv: number } | null>(null);
    cajaProfitMin = signal<{ igv: number, noIgv: number } | null>(null);
    cajaProfitMax = signal<{ igv: number, noIgv: number } | null>(null);

    constructor(private form: FormGroup) { }

    setupAutoCalculations(): void {
        this.form.get('precioCompra')?.valueChanges.subscribe(() => {
            this.recalculateAllPrices();
        });

        // Unit
        this.form.get('tipoGananciaUnidad')?.valueChanges.subscribe(() => this.calculateUnit());
        this.form.get('gananciaUnidad')?.valueChanges.subscribe(() => this.calculateUnit());
        this.form.get('gananciaUnidadMin')?.valueChanges.subscribe(() => this.calculateUnit());

        this.form.get('precioVentaUnitario')?.valueChanges.subscribe(() => this.calculateUnitReverse());
        this.form.get('precioUnitarioMin')?.valueChanges.subscribe(() => this.calculateUnitReverse());

        // Blister
        this.form.get('tipoGananciaBlister')?.valueChanges.subscribe(() => this.calculateBlister());
        this.form.get('gananciaBlister')?.valueChanges.subscribe(() => this.calculateBlister());
        this.form.get('gananciaBlisterMin')?.valueChanges.subscribe(() => this.calculateBlister());
        this.form.get('factorBlister')?.valueChanges.subscribe(() => this.calculateBlister());

        this.form.get('precioVentaBlister')?.valueChanges.subscribe(() => this.calculateBlisterReverse());
        this.form.get('precioBlisterMin')?.valueChanges.subscribe(() => this.calculateBlisterReverse());

        // Caja
        this.form.get('tipoGananciaCaja')?.valueChanges.subscribe(() => this.calculateCaja());
        this.form.get('gananciaCaja')?.valueChanges.subscribe(() => this.calculateCaja());
        this.form.get('gananciaCajaMin')?.valueChanges.subscribe(() => this.calculateCaja());
        this.form.get('factorCaja')?.valueChanges.subscribe(() => this.calculateCaja());

        this.form.get('precioVentaCaja')?.valueChanges.subscribe(() => this.calculateCajaReverse());
        this.form.get('precioCajaMin')?.valueChanges.subscribe(() => this.calculateCajaReverse());
    }

    recalculateAllPrices(): void {
        this.calculateUnit();
        this.calculateBlister();
        this.calculateCaja();
    }

    // --- Unit logic ---

    calculateUnit(): void {
        const type = this.form.get('tipoGananciaUnidad')?.value;
        const costo = this.getUnitCost();

        let ganancia = this.form.get('gananciaUnidad')?.value;
        if (ganancia !== null && ganancia !== '') {
            ganancia = parseFloat(ganancia);
            if (type === 'PORCENTAJE') {
                try {
                    const res = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, ganancia);
                    this.form.get('precioVentaUnitario')?.setValue(res.precioVenta, { emitEvent: false });
                } catch (e) {
                    // Fallback simpler math if util fails or for safety
                    const venta = costo * (1 + ganancia / 100);
                    this.form.get('precioVentaUnitario')?.setValue(venta.toFixed(2), { emitEvent: false });
                }
            } else if (type === 'FIJO') {
                const venta = costo + ganancia;
                this.form.get('precioVentaUnitario')?.setValue(venta.toFixed(2), { emitEvent: false });
            }
        }

        // Min Price Calc
        let gananciaMin = this.form.get('gananciaUnidadMin')?.value;
        if (gananciaMin !== null && gananciaMin !== '') {
            gananciaMin = parseFloat(gananciaMin);
            if (type === 'PORCENTAJE') {
                try {
                    const resMin = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, gananciaMin);
                    this.form.get('precioUnitarioMin')?.setValue(resMin.precioVenta, { emitEvent: false });
                } catch (e) { }
            } else if (type === 'FIJO') {
                const ventaMin = costo + gananciaMin;
                this.form.get('precioUnitarioMin')?.setValue(ventaMin.toFixed(2), { emitEvent: false });
            }
        }

        this.updateUnitProfit();
    }

    calculateUnitReverse(): void {
        const type = this.form.get('tipoGananciaUnidad')?.value;
        const costo = this.getUnitCost();
        const venta = parseFloat(this.form.get('precioVentaUnitario')?.value || '0');

        if (costo > 0 && venta > 0) {
            if (type === 'FIJO') {
                const margen = venta - costo;
                this.form.get('gananciaUnidad')?.setValue(margen.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                // Margen sobre Ventas: (Venta - Costo) / Venta
                const margen = ((venta - costo) / venta) * 100;
                this.form.get('gananciaUnidad')?.setValue(margen.toFixed(2), { emitEvent: false });
            }
        }

        // Reverse Min
        const ventaMin = parseFloat(this.form.get('precioUnitarioMin')?.value || '0');
        if (costo > 0 && ventaMin > 0) {
            if (type === 'FIJO') {
                const margenMin = ventaMin - costo;
                this.form.get('gananciaUnidadMin')?.setValue(margenMin.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                const margenMin = ((ventaMin - costo) / ventaMin) * 100;
                this.form.get('gananciaUnidadMin')?.setValue(margenMin.toFixed(2), { emitEvent: false });
            }
        }

        this.updateUnitProfit();
    }

    private updateUnitProfit() {
        const costo = this.getUnitCost();
        const venta = this.form.get('precioVentaUnitario')?.value || 0;
        const min = this.form.get('precioUnitarioMin')?.value || 0;
        const max = this.form.get('precioUnitarioMax')?.value || 0;

        this.unitProfit.set(this.calculateProfitStats(costo, venta));
        this.unitProfitMin.set(min > 0 ? this.calculateProfitStats(costo, min) : null);
        this.unitProfitMax.set(max > 0 ? this.calculateProfitStats(costo, max) : null);
    }

    // --- Blister logic ---

    calculateBlister(): void {
        if (!this.form.get('manejaBlister')?.value) return;
        const type = this.form.get('tipoGananciaBlister')?.value;
        const costo = this.getBlisterCost();

        let ganancia = this.form.get('gananciaBlister')?.value;
        if (ganancia !== null && ganancia !== '') {
            ganancia = parseFloat(ganancia);
            if (type === 'PORCENTAJE') {
                try {
                    const res = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, ganancia);
                    this.form.get('precioVentaBlister')?.setValue(res.precioVenta, { emitEvent: false });
                } catch (e) { }
            } else if (type === 'FIJO') {
                const venta = costo + ganancia;
                this.form.get('precioVentaBlister')?.setValue(venta.toFixed(2), { emitEvent: false });
            }
        }

        let gananciaMin = this.form.get('gananciaBlisterMin')?.value;
        if (gananciaMin !== null && gananciaMin !== '') {
            gananciaMin = parseFloat(gananciaMin);
            if (type === 'PORCENTAJE') {
                try {
                    const res = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, gananciaMin);
                    this.form.get('precioBlisterMin')?.setValue(res.precioVenta, { emitEvent: false });
                } catch (e) { }
            } else if (type === 'FIJO') {
                const venta = costo + gananciaMin;
                this.form.get('precioBlisterMin')?.setValue(venta.toFixed(2), { emitEvent: false });
            }
        }

        this.updateBlisterProfit();
    }

    calculateBlisterReverse(): void {
        if (!this.form.get('manejaBlister')?.value) return;
        const type = this.form.get('tipoGananciaBlister')?.value;
        const costo = this.getBlisterCost();
        const venta = parseFloat(this.form.get('precioVentaBlister')?.value || '0');

        if (costo > 0 && venta > 0) {
            if (type === 'FIJO') {
                const margen = venta - costo;
                this.form.get('gananciaBlister')?.setValue(margen.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                const margen = ((venta - costo) / venta) * 100;
                this.form.get('gananciaBlister')?.setValue(margen.toFixed(2), { emitEvent: false });
            }
        }

        const ventaMin = parseFloat(this.form.get('precioBlisterMin')?.value || '0');
        if (costo > 0 && ventaMin > 0) {
            if (type === 'FIJO') {
                const margen = ventaMin - costo;
                this.form.get('gananciaBlisterMin')?.setValue(margen.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                const margen = ((ventaMin - costo) / ventaMin) * 100;
                this.form.get('gananciaBlisterMin')?.setValue(margen.toFixed(2), { emitEvent: false });
            }
        }

        this.updateBlisterProfit();
    }

    private updateBlisterProfit() {
        const costo = this.getBlisterCost();
        const venta = this.form.get('precioVentaBlister')?.value || 0;
        const min = this.form.get('precioBlisterMin')?.value || 0;
        const max = this.form.get('precioBlisterMax')?.value || 0;

        this.blisterProfit.set(this.calculateProfitStats(costo, venta));
        this.blisterProfitMin.set(min > 0 ? this.calculateProfitStats(costo, min) : null);
        this.blisterProfitMax.set(max > 0 ? this.calculateProfitStats(costo, max) : null);
    }

    // --- Caja logic ---

    calculateCaja(): void {
        if (!this.form.get('manejaCaja')?.value) return;
        const type = this.form.get('tipoGananciaCaja')?.value;
        const costo = this.getCajaCost();

        let ganancia = this.form.get('gananciaCaja')?.value;
        if (ganancia !== null && ganancia !== '') {
            ganancia = parseFloat(ganancia);
            if (type === 'PORCENTAJE') {
                try {
                    const res = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, ganancia);
                    this.form.get('precioVentaCaja')?.setValue(res.precioVenta, { emitEvent: false });
                } catch (e) { }
            } else if (type === 'FIJO') {
                const venta = costo + ganancia;
                this.form.get('precioVentaCaja')?.setValue(venta.toFixed(2), { emitEvent: false });
            }
        }

        let gananciaMin = this.form.get('gananciaCajaMin')?.value;
        if (gananciaMin !== null && gananciaMin !== '') {
            gananciaMin = parseFloat(gananciaMin);
            if (type === 'PORCENTAJE') {
                try {
                    const res = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(costo, gananciaMin);
                    this.form.get('precioCajaMin')?.setValue(res.precioVenta, { emitEvent: false });
                } catch (e) { }
            } else if (type === 'FIJO') {
                const venta = costo + gananciaMin;
                this.form.get('precioCajaMin')?.setValue(venta.toFixed(2), { emitEvent: false });
            }
        }

        this.updateCajaProfit();
    }

    calculateCajaReverse(): void {
        if (!this.form.get('manejaCaja')?.value) return;
        const type = this.form.get('tipoGananciaCaja')?.value;
        const costo = this.getCajaCost();
        const venta = parseFloat(this.form.get('precioVentaCaja')?.value || '0');

        if (costo > 0 && venta > 0) {
            if (type === 'FIJO') {
                const margen = venta - costo;
                this.form.get('gananciaCaja')?.setValue(margen.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                const margen = ((venta - costo) / venta) * 100;
                this.form.get('gananciaCaja')?.setValue(margen.toFixed(2), { emitEvent: false });
            }
        }

        const ventaMin = parseFloat(this.form.get('precioCajaMin')?.value || '0');
        if (costo > 0 && ventaMin > 0) {
            if (type === 'FIJO') {
                const margen = ventaMin - costo;
                this.form.get('gananciaCajaMin')?.setValue(margen.toFixed(2), { emitEvent: false });
            } else if (type === 'PORCENTAJE') {
                const margen = ((ventaMin - costo) / ventaMin) * 100;
                this.form.get('gananciaCajaMin')?.setValue(margen.toFixed(2), { emitEvent: false });
            }
        }

        this.updateCajaProfit();
    }

    private updateCajaProfit() {
        const costo = this.getCajaCost();
        const venta = this.form.get('precioVentaCaja')?.value || 0;
        const min = this.form.get('precioCajaMin')?.value || 0;
        const max = this.form.get('precioCajaMax')?.value || 0;

        this.cajaProfit.set(this.calculateProfitStats(costo, venta));
        this.cajaProfitMin.set(min > 0 ? this.calculateProfitStats(costo, min) : null);
        this.cajaProfitMax.set(max > 0 ? this.calculateProfitStats(costo, max) : null);
    }

    // --- Helpers ---

    getUnitCost(): number {
        return this.form.get('precioCompra')?.value || 0;
    }

    getBlisterCost(): number {
        const base = this.getUnitCost();
        const factor = this.form.get('factorBlister')?.value || 1;
        return base * factor;
    }

    getCajaCost(): number {
        const base = this.getUnitCost();
        let factor = this.form.get('factorCaja')?.value || 1;
        if (this.form.get('manejaBlister')?.value) {
            factor *= (this.form.get('factorBlister')?.value || 1);
        }
        return base * factor;
    }

    calculateProfitStats(costo: number, venta: number): { igv: number, noIgv: number } {
        const gainIgv = parseFloat((venta - costo).toFixed(2));
        const gainNoIgv = parseFloat(((venta / 1.18) - (costo / 1.18)).toFixed(2));
        return { igv: gainIgv, noIgv: gainNoIgv };
    }

    calculatePrice(form: FormGroup, level: 'unit' | 'blister' | 'caja', type: 'standard' | 'min') {
        const baseCost = form.get('precioCompra')?.value || 0;
        let cost = baseCost;

        let tipoGananciaCtrl = '';
        let valorGananciaCtrl = '';
        let targetPriceCtrl = '';

        if (level === 'unit') {
            tipoGananciaCtrl = type === 'standard' ? 'tipoGananciaUnidad' : 'tipoGananciaUnidadMin'; // Fixed typo 'Unit' to 'Unidad' if model matches
            // Wait, component used 'tipoGananciaUnit' in some places? No, model says 'tipoGananciaUnidad'.
            // Component line 357: targetForm.get('tipoGananciaUnidad')
            // Component line 775: 'tipoGananciaUnit'?? Wait, let me check the component again.
            // Ah, in `calculatePrice` method of component (line 775) it was using 'tipoGananciaUnit'.
            // BUT the form group definition (line 279) says 'tipoGananciaUnidad'.
            // So the original component code might have had a bug or 'tipoGananciaUnit' was mapped correctly?
            // Let's assume the form control name is 'tipoGananciaUnidad' as per factory.

            tipoGananciaCtrl = type === 'standard' ? 'tipoGananciaUnidad' : 'gananciaUnidadMin'; // Wait, let's look at factory.
            // Factory: tipoGananciaUnidad, gananciaUnidad, gananciaUnidadMin.
            // Original Component: calculatePrice used 'tipoGananciaUnit' ??
            // Let's look at original `calculatePrice` (line 764).
            // It used `tipoGananciaUnit`?
            // Line 775: `tipoGananciaCtrl = type === 'standard' ? 'tipoGananciaUnit' : 'tipoGananciaUnitMin';`
            // But form keys are `tipoGananciaUnidad`. `tipoGananciaUnit` doesn't exist in `createFormGroup`.
            // So `calculatePrice` might have been broken for 'unit' or using wrong keys?
            // Or maybe I misread.
            // Let's stick to the correct keys from Factory:
            // Unit: tipoGananciaUnidad, gananciaUnidad, gananciaUnidadMin.
            // Blister: tipoGananciaBlister, gananciaBlister, gananciaBlisterMin.

            // Wait, `tipoGananciaUnidad` is the control for the TYPE (Fijo/Porcentaje).
            // Is there a `tipoGananciaUnidadMin`? No. Usually the type is shared or only one type per level.
            // In `calculateUnit`, it uses `tipoGananciaUnidad`.
            // So for min price, we assume same type?
            // The original code `calculatePrice` logic seems effectively unused or only for import?
            // It is used in `initializeImportForms` (line 696).
            // And it uses strings 'tipoGananciaUnit'.
            // If I fix this refactor, I should use the correct keys.

            tipoGananciaCtrl = 'tipoGananciaUnidad';
            valorGananciaCtrl = type === 'standard' ? 'gananciaUnidad' : 'gananciaUnidadMin';
            targetPriceCtrl = type === 'standard' ? 'precioVentaUnitario' : 'precioUnitarioMin';

        } else if (level === 'blister') {
            cost = baseCost * (form.get('factorBlister')?.value || 1);
            tipoGananciaCtrl = 'tipoGananciaBlister';
            valorGananciaCtrl = type === 'standard' ? 'gananciaBlister' : 'gananciaBlisterMin';
            targetPriceCtrl = type === 'standard' ? 'precioVentaBlister' : 'precioBlisterMin';
        } else if (level === 'caja') {
            let factorRealCaja = form.get('factorCaja')?.value || 1;
            if (form.get('manejaBlister')?.value) {
                factorRealCaja *= (form.get('factorBlister')?.value || 1);
            }
            cost = baseCost * factorRealCaja;
            tipoGananciaCtrl = 'tipoGananciaCaja';
            valorGananciaCtrl = type === 'standard' ? 'gananciaCaja' : 'gananciaCajaMin';
            targetPriceCtrl = type === 'standard' ? 'precioVentaCaja' : 'precioCajaMin';
        }

        const tipoGanancia = form.get(tipoGananciaCtrl)?.value;
        const valGananciaRaw = form.get(valorGananciaCtrl)?.value;

        if (valGananciaRaw === null || valGananciaRaw === '') return;

        const valorGanancia = parseFloat(valGananciaRaw);
        if (cost <= 0) return;

        let result;
        if (tipoGanancia === 'PORCENTAJE') {
            result = PricingUtils.calcularPrecioVentaConIgvPorPorcentaje(cost, valorGanancia);
        } else {
            // Logic for FIJO from original component
            const precioVenta = cost + valorGanancia;
            result = { precioVenta: precioVenta };
        }

        if (result && result.precioVenta) {
            form.patchValue({ [targetPriceCtrl]: result.precioVenta.toFixed(2) }, { emitEvent: false });
        }
    }
}
