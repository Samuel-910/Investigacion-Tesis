import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter, TemplateRef } from '@angular/core';

export interface Columna {
  field: string;
  header: string;
  tipo?:
  | 'order-status'
  | 'rango-critico'
  | 'rango-normal'
  | 'date'
  | 'text'
  | 'badge'
  | 'index'
  | 'area-badge'
  | 'nombre-exam'
  | 'area-info'
  | 'currency'
  | 'tiempo-entrega'
  | 'nombre_nivel'
  | 'status'
  | 'venta-status'
  | 'multi-date'
  | 'object'
  | 'list-badges'
  | 'custom-button-action'
  | 'custom-button-action'
  | 'expiration-clock'
  | 'logo'
  | 'layered-info'
  | 'sino-na'
  | 'currency-status'
  | 'caja-status'
  | 'movimiento-status';
  subField: string[];
  badgeConfig?: {
    container?: string;
    from?: string; // Color inicial
    to?: string; // Color final
    text?: string; // Color de texto
    border?: string; // Color de borde
  };
  actionLabel?: string;
  actionIcon?: string;
  actionClass?: string;
  layeredConfig?: LayeredInfo[];
}

export interface LayeredInfo {
  field: string;
  label?: string;
  icon?: string; // Class fontawesome or emoji char
  class?: string;
  suffix?: string;
  format?: 'currency' | 'number' | 'text' | 'date' | 'days-remaining'; // Agregamos days-remaining
  showIf?: string; // Optional: field name to check truthiness before showing
}

@Component({
  selector: 'app-tabla-general',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tabla-general.component.html',
})
export class TablaGeneralComponent {
  @Input() loading: boolean = false;
  @Input() datos: any[] = [];
  @Input() columnas: Columna[] = [];
  @Input() tituloAcciones: string = 'Acciones';
  @Input() showLinkAction: boolean = false;
  @Input() linkActionLabel: string = 'Ver Detalles';
  @Input() linkActionIcon: string = 'fa-eye';
  @Input() currentPage: number = 0;
  @Input() pageSize: number = 10;
  @Input() showEdit: boolean = true;
  @Input() editCondition?: (item: any) => boolean;
  @Input() showDelete: boolean = true;
  @Input() showActions: boolean = true;
  @Input() showSubExams: boolean = false;
  @Input() showDetallesExamenes: boolean = false;
  @Input() showViewDetails: boolean = false;
  @Input() showCreateNivel: boolean = false;
  @Input() showConfig: boolean = false;
  @Input() showPrint: boolean = false;
  @Input() showDownloadPdf: boolean = false;
  @Input() onlyPrintValidated: boolean = false;
  @Input() showCustom: boolean = false;
  @Input() customIcon: string = 'fa-circle-info';
  @Input() customTooltip: string = 'Acción';
  @Input() editIcon: string = '';
  @Input() editTooltip: string = 'Editar';
  @Input() deleteIcon: string = '';
  @Input() deleteTooltip: string = 'Eliminar';
  @Input() accionesTemplate: TemplateRef<any> | null = null;

  @Input() selectable: boolean = false;
  @Input() selectedItems: any[] = [];
  
  @Output() onNivel = new EventEmitter<any>();
  @Output() onConfig = new EventEmitter<any>();
  @Output() onEdit = new EventEmitter<any>();
  @Output() onDelete = new EventEmitter<any>();
  @Output() onCustom = new EventEmitter<any>();
  @Output() onView = new EventEmitter<any>();
  @Output() viewSubExams = new EventEmitter<any>();
  @Output() viewdetalles = new EventEmitter<any>();
  @Output() onLinkAction = new EventEmitter<any>();
  @Output() onLinkClick = new EventEmitter<any>();
  @Output() viewDetails = new EventEmitter<any>();
  @Output() onPrint = new EventEmitter<any>();
  @Output() onDownloadPdf = new EventEmitter<any>();
  @Output() selectionChange = new EventEmitter<any[]>();

  toggleSelection(item: any): void {
    if (!this.selectedItems) {
      this.selectedItems = [];
    }
    const index = this.selectedItems.findIndex((s) => s === item);
    if (index > -1) {
      this.selectedItems.splice(index, 1);
    } else {
      this.selectedItems.push(item);
    }
    this.selectionChange.emit(this.selectedItems);
  }

  toggleSelectAll(event: any): void {
    if (event.target.checked) {
      this.selectedItems = [...this.datos];
    } else {
      this.selectedItems = [];
    }
    this.selectionChange.emit(this.selectedItems);
  }

  isAllSelected(): boolean {
    return this.datos && this.datos.length > 0 && this.selectedItems && this.selectedItems.length === this.datos.length;
  }

  isSelected(item: any): boolean {
    if (!this.selectedItems) return false;
    return this.selectedItems.findIndex((s) => s === item) > -1;
  }

  // Helpers para multi-date
  isMultiDate(text: string): boolean {
    return !!(text && text.includes(' | '));
  }

  getLastDate(text: string): string {
    if (!text) return '';
    const parts = text.split(' | ');
    return parts[parts.length - 1];
  }

  getExtraCount(text: string): number {
    if (!text) return 0;
    return text.split(' | ').length - 1;
  }

  getAllDates(text: string): string[] {
    if (!text) return [];
    return text.split(' | ');
  }

  // Helpers para expiration-clock
  getDaysRemaining(dateString: string): number {
    if (!dateString) return 0;
    const date = new Date(dateString);
    const today = new Date();
    // Reset hours to compare purely dates
    date.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);
    const diffTime = date.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getExpirationColor(days: number, threshold: number = 90): string {
    if (days <= 7) return 'text-red-600';
    if (days <= threshold) return 'text-amber-500';
    return 'text-emerald-600';
  }

  getExpirationBg(days: number, threshold: number = 90): string {
    if (days <= 7) return 'bg-red-50 border-red-200';
    if (days <= threshold) return 'bg-amber-50 border-amber-200';
    return 'bg-emerald-50 border-emerald-200';
  }

  getVisibleLayers(item: any, config: LayeredInfo[] = []): LayeredInfo[] {
    if (!config) return [];
    return config.filter(layer => !layer.showIf || item[layer.showIf]);
  }

  getEstadoConfig(value: any): { label: string, bgClass: string, textClass: string, borderClass: string, icon: string, pulse: boolean } {
    let name = '';
    let valor = -1;

    if (value && typeof value === 'object') {
      name = value.name || '';
      valor = value.valor !== undefined ? value.valor : -1;
    } else if (typeof value === 'string') {
      name = value.toUpperCase();
      if (name === 'A' || name === 'S' || name === '1' || name === 'ABIERTA') name = 'ACTIVO';
      if (name === 'I' || name === 'N' || name === '0' || name === 'CERRADA') name = 'INACTIVO';
      if (name === 'V') name = 'VIGENTE';
      if (name === 'P') name = 'PENDIENTE';
      if (name === 'X') name = 'ANULADO';
    } else if (typeof value === 'number') {
      valor = value;
      if (valor === 1) name = 'ACTIVO';
      if (valor === 0) name = 'INACTIVO';
    } else if (typeof value === 'boolean') {
      name = value ? 'ACTIVO' : 'INACTIVO';
    }

    // Default fallback
    let config = { label: name || 'DESCONOCIDO', bgClass: 'bg-slate-100 dark:bg-slate-700', textClass: 'text-slate-500 dark:text-slate-400', borderClass: 'border-slate-200 dark:border-slate-600', icon: 'fa-circle', pulse: false };

    if (name === 'ACTIVO' || valor === 1) config = { label: 'ACTIVO', bgClass: 'bg-emerald-100 dark:bg-emerald-900/30', textClass: 'text-emerald-800 dark:text-emerald-400', borderClass: 'border-emerald-200 dark:border-emerald-800', icon: 'fa-check-circle', pulse: false };
    else if (name === 'INACTIVO' || valor === 0) config = { label: 'INACTIVO', bgClass: 'bg-slate-100 dark:bg-slate-700', textClass: 'text-slate-500 dark:text-slate-400', borderClass: 'border-slate-200 dark:border-slate-600', icon: 'fa-times-circle', pulse: false };
    else if (name === 'VIGENTE' || valor === 9 || name === 'PROCESADO' || valor === 13) config = { label: 'VIGENTE', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-700 dark:text-emerald-400', borderClass: 'border-emerald-200 dark:border-emerald-800', icon: 'fa-check-circle', pulse: false };
    else if (name === 'ANULADO' || valor === 10 || name === 'ELIMINADO' || valor === 3 || name === 'CANCELADO' || valor === 8) config = { label: 'ANULADO', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-700 dark:text-rose-400', borderClass: 'border-rose-200 dark:border-rose-800', icon: 'fa-times-circle', pulse: false };
    else if (name === 'PENDIENTE' || valor === 2 || name === 'EN_ESPERA' || valor === 14) config = { label: 'PENDIENTE', bgClass: 'bg-amber-100 dark:bg-amber-900/30', textClass: 'text-amber-800 dark:text-amber-400', borderClass: 'border-amber-200 dark:border-amber-800', icon: 'fa-clock', pulse: true };
    else if (name === 'PENDIENTE_ANULACION' || valor === 12) config = { label: 'PENDIENTE ANUL.', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-700 dark:text-amber-400', borderClass: 'border-amber-200 dark:border-amber-800', icon: 'fa-clock', pulse: true };
    else if (name === 'REGISTRADO' || valor === 4) config = { label: 'REGISTRADO', bgClass: 'bg-emerald-100 dark:bg-emerald-900/30', textClass: 'text-emerald-800 dark:text-emerald-400', borderClass: 'border-emerald-200 dark:border-emerald-800', icon: 'fa-save', pulse: false };
    else if (name === 'SOLICITADO' || valor === 5) config = { label: 'SOLICITADO', bgClass: 'bg-blue-100 dark:bg-blue-900/30', textClass: 'text-blue-800 dark:text-blue-400', borderClass: 'border-blue-200 dark:border-blue-800', icon: 'fa-paper-plane', pulse: false };
    else if (name === 'VALIDADO' || valor === 15) config = { label: 'VALIDADO', bgClass: 'bg-orange-100 dark:bg-orange-900/30', textClass: 'text-orange-800 dark:text-orange-400', borderClass: 'border-orange-200 dark:border-orange-800', icon: 'fa-check-double', pulse: false };
    else if (name === 'PAGADO' || valor === 16) config = { label: 'PAGADO', bgClass: 'bg-indigo-100 dark:bg-indigo-900/30', textClass: 'text-indigo-800 dark:text-indigo-400', borderClass: 'border-indigo-200 dark:border-indigo-800', icon: 'fa-money-bill-wave', pulse: false };
    else if (name === 'COTIZACION' || valor === 17) config = { label: 'COTIZACIÓN', bgClass: 'bg-purple-100 dark:bg-purple-900/30', textClass: 'text-purple-800 dark:text-purple-400', borderClass: 'border-purple-200 dark:border-purple-800', icon: 'fa-file-invoice-dollar', pulse: false };
    else if (name === 'ENVIADO' || valor === 6) config = { label: 'ENVIADO', bgClass: 'bg-cyan-100 dark:bg-cyan-900/30', textClass: 'text-cyan-800 dark:text-cyan-400', borderClass: 'border-cyan-200 dark:border-cyan-800', icon: 'fa-truck', pulse: false };
    else if (name === 'RECIBIDO' || valor === 7) config = { label: 'RECIBIDO', bgClass: 'bg-teal-100 dark:bg-teal-900/30', textClass: 'text-teal-800 dark:text-teal-400', borderClass: 'border-teal-200 dark:border-teal-800', icon: 'fa-box-open', pulse: false };
    else if (name === 'EN_PROCESO' || name === 'PROCESANDO') config = { label: 'EN PROCESO', bgClass: 'bg-orange-50 dark:bg-orange-900/30', textClass: 'text-orange-700 dark:text-orange-400', borderClass: 'border-orange-200 dark:border-orange-800', icon: 'fa-flask', pulse: true };
    else if (name === 'ENTREGADO') config = { label: 'ENTREGADO', bgClass: 'bg-purple-50 dark:bg-purple-900/30', textClass: 'text-purple-700 dark:text-purple-400', borderClass: 'border-purple-200 dark:border-purple-800', icon: 'fa-hand-holding-medical', pulse: false };
    else if (name === 'OBSERVADO') config = { label: 'OBSERVADO', bgClass: 'bg-yellow-50 dark:bg-yellow-900/30', textClass: 'text-yellow-700 dark:text-yellow-400', borderClass: 'border-yellow-200 dark:border-yellow-800', icon: 'fa-exclamation-triangle', pulse: false };
    else if (name === 'VENCIDO') config = { label: 'VENCIDO', bgClass: 'bg-purple-100 dark:bg-purple-900/30', textClass: 'text-purple-700 dark:text-purple-400', borderClass: 'border-purple-200 dark:border-purple-800', icon: 'fa-exclamation-circle', pulse: false };
    else if (name === 'CRÍTICO' || name === 'PELIGRO') config = { label: 'CRÍTICO', bgClass: 'bg-rose-100 dark:bg-rose-900/30', textClass: 'text-rose-700 dark:text-rose-400', borderClass: 'border-rose-200 dark:border-rose-800', icon: 'fa-exclamation-triangle', pulse: true };
    else if (name === 'VENCE PRONTO' || name === 'ALERTA') config = { label: 'VENCE PRONTO', bgClass: 'bg-amber-100 dark:bg-amber-900/30', textClass: 'text-amber-700 dark:text-amber-400', borderClass: 'border-amber-200 dark:border-amber-800', icon: 'fa-bell', pulse: true };
    else if (name === 'SIN PRECIO') config = { label: 'SIN PRECIO', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-600 dark:text-rose-400', borderClass: 'border-rose-200 dark:border-rose-800', icon: 'fa-tag', pulse: false };
    else if (name === 'LISTO' || name === 'LISTO PARA VENTA') config = { label: 'LISTO', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600 dark:text-emerald-400', borderClass: 'border-emerald-200 dark:border-emerald-800', icon: 'fa-check', pulse: false };

    return config;
  }
}
