import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';
import { DocumentoFormato, Modulo, Plantilla, TipoDocumentoPlantilla } from '../../../models/documento.model';
import { PuntoDocumentoService } from '../../../services/punto-documento.service';

@Component({
  selector: 'app-copiar-documento-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, PrimaryButtonComponent],
  template: `
    <div class="p-6">
      <div class="mb-6 p-4 bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 rounded-2xl">
        <h3 class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3">Información del Diseño Original</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-500 flex items-center justify-center">
              <i class="fas fa-shopping-cart"></i>
            </div>
            <div class="flex flex-col">
              <span class="text-[10px] font-bold text-slate-400 uppercase">Módulo</span>
              <span class="text-sm font-black text-slate-700 dark:text-slate-200">{{ moduloLabel }}</span>
            </div>
          </div>
          
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center">
              <i class="fas fa-ruler-combined"></i>
            </div>
            <div class="flex flex-col">
              <span class="text-[10px] font-bold text-slate-400 uppercase">Formato</span>
              <span class="text-sm font-black text-slate-700 dark:text-slate-200">{{ formatoLabel }}</span>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-500 flex items-center justify-center">
              <i class="fas fa-compass"></i>
            </div>
            <div class="flex flex-col">
              <span class="text-[10px] font-bold text-slate-400 uppercase">Orientación</span>
              <span class="text-sm font-black text-slate-700 dark:text-slate-200">{{ plantilla.orientacion }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="mb-6">
        <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2 uppercase tracking-wider">
          1. Nombre de la Plantilla
        </label>
        <input 
          type="text" 
          [(ngModel)]="nombre"
          placeholder="Ej: Factura Electrónica Pro"
          class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
        >
      </div>
      <div class="mb-6">
        <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-3 uppercase tracking-wider">
          2. Tipo de Documento
        </label>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          @for (tipo of tiposFiltrados; track tipo.id) {
            <div 
              (click)="seleccionarTipo(tipo.id)"
              [class.border-indigo-500]="tipoSeleccionado() === tipo.id"
              [class.bg-indigo-50]="tipoSeleccionado() === tipo.id"
              [class.dark:bg-indigo-900/20]="tipoSeleccionado() === tipo.id"
              class="flex flex-col items-center p-4 rounded-xl border border-slate-200 dark:border-slate-700 cursor-pointer transition-all hover:border-indigo-400 bg-white dark:bg-slate-800"
            >
              <i [class]="'fas ' + tipo.icon + ' text-lg mb-2 ' + (tipoSeleccionado() === tipo.id ? 'text-indigo-500' : 'text-slate-400')"></i>
              <span class="text-xs font-bold text-slate-700 dark:text-slate-300">{{ tipo.label }}</span>
            </div>
          }
        </div>
      </div>



      <div class="flex justify-end gap-3 mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
        <button 
          (click)="onCancel.emit()"
          class="px-6 py-2.5 rounded-xl text-slate-600 dark:text-slate-400 font-bold hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        >
          Cancelar
        </button>
        <app-primary-button 
          label="Copiar y Empezar" 
          [disabled]="!isValid"
          (btnClick)="continuar()"
        >
        </app-primary-button>
      </div>
    </div>
  `
})
export class CopiarDocumentoModalComponent implements OnInit {
  @Input() plantilla!: Plantilla;
  @Output() onCancel = new EventEmitter<void>();
  @Output() onContinue = new EventEmitter<any>();

  private puntoDocService = inject(PuntoDocumentoService);

  tipoSeleccionado = signal<TipoDocumentoPlantilla | null>(null);
  nombre = '';
  tiposDocumentoDB = signal<any[]>([]);

  modulos = [
    { id: 'VENTA' as Modulo, label: 'Ventas' },
    { id: 'COMPRA' as Modulo, label: 'Compras' },
    { id: 'COTIZACION' as Modulo, label: 'Cotizaciones' },
    { id: 'NOTA_VENTA' as Modulo, label: 'Notas de Venta' },
    { id: 'INVENTARIO' as Modulo, label: 'Inventario' },
    { id: 'INGRESOS_DIVERSOS' as Modulo, label: 'Ingresos Diversos' },
    { id: 'SALIDAS_DIVERSAS' as Modulo, label: 'Salidas Diversas' }
  ];

  tiposBase = [
    { id: 'FACTURA' as TipoDocumentoPlantilla, label: 'Factura', icon: 'fa-file-invoice-dollar' },
    { id: 'BOLETA' as TipoDocumentoPlantilla, label: 'Boleta', icon: 'fa-receipt' },
    { id: 'NOTA_CREDITO' as TipoDocumentoPlantilla, label: 'N. Crédito', icon: 'fa-file-contract' },
    { id: 'GUIA_REMISION' as TipoDocumentoPlantilla, label: 'Guía Rem.', icon: 'fa-shipping-fast' },
    { id: 'RECIBO' as TipoDocumentoPlantilla, label: 'Recibo', icon: 'fa-file-invoice' },
    { id: 'CT' as TipoDocumentoPlantilla, label: 'Cotización', icon: 'fa-file-alt' },
    { id: 'NV' as TipoDocumentoPlantilla, label: 'Nota de Venta', icon: 'fa-file-signature' },
    { id: 'INGRESOS_DIVERSOS' as TipoDocumentoPlantilla, label: 'I. Diverso', icon: 'fa-sync' },
    { id: 'SALIDAS_DIVERSAS' as TipoDocumentoPlantilla, label: 'S. Diverso', icon: 'fa-sync' },
    { id: 'DIVERSOS' as TipoDocumentoPlantilla, label: 'M. Diverso', icon: 'fa-sync' }
  ];

  get moduloLabel(): string {
    const mod = this.plantilla.modulo;
    const found = this.modulos.find(m => m.id === mod);
    return found ? found.label : mod;
  }

  get formatoLabel(): string {
    return this.plantilla.formato?.nombre || 'Personalizado';
  }

  get tiposFiltrados() {
    const mod = this.plantilla.modulo;
    if (!mod) return [];

    return this.tiposDocumentoDB()
      .filter(t => {
        if (mod === 'VENTA') return t.ventas === '1' && t.tipoDoc !== 'NV';
        if (mod === 'NOTA_VENTA') return t.tipoDoc === 'NV';
        if (mod === 'COMPRA') return t.compras === '1';
        if (mod === 'INVENTARIO') return t.clinic === '1';
        if (mod === 'INGRESOS_DIVERSOS' || mod === 'SALIDAS_DIVERSAS' || mod === 'DIVERSOS') return true;
        return true;
      })
      .map(t => {
        const base = this.tiposBase.find(b => b.id === t.tipoDoc);
        return {
          id: t.tipoDoc,
          label: t.nombre,
          icon: base ? base.icon : 'fa-file-alt',
          mod: mod
        };
      });
  }

  get isValid() {
    return this.tipoSeleccionado() && this.nombre.trim().length > 3;
  }

  ngOnInit(): void {
    const tipo = this.plantilla.tipoDocumento;
    this.tipoSeleccionado.set(typeof tipo === 'object' ? (tipo as any).tipoDoc : tipo);
    this.nombre = `Copia de ${this.plantilla.nombre}`;

    this.puntoDocService.obtenerTiposDocumentos().subscribe({
      next: (res) => {
        if (res.success) {
          this.tiposDocumentoDB.set(res.data);
        }
      }
    });
  }

  seleccionarTipo(id: TipoDocumentoPlantilla) {
    this.tipoSeleccionado.set(id);
  }

  continuar() {
    if (this.isValid) {
      this.onContinue.emit({
        nombre: this.nombre,
        modulo: this.plantilla.modulo,
        tipoDocumento: this.tipoSeleccionado(),
        formatoId: this.plantilla.formato?.id || null,
        orientacion: this.plantilla.orientacion,
        copyId: this.plantilla.id
      });
    }
  }
}
