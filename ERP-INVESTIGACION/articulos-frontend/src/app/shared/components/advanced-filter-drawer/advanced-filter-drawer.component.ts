import { Component, EventEmitter, Input, Output, signal, effect, OnInit, OnChanges, SimpleChanges, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormInputComponent } from '../forms/form-input/form-input.component';
import { SearchableSelectComponent } from '../searchable-select/searchable-select.component';

export interface AdvancedFilters {
  fechaDesde: string;
  fechaHasta: string;
  sucursalId: number | 'TODOS';
  estado: string;
  tipoMovimiento: string;
  tipoDocumento: string;
  serie: string;
  numeroComprobante: string;
  numeroDesde?: string;
  numeroHasta?: string;
  vendedorId: number | 'TODOS';
  puntoVentaId: number | 'TODOS';
  condicionPagoId: number | 'TODOS';
  categoriaId?: number | 'TODOS';
  esGenerico?: boolean | 'TODOS';
  manejaLotes?: boolean | 'TODOS';
  tipoAfectacion?: string | 'TODOS';
  mes?: number | 'TODOS';
  anio?: number | 'TODOS';
}

@Component({
  selector: 'app-advanced-filter-drawer',
  standalone: true,
  imports: [CommonModule, FormsModule, FormInputComponent, SearchableSelectComponent],
  templateUrl: './advanced-filter-drawer.component.html'
})
export class AdvancedFilterDrawerComponent implements OnInit, OnChanges {
  // Inputs options
  @Input() sucursalOptions: any[] = [{ label: 'Todas', value: 'TODOS' }];
  @Input() vendedorOptions: any[] = [{ label: 'Todos los Vendedores', value: 'TODOS' }];
  @Input() puntoVentaOptions: any[] = [{ label: 'Todos', value: 'TODOS' }];
  @Input() condicionPagoOptions: any[] = [{ label: 'Todas las Condiciones', value: 'TODOS' }];
  @Input() estadoOptions: any[] = [
    { label: 'Todos', value: 'TODOS' },
    { label: 'Activo / Vigente', value: 'ACTIVO' },
    { label: 'Borrador (Cot.)', value: 'COTIZACION' },
    { label: 'Anulado', value: 'ANULADO' }
  ];
  @Input() tipoDocumentoOptions: any[] = [
    { label: 'Todos', value: 'TODOS' },
    { label: 'Factura', value: 'Factura' },
    { label: 'Boleta', value: 'Boleta' }
  ];

  @Input() initialSucursalId?: number | 'TODOS';
  @Input() initialPuntoVentaId?: number | 'TODOS';
  @Input() initialMes?: number | 'TODOS';
  @Input() initialAnio?: number | 'TODOS';

  // Visibility toggles
  @Input() showClasificacion = true;
  @Input() showFechas = true;
  @Input() showDocumento = true;
  @Input() showOperacion = true; // Vendedor y Condición de Pago
  @Input() showUbicacion = true; // Sucursal y Punto de Venta
  @Input() showEstado = true;
  @Input() showCatalogo = false;
  @Input() showRangoNumeros = false;
  @Input() showMesAnio = false; // Mes y Año
  @Input() showPuntoVenta = true;
  @Input() hideTrigger = false;

  @Input() mesOptions: any[] = [];
  @Input() anioOptions: any[] = [];

  // Búsqueda Global Integrada
  @Input() showGlobalSearch = false;
  @Input() searchPlaceholder = 'Buscar...';
  @Input() globalSearchTerm = '';
  @Output() onGlobalSearch = new EventEmitter<string>();

  // Opciones Catálogo
  @Input() categoriaOptions: any[] = [{ label: 'Todas las Categorías', value: 'TODOS' }];
  @Input() marcaOptions: any[] = [
    { label: 'Todos', value: 'TODOS' },
    { label: 'Marca', value: false },
    { label: 'Genérico', value: true }
  ];
  @Input() booleanOptions: any[] = [
    { label: 'Todos', value: 'TODOS' },
    { label: 'Sí', value: true },
    { label: 'No', value: false }
  ];
  @Input() afectacionOptions: any[] = [
    { label: 'Todas', value: 'TODOS' },
    { label: 'Gravado (IGV 18%)', value: 'GRAVADO' },
    { label: 'Exonerado', value: 'EXONERADO' },
    { label: 'Inafecto', value: 'INAFECTO' }
  ];

  // Outputs
  @Output() onApplyFilters = new EventEmitter<AdvancedFilters>();
  @Output() activeFiltersCount = new EventEmitter<number>();

  @ViewChild('drawerBody') drawerBody?: ElementRef;

  // UI State
  isOpen = signal(false);

  // Filter State
  filtros = signal<AdvancedFilters>({
    fechaDesde: '',
    fechaHasta: '',
    sucursalId: 'TODOS',
    estado: 'TODOS',
    tipoMovimiento: 'TODOS',
    tipoDocumento: 'TODOS',
    serie: '',
    numeroComprobante: '',
    numeroDesde: '',
    numeroHasta: '',
    vendedorId: 'TODOS',
    puntoVentaId: 'TODOS',
    condicionPagoId: 'TODOS',
    categoriaId: 'TODOS',
    esGenerico: 'TODOS',
    manejaLotes: 'TODOS',
    tipoAfectacion: 'TODOS',
    mes: 'TODOS',
    anio: 'TODOS'
  });

  // Active tags to render
  activeTags = signal<{ icon: string, text: string, type: keyof AdvancedFilters, colorTheme: string }[]>([]);

  private applyTimeout?: ReturnType<typeof setTimeout>;
  radioGroup = 'tipo_mov_' + Math.random().toString(36).substring(2, 11);

  constructor() {
    // Whenever filters change, update the tags and the count
    effect(() => {
      this.renderTags();
    }, { allowSignalWrites: true });
  }

  ngOnInit() {
    if (this.initialSucursalId !== undefined) {
      this.filtros.update(f => ({ ...f, sucursalId: this.initialSucursalId! }));
    }
    if (this.initialPuntoVentaId !== undefined) {
      this.filtros.update(f => ({ ...f, puntoVentaId: this.initialPuntoVentaId! }));
    }
    if (this.initialMes !== undefined) {
      this.filtros.update(f => ({ ...f, mes: this.initialMes! }));
    }
    if (this.initialAnio !== undefined) {
      this.filtros.update(f => ({ ...f, anio: this.initialAnio! }));
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['initialSucursalId'] && !changes['initialSucursalId'].isFirstChange()) {
      const newVal = changes['initialSucursalId'].currentValue;
      if (newVal !== undefined) {
        this.filtros.update(f => ({ ...f, sucursalId: newVal }));
      }
    }
    
    if (changes['initialPuntoVentaId'] && !changes['initialPuntoVentaId'].isFirstChange()) {
      const newVal = changes['initialPuntoVentaId'].currentValue;
      if (newVal !== undefined) {
        this.filtros.update(f => ({ ...f, puntoVentaId: newVal }));
      }
    }
    
    if (changes['sucursalOptions'] || changes['vendedorOptions'] || changes['puntoVentaOptions'] || changes['initialSucursalId'] || changes['initialPuntoVentaId'] || changes['categoriaOptions']) {
      this.renderTags();
    }
  }

  onGlobalSearchChange(term: string) {
    this.globalSearchTerm = term;
    this.onGlobalSearch.emit(term);
  }

  openDrawer() {
    this.isOpen.set(true);
    setTimeout(() => {
      if (this.drawerBody) {
        const focusableElements = this.drawerBody.nativeElement.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        if (focusableElements.length > 0) {
          focusableElements[0].focus();
        }
      }
    }, 100);
  }

  closeDrawer() {
    this.isOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  onKeydownHandler() {
    if (this.isOpen()) {
      this.closeDrawer();
    }
  }

  @HostListener('document:keydown.tab', ['$event'])
  onKeydownTab(event: any) {
    if (this.isOpen() && this.drawerBody) {
      const focusableElements = this.drawerBody.nativeElement.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (focusableElements.length === 0) return;

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];

      // Si el foco está fuera del drawer, forzarlo adentro
      if (!this.drawerBody.nativeElement.contains(document.activeElement)) {
        firstElement.focus();
        event.preventDefault();
        return;
      }

      if (event.shiftKey) { // Shift + Tab (hacia atrás)
        if (document.activeElement === firstElement) {
          lastElement.focus();
          event.preventDefault();
        }
      } else { // Tab (hacia adelante)
        if (document.activeElement === lastElement) {
          firstElement.focus();
          event.preventDefault();
        }
      }
    }
  }

  @HostListener('document:keydown', ['$event'])
  onKeydownShortcut(event: KeyboardEvent) {
    // Alt + F para abrir/cerrar filtros
    if (event.altKey && event.key.toLowerCase() === 'f') {
      event.preventDefault(); // Evitar comportamientos por defecto del navegador si los hubiera
      if (this.isOpen()) {
        this.closeDrawer();
      } else {
        this.openDrawer();
      }
    }
  }

  applyFilters() {
    clearTimeout(this.applyTimeout);
    this.applyTimeout = setTimeout(() => {
      this.onApplyFilters.emit(this.filtros());
    }, 400); // 400ms debounce
  }

  clearFilters() {
    this.filtros.set({
      fechaDesde: '',
      fechaHasta: '',
      sucursalId: 'TODOS',
      estado: 'TODOS',
      tipoMovimiento: 'TODOS',
      tipoDocumento: 'TODOS',
      serie: '',
      numeroComprobante: '',
      numeroDesde: '',
      numeroHasta: '',
      vendedorId: 'TODOS',
      puntoVentaId: 'TODOS',
      condicionPagoId: 'TODOS',
      categoriaId: 'TODOS',
      esGenerico: 'TODOS',
      manejaLotes: 'TODOS',
      tipoAfectacion: 'TODOS',
      mes: 'TODOS',
      anio: 'TODOS'
    });
    this.applyFilters();
  }

  removeFilter(type: keyof AdvancedFilters) {
    const current = { ...this.filtros() };
    if (type === 'fechaDesde' || type === 'fechaHasta') {
      current.fechaDesde = '';
      current.fechaHasta = '';
    } else if (type === 'serie' || type === 'numeroComprobante') {
      current[type] = '';
    } else if (type === 'numeroDesde' || type === 'numeroHasta') {
      current.numeroDesde = '';
      current.numeroHasta = '';
    } else {
      (current as any)[type] = 'TODOS';
    }
    this.filtros.set(current);
    this.applyFilters();
  }

  setTipoMovimiento(tipo: string) {
    this.updateFilter('tipoMovimiento', tipo);
  }

  @Input() documentosDisponibles: any[] = [];

  updateFilter(field: keyof AdvancedFilters, value: any) {
    let finalValue = value;
    if (value === null || value === undefined || value === '') {
      if (field === 'fechaDesde' || field === 'fechaHasta' || field === 'serie' || field === 'numeroComprobante' || field === 'numeroDesde' || field === 'numeroHasta') {
        finalValue = '';
      } else {
        finalValue = 'TODOS';
      }
    }
    
    // Auto-fill serie based on selected tipoDocumento
    if (field === 'tipoDocumento' && finalValue !== 'TODOS' && this.documentosDisponibles.length > 0) {
       let tipoDocVal = finalValue;
       let serieVal = null;
       if (typeof finalValue === 'string' && finalValue.includes('|')) {
           const parts = finalValue.split('|');
           tipoDocVal = parts[0];
           serieVal = parts[1];
       }

       const doc = this.documentosDisponibles.find((d: any) => d.tipoDoc === tipoDocVal && (!serieVal || d.serie === serieVal));
       if (doc && doc.serie) {
           this.filtros.update(f => ({ ...f, [field]: finalValue, serie: doc.serie }));
           this.applyFilters();
           return;
       }
    }

    this.filtros.update(f => ({ ...f, [field]: finalValue }));
    this.applyFilters();
  }

  private renderTags() {
    const f = this.filtros();
    const tags: { icon: string, text: string, type: keyof AdvancedFilters, colorTheme: string }[] = [];
    let count = 0;

    // Tipo Movimiento
    if (f.tipoMovimiento !== 'TODOS') {
      count++;
      let lbl = 'Compra', theme = 'blue';
      if (f.tipoMovimiento === 'VENTA') { lbl = 'Venta'; theme = 'emerald'; }
      if (f.tipoMovimiento === 'CREDITO_DEBITO') { lbl = 'Crédito/Débito'; theme = 'red'; }
      if (f.tipoMovimiento === 'OTROS') { lbl = 'Otros Mov.'; theme = 'indigo'; }
      tags.push({ icon: 'fa-tags', text: `Tipo: ${lbl}`, type: 'tipoMovimiento', colorTheme: theme });
    }

    // Fechas
    if (f.fechaDesde || f.fechaHasta) {
      count++;
      tags.push({ icon: 'fa-calendar-days', text: `${f.fechaDesde || 'Inicio'} a ${f.fechaHasta || 'Fin'}`, type: 'fechaDesde', colorTheme: 'blue' });
    }

    // Sucursal
    if (f.sucursalId !== 'TODOS') {
      count++;
      const op = this.sucursalOptions.find(o => o.value == f.sucursalId);
      tags.push({ icon: 'fa-store', text: op ? op.label : String(f.sucursalId), type: 'sucursalId', colorTheme: 'slate' });
    }

    // Punto Venta
    if (f.puntoVentaId !== 'TODOS') {
      count++;
      const op = this.puntoVentaOptions.find(o => o.value == f.puntoVentaId);
      tags.push({ icon: 'fa-cash-register', text: `Caja: ${op ? op.label : String(f.puntoVentaId)}`, type: 'puntoVentaId', colorTheme: 'slate' });
    }

    // Tipo Doc
    if (f.tipoDocumento !== 'TODOS') {
      count++;
      const op = this.tipoDocumentoOptions.find(o => o.value === f.tipoDocumento);
      tags.push({ icon: 'fa-file-invoice', text: `Doc: ${op ? op.label : f.tipoDocumento}`, type: 'tipoDocumento', colorTheme: 'slate' });
    }

    // Serie
    if (f.serie && f.serie.trim()) {
      count++;
      tags.push({ icon: 'fa-font', text: `Serie: ${f.serie.toUpperCase()}`, type: 'serie', colorTheme: 'slate' });
    }

    // Numero
    if (f.numeroComprobante && !this.showRangoNumeros) {
      count++;
      tags.push({ icon: 'fa-hashtag', text: `Nº: ${f.numeroComprobante}`, type: 'numeroComprobante', colorTheme: 'slate' });
    }

    // Rango Numeros
    if (this.showRangoNumeros && (f.numeroDesde || f.numeroHasta)) {
      count++;
      tags.push({ icon: 'fa-hashtag', text: `Nº: ${f.numeroDesde || '*'} - ${f.numeroHasta || '*'}`, type: 'numeroDesde', colorTheme: 'slate' });
    }

    // Vendedor
    if (f.vendedorId !== 'TODOS') {
      count++;
      const op = this.vendedorOptions.find(o => o.value === f.vendedorId);
      tags.push({ icon: 'fa-user', text: op ? op.label : String(f.vendedorId), type: 'vendedorId', colorTheme: 'indigo' });
    }

    // Pago
    if (f.condicionPagoId !== 'TODOS') {
      count++;
      const op = this.condicionPagoOptions.find(o => o.value === f.condicionPagoId);
      tags.push({ icon: 'fa-money-bill', text: op ? op.label : String(f.condicionPagoId), type: 'condicionPagoId', colorTheme: 'emerald' });
    }

    // Estado
    if (f.estado !== 'TODOS') {
      count++;
      const isVigente = f.estado === 'ACTIVO';
      tags.push({ icon: isVigente ? 'fa-circle-check' : 'fa-circle-xmark', text: f.estado, type: 'estado', colorTheme: isVigente ? 'emerald' : 'red' });
    }

    // Catálogo
    if (f.categoriaId && f.categoriaId !== 'TODOS') {
      count++;
      const op = this.categoriaOptions.find(o => o.value == f.categoriaId);
      tags.push({ icon: 'fa-tags', text: op ? op.label : 'Categoría', type: 'categoriaId', colorTheme: 'orange' });
    }
    if (f.esGenerico !== undefined && f.esGenerico !== 'TODOS') {
      count++;
      const isGenericoBool = f.esGenerico === true || String(f.esGenerico) === 'true';
      const op = this.marcaOptions.find(o => String(o.value) === String(isGenericoBool));
      tags.push({ icon: 'fa-box', text: op ? op.label : 'Marca', type: 'esGenerico', colorTheme: 'blue' });
    }
    if (f.manejaLotes !== undefined && f.manejaLotes !== 'TODOS') {
      count++;
      const isLotesBool = f.manejaLotes === true || String(f.manejaLotes) === 'true';
      tags.push({ icon: 'fa-layer-group', text: isLotesBool ? 'Con Lotes' : 'Sin Lotes', type: 'manejaLotes', colorTheme: 'emerald' });
    }
    if (f.tipoAfectacion && f.tipoAfectacion !== 'TODOS') {
      count++;
      const op = this.afectacionOptions.find(o => o.value === f.tipoAfectacion);
      tags.push({ icon: 'fa-file-invoice-dollar', text: op ? op.label : f.tipoAfectacion, type: 'tipoAfectacion', colorTheme: 'slate' });
    }
    if (f.mes && f.mes !== 'TODOS') {
      count++;
      const op = this.mesOptions.find(o => o.value == f.mes);
      tags.push({ icon: 'fa-calendar', text: op ? op.label : String(f.mes), type: 'mes', colorTheme: 'blue' });
    }
    if (f.anio && f.anio !== 'TODOS') {
      count++;
      const op = this.anioOptions.find(o => o.value == f.anio);
      tags.push({ icon: 'fa-calendar', text: op ? op.label : String(f.anio), type: 'anio', colorTheme: 'blue' });
    }

    this.activeTags.set(tags);
    this.activeFiltersCount.emit(count);
  }

  getColorClasses(theme: string): string {
    if (theme === 'brand' || theme === 'blue') return 'bg-blue-500/10 border-blue-500/30 text-blue-600 dark:text-blue-400';
    if (theme === 'emerald') return 'bg-emerald-500/10 border-emerald-500/30 text-emerald-600 dark:text-emerald-400';
    if (theme === 'red') return 'bg-red-500/10 border-red-500/30 text-red-600 dark:text-red-400';
    if (theme === 'indigo') return 'bg-indigo-500/10 border-indigo-500/30 text-indigo-600 dark:text-indigo-400';
    if (theme === 'orange') return 'bg-orange-500/10 border-orange-500/30 text-orange-600 dark:text-orange-400';
    return 'bg-slate-100 dark:bg-slate-800 border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-300';
  }
}
