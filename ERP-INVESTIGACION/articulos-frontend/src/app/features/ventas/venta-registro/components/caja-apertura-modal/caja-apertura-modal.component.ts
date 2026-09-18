import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { CajaChicaService } from '../../../../../core/services/caja-chica.service';
import { AlertService } from '../../../../../core/services/alert.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-caja-apertura-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  template: `
    <app-modal [isOpen]="isOpen" title="Apertura de Caja Chica" (modalClose)="close()" iconType="info" [showFooter]="true">
      <div class="space-y-4">
        <div class="bg-indigo-50 dark:bg-indigo-900/20 p-4 rounded-xl border border-indigo-100 dark:border-indigo-800">
          <p class="text-sm text-indigo-700 dark:text-indigo-300">
            Para realizar ventas, es necesario tener una caja chica abierta para su punto de venta.
          </p>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">Nombre de la Caja / Punto</label>
          <input type="text" [(ngModel)]="cajaNombre" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-indigo-500 outline-none transition-all" placeholder="Ej: CAJA PRINCIPAL">
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-medium text-slate-700 dark:text-slate-300">Saldo Inicial (S/)</label>
          <div class="relative">
            <span class="absolute left-4 top-3.5 text-slate-400 font-medium">S/</span>
            <input type="number" [(ngModel)]="saldoInicial" class="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-indigo-500 outline-none transition-all" placeholder="0.00">
          </div>
        </div>
      </div>

      <div footer class="flex justify-end gap-3 w-full">
        <button (click)="close()" class="px-6 py-2.5 rounded-xl text-slate-600 dark:text-slate-400 font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all">
          Cancelar
        </button>
        <button (click)="confirmarApertura()" [disabled]="cargando || !cajaNombre" class="px-8 py-2.5 rounded-xl bg-indigo-600 text-white font-semibold shadow-lg shadow-indigo-200 dark:shadow-none hover:bg-indigo-700 disabled:opacity-50 transition-all">
          {{ cargando ? 'Abriendo...' : 'Abrir Caja' }}
        </button>
      </div>
    </app-modal>
  `
})
export class CajaAperturaModalComponent {
  @Input() isOpen = false;
  @Output() modalClose = new EventEmitter<boolean>();

  private cajaChicaService = inject(CajaChicaService);
  private alertService = inject(AlertService);
  private authService = inject(AuthService);

  cajaNombre = 'CAJA PRINCIPAL';
  saldoInicial = 0;
  cargando = false;

  close() {
    this.modalClose.emit(false);
  }

  confirmarApertura() {
    this.cargando = true;
    const sucursalId = this.authService.getSucursalIdFromToken();
    const puntoId = this.authService.getPuntoIdFromToken();

    if (!sucursalId || !puntoId) {
      this.alertService.error('No se pudo identificar la sucursal o punto del usuario');
      this.cargando = false;
      return;
    }

    this.cajaChicaService.abrirCaja({
      nombre: this.cajaNombre,
      idSucursal: Number(sucursalId),
      idPuntoVenta: Number(puntoId),
      saldoInicial: this.saldoInicial
    }).subscribe({
      next: (res) => {
        this.alertService.success('Caja abierta correctamente');
        this.modalClose.emit(true);
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.alertService.error('Error al abrir caja: ' + (err.error?.message || 'Error desconocido'));
        this.cargando = false;
      }
    });
  }
}
