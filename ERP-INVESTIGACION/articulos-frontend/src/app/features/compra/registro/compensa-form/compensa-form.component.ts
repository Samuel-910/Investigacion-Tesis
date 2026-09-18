import { Component, OnInit, signal, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CompraService } from '../../services/compra.service';
import { AlertService } from '../../../../core/services/alert.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-compensa-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './compensa-form.component.html'
})
export class CompensaFormComponent implements OnInit {
  @Input() idCompra: number | null = null;
  @Output() cerrar = new EventEmitter<void>();
  @Output() guardado = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private compraService = inject(CompraService);
  private alertService = inject(AlertService);
  private authService = inject(AuthService);

  form!: FormGroup;
  loading = signal(false);
  compraDatos = signal<any>(null);
  totals = signal({ subtotal: 0, gravada: 0, exonerada: 0, inafecta: 0, igv: 0, totalCalculado: 0, total: 0 });

  ngOnInit() {
    this.initForm();
    if (this.idCompra) {
      this.cargarCompra();
    }
  }

  initForm() {
    this.form = this.fb.group({
      valorVentaGravado: [0],
      valorVentaExonerado: [0],
      valorVentaInafecto: [0],
      igv: [0],
      percepcion: [0],
      ajusteRedondeo: [0],
      total: [0]
    });

    this.form.valueChanges.subscribe(() => {
      this.calculateTotals();
    });
  }

  cargarCompra() {
    this.loading.set(true);
    this.compraService.obtener(this.idCompra!).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.compraDatos.set(res.data);

          this.form.patchValue({
            valorVentaGravado: res.data.valorVentaGravado || 0,
            valorVentaExonerado: res.data.valorVentaExonerado || 0,
            valorVentaInafecto: res.data.valorVentaInafecto || 0,
            igv: res.data.igv || 0,
            percepcion: res.data.percepcion || 0,
            ajusteRedondeo: res.data.ajusteRedondeo || 0,
            total: res.data.total || 0
          }, { emitEvent: false });
          this.calculateTotals();
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.alertService.toast('Error al cargar la compra', 'error');
      }
    });
  }

  calculateTotals() {
    const gravada = Number(this.form.get('valorVentaGravado')?.value) || 0;
    const exonerada = Number(this.form.get('valorVentaExonerado')?.value) || 0;
    const inafecta = Number(this.form.get('valorVentaInafecto')?.value) || 0;
    const igv = Number(this.form.get('igv')?.value) || 0;
    const totalCalculado = Number((gravada + exonerada + inafecta + igv).toFixed(2));

    const percepcion = Number(this.form.get('percepcion')?.value) || 0;
    const ajuste = Number(this.form.get('ajusteRedondeo')?.value) || 0;

    const montoPercepcion = Number((totalCalculado * (percepcion / 100)).toFixed(2));
    const totalGeneral = Number((totalCalculado + montoPercepcion + ajuste).toFixed(2));

    this.totals.set({
      subtotal: totalCalculado,
      gravada,
      exonerada,
      inafecta,
      igv,
      totalCalculado,
      total: totalGeneral
    });
  }

  onGuardar() {
    if (!this.idCompra) return;

    this.loading.set(true);
    const currentData = this.compraDatos();
    const idSucursal = this.authService.getSucursalIdFromToken();

    const data = {
      id: this.idCompra,
      idSucursal: currentData.idSucursal || idSucursal,
      idProveedor: currentData.proveedor.id,
      tipoComprobante: currentData.tipoComprobante,
      serie: currentData.serie,
      correlativo: currentData.correlativo,
      fechaEmision: currentData.fechaEmision,
      condicionPago: currentData.condicionPago,
      moneda: currentData.moneda,
      valorVentaGravado: this.totals().gravada,
      valorVentaExonerado: this.totals().exonerada,
      valorVentaInafecto: this.totals().inafecta,
      igv: this.totals().igv,
      percepcion: Number(this.form.get('percepcion')?.value) || 0,
      ajusteRedondeo: Number(this.form.get('ajusteRedondeo')?.value) || 0,
      total: this.totals().total,
      detalles: (currentData.detalles || []).map((d: any) => ({
        ...d,
        idProducto: d.producto?.id || d.idProducto
      })),
      isAjusteManual: true
    };

    this.compraService.actualizar(this.idCompra, data).subscribe({
      next: (res) => {
        if (res.success) {
          this.alertService.toast('Resumen de compra actualizado exitosamente', 'success');
          this.guardado.emit();
        } else {
          this.alertService.toast(res.message || 'Error al actualizar', 'error');
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.alertService.toast('Error de conexión', 'error');
      }
    });
  }

  cancelar() {
    this.cerrar.emit();
  }
}
