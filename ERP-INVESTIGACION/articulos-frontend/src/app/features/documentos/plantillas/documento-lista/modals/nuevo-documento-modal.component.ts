import { Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';
import { SearchableSelectComponent } from '../../../../../shared/components/searchable-select/searchable-select.component';
import { DocumentoFormato, Modulo, TipoDocumentoPlantilla } from '../../../models/documento.model';
import { PuntoDocumentoService } from '../../../services/punto-documento.service';
import { FormatoService } from '../../../services/formato.service';

@Component({
  selector: 'app-nuevo-documento-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, PrimaryButtonComponent, SearchableSelectComponent],
  template: `
    <div class="p-6">
      <div class="mb-8">
        <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-4 uppercase tracking-wider">
          1. Selecciona el Origen (Módulo)
        </label>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          @for (mod of modulos; track mod.id) {
            <div 
              (click)="seleccionarModulo(mod.id)"
              [class.border-blue-500]="moduloSeleccionado() === mod.id"
              [class.bg-blue-50]="moduloSeleccionado() === mod.id"
              [class.dark:bg-blue-900/20]="moduloSeleccionado() === mod.id"
              class="relative flex flex-col items-center p-5 rounded-2xl border-2 border-slate-100 dark:border-slate-700 cursor-pointer transition-all hover:border-blue-400 hover:shadow-md group bg-white dark:bg-slate-800"
            >
              <div 
                [class.bg-blue-500]="moduloSeleccionado() === mod.id"
                [class.text-white]="moduloSeleccionado() === mod.id"
                class="w-12 h-12 rounded-xl bg-slate-100 dark:bg-slate-700 flex items-center justify-center mb-3 transition-colors group-hover:bg-blue-500 group-hover:text-white"
              >
                <i [class]="'fas ' + mod.icon + ' text-xl'"></i>
              </div>
              <span class="font-bold text-slate-900 dark:text-white">{{ mod.label }}</span>
              <p class="text-xs text-slate-500 text-center mt-1">{{ mod.desc }}</p>
              
              @if (moduloSeleccionado() === mod.id) {
                <div class="absolute top-2 right-2 text-blue-500 animate-bounce-short">
                  <i class="fas fa-check-circle text-lg"></i>
                </div>
              }
            </div>
          }
        </div>
      </div>

      <div class="mb-8 transition-all duration-500" [class.opacity-50]="!moduloSeleccionado()" [class.pointer-events-none]="!moduloSeleccionado()">
        <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-4 uppercase tracking-wider">
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

      <div class="mb-6 transition-all duration-500" [class.opacity-50]="!tipoSeleccionado()" [class.pointer-events-none]="!tipoSeleccionado()">
        <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2 uppercase tracking-wider">
          3. Nombre de la Plantilla
        </label>
        <input 
          type="text" 
          [(ngModel)]="nombre"
          placeholder="Ej: Factura Electrónica Pro"
          class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
        >
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8 transition-all duration-500" [class.opacity-50]="!tipoSeleccionado()" [class.pointer-events-none]="!tipoSeleccionado()">
        <div>
          <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2 uppercase tracking-wider">
            4. Formato
          </label>
          <app-searchable-select 
            [data]="listadoFormatos" 
            placeholder="Seleccionar formato..." 
            bindLabel="nombre" 
            bindValue="id"
            [ngModel]="formatoId()"
            (ngModelChange)="formatoId.set($event)"
            iconBgColor="bg-emerald-500">
            <i icon class="fas fa-ruler-combined text-white text-xs"></i>
          </app-searchable-select>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2 uppercase tracking-wider">
            5. Orientación
          </label>
          <div class="flex p-1 bg-slate-100 dark:bg-slate-700 rounded-xl">
            <button 
              (click)="orientacion.set('Vertical')"
              [class.bg-white]="orientacion() === 'Vertical'"
              [class.dark:bg-slate-800]="orientacion() === 'Vertical'"
              [class.shadow-sm]="orientacion() === 'Vertical'"
              [class.text-blue-600]="orientacion() === 'Vertical'"
              class="flex-1 py-2 text-xs font-bold rounded-lg transition-all"
            >
              Vertical
            </button>
            <button 
              (click)="orientacion.set('Horizontal')"
              [class.bg-white]="orientacion() === 'Horizontal'"
              [class.dark:bg-slate-800]="orientacion() === 'Horizontal'"
              [class.shadow-sm]="orientacion() === 'Horizontal'"
              [class.text-blue-600]="orientacion() === 'Horizontal'"
              class="flex-1 py-2 text-xs font-bold rounded-lg transition-all"
            >
              Horizontal
            </button>
          </div>
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
          label="Comenzar a Diseñar" 
          icon="plus" 
          [disabled]="!isValid"
          (btnClick)="continuar()"
        >
        </app-primary-button>
      </div>
    </div>
  `,
  styles: [`
    .animate-bounce-short {
      animation: bounce 1s infinite;
    }
    @keyframes bounce {
      0%, 100% { transform: translateY(-5%); animation-timing-function: cubic-bezier(0.8,0,1,1); }
      50% { transform: none; animation-timing-function: cubic-bezier(0,0,0.2,1); }
    }
  `]
})
export class NuevoDocumentoModalComponent implements OnInit {
  @Output() onCancel = new EventEmitter<void>();
  @Output() onContinue = new EventEmitter<any>();

  private formatoService = inject(FormatoService);
  private puntoDocService = inject(PuntoDocumentoService);

  moduloSeleccionado = signal<Modulo | null>(null);
  tipoSeleccionado = signal<TipoDocumentoPlantilla | null>(null);
  nombre = '';
  formatoId = signal<number | null>(null);
  orientacion = signal<string>('Vertical');
  listadoFormatos: DocumentoFormato[] = [];
  tiposDocumentoDB = signal<any[]>([]);

  modulos = [
    { id: 'VENTA' as Modulo, label: 'Ventas', icon: 'fa-shopping-cart', desc: 'Facturación y Boletas' },
    { id: 'COMPRA' as Modulo, label: 'Compras', icon: 'fa-truck-loading', desc: 'Recepciones y Ordenes' },
    { id: 'COTIZACION' as Modulo, label: 'Cotizaciones', icon: 'fa-file-invoice-dollar', desc: 'Presupuestos y Cotiz.' },
    { id: 'NOTA_VENTA' as Modulo, label: 'Notas de Venta', icon: 'fa-file-signature', desc: 'Recibos Internos' },
    { id: 'INVENTARIO' as Modulo, label: 'Inventario', icon: 'fa-boxes', desc: 'Guías y Stocks' },
    { id: 'INGRESOS_DIVERSOS' as Modulo, label: 'Ingresos Diversos', icon: 'fa-arrow-alt-circle-right', desc: 'Entradas manuales' },
    { id: 'SALIDAS_DIVERSAS' as Modulo, label: 'Salidas Diversas', icon: 'fa-arrow-alt-circle-left', desc: 'Salidas manuales' }
  ];

  tiposBase = [
    { id: 'FACTURA' as TipoDocumentoPlantilla, label: 'Factura', icon: 'fa-file-invoice-dollar', mod: 'VENTA' },
    { id: 'BOLETA' as TipoDocumentoPlantilla, label: 'Boleta', icon: 'fa-receipt', mod: 'VENTA' },
    { id: 'NOTA_CREDITO' as TipoDocumentoPlantilla, label: 'N. Crédito', icon: 'fa-file-contract', mod: 'VENTA' },
    { id: 'GUIA_REMISION' as TipoDocumentoPlantilla, label: 'Guía Rem.', icon: 'fa-shipping-fast', mod: 'INVENTARIO' },
    { id: 'RECIBO' as TipoDocumentoPlantilla, label: 'Recibo', icon: 'fa-file-invoice', mod: 'COMPRA' },
    { id: 'CT' as TipoDocumentoPlantilla, label: 'Cotización', icon: 'fa-file-alt', mod: 'COTIZACION' },
    { id: 'NV' as TipoDocumentoPlantilla, label: 'Nota de Venta', icon: 'fa-file-signature', mod: 'NOTA_VENTA' },
    { id: 'INGRESOS_DIVERSOS' as TipoDocumentoPlantilla, label: 'I. Diverso', icon: 'fa-sync', mod: 'INGRESOS_DIVERSOS' },
    { id: 'SALIDAS_DIVERSAS' as TipoDocumentoPlantilla, label: 'S. Diverso', icon: 'fa-sync', mod: 'SALIDAS_DIVERSAS' },
    { id: 'DIVERSOS' as TipoDocumentoPlantilla, label: 'M. Diverso', icon: 'fa-sync', mod: 'DIVERSOS' }
  ];

  get tiposFiltrados() {
    const mod = this.moduloSeleccionado();
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
    return this.moduloSeleccionado() && this.tipoSeleccionado() && this.nombre.trim().length > 3 && this.formatoId();
  }

  ngOnInit(): void {
    this.cargarFormatos();
  }

  cargarFormatos(): void {
    this.formatoService.listar().subscribe({
      next: (res: any) => {
        if (res.success) {
          this.listadoFormatos = res.data;
        }
      }
    });

    this.puntoDocService.obtenerTiposDocumentos().subscribe({
      next: (res) => {
        if (res.success) {
          this.tiposDocumentoDB.set(res.data);
        }
      }
    });
  }

  seleccionarModulo(id: Modulo) {
    this.moduloSeleccionado.set(id);
    this.tipoSeleccionado.set(null);
  }

  seleccionarTipo(id: TipoDocumentoPlantilla) {
    this.tipoSeleccionado.set(id);
  }

  continuar() {
    if (this.isValid) {
      this.onContinue.emit({
        nombre: this.nombre,
        modulo: this.moduloSeleccionado(),
        tipoDocumento: this.tipoSeleccionado(),
        formatoId: this.formatoId(),
        orientacion: this.orientacion(),
        copyId: null
      });
    }
  }
}
