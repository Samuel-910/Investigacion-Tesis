import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

type ButtonSize = 'sm' | 'md' | 'lg';
type ButtonColor = 'brand' | 'blue' | 'green' | 'red' | 'purple' | 'gray' | 'slate';

@Component({
  selector: 'app-primary-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button 
      (click)="handleClick()"
      [disabled]="disabled || loading"
      [class]="getButtonClasses()"
      [type]="type">
      
      <svg *ngIf="loading" 
           class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" 
           xmlns="http://www.w3.org/2000/svg" 
           fill="none" 
           viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>

      <svg *ngIf="icon && !loading" 
           [class]="getIconClasses()"
           fill="none" 
           stroke="currentColor" 
           viewBox="0 0 24 24">
        <path stroke-linecap="round" 
              stroke-linejoin="round" 
              stroke-width="2.5"
              [attr.d]="getIconPath()" />
      </svg>
      
      <span class="tracking-tight">{{ loading ? 'Cargando...' : label }}</span>
    </button>
  `,
  styles: []
})
export class PrimaryButtonComponent {
  @Input() label: string = '';
  @Input() icon: 'plus' | 'edit' | 'delete' | 'save' | 'cancel' | 'search' | 'download' | 'upload' | 'file-excel' | 'print' | 'payments' | 'add_circle' | null = null;
  @Input() size: ButtonSize = 'md';
  @Input() color: ButtonColor = 'brand';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() fullWidth: boolean = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';

  @Output() btnClick = new EventEmitter<void>();

  handleClick(): void {
    if (!this.disabled && !this.loading) {
      this.btnClick.emit();
    }
  }

  getButtonClasses(): string {
    const baseClasses = 'group flex items-center gap-1 font-bold rounded-lg transition-all duration-300 shadow-sm hover:shadow-md transform hover:-translate-y-0.5 active:scale-95';

    const sizeClasses: Record<ButtonSize, string> = {
      'sm': 'px-2 py-1 text-[10px]',
      'md': 'px-3 py-1.5 text-[11px]',
      'lg': 'px-4 py-2 text-xs'
    };

    const colorClasses: Record<ButtonColor, string> = {
      // Brand color con soporte dark mode
      'brand': 'bg-slate-800 hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600 text-white border border-slate-700 dark:border-slate-600',

      // Slate con soporte dark mode
      'slate': 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-800 dark:text-slate-100 border border-slate-300 dark:border-slate-600',

      // Blue con soporte dark mode
      'blue': 'bg-blue-600 hover:bg-blue-700 dark:bg-blue-600 dark:hover:bg-blue-700 text-white border border-blue-700 dark:border-blue-800',

      // Green con soporte dark mode
      'green': 'bg-emerald-600 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-700 text-white border border-emerald-700 dark:border-emerald-800',

      // Red con soporte dark mode
      'red': 'bg-rose-600 hover:bg-rose-700 dark:bg-rose-600 dark:hover:bg-rose-700 text-white border border-rose-700 dark:border-rose-800',

      // Purple con soporte dark mode
      'purple': 'bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-600 dark:hover:bg-indigo-700 text-white border border-indigo-700 dark:border-indigo-800',

      // Gray con soporte dark mode
      'gray': 'bg-slate-500 hover:bg-slate-600 dark:bg-slate-600 dark:hover:bg-slate-700 text-white border border-slate-600 dark:border-slate-700'
    };

    const disabledClasses = (this.disabled || this.loading) ? 'opacity-40 cursor-not-allowed hover:shadow-sm hover:translate-y-0 active:scale-100' : '';
    const widthClass = this.fullWidth ? 'w-full justify-center' : '';

    return `${baseClasses} ${sizeClasses[this.size]} ${colorClasses[this.color]} ${disabledClasses} ${widthClass}`;
  }

  getIconClasses(): string {
    const sizeClasses: Record<ButtonSize, string> = {
      'sm': 'w-3 h-3',
      'md': 'w-3.5 h-3.5',
      'lg': 'w-4 h-4'
    };

    const animationClass = this.icon === 'plus' ? 'group-hover:rotate-90' : '';

    return `${sizeClasses[this.size]} ${animationClass} transition-transform duration-300`;
  }

  getIconPath(): string {
    const icons: Record<string, string> = {
      'plus': 'M12 4v16m8-8H4',
      'edit': 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z',
      'delete': 'M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16',
      'save': 'M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4',
      'cancel': 'M6 18L18 6M6 6l12 12',
      'search': 'M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z',
      'download': 'M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4',
      'upload': 'M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12',
      'file-excel': 'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      'print': 'M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z',
      'payments': 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.82v-1.91c-1.55-.37-2.91-1.32-3.41-2.4l1.37-.58c.31.72 1.14 1.34 2.04 1.57.81.21 1.77.1 2.37-.25.56-.33.78-.9.78-1.5a1.491 1.491 0 00-.91-1.39c-.58-.33-1.63-.64-2.88-.95-1.51-.38-2.58-.78-3.21-1.21-1.22-.84-1.83-2.07-1.83-3.69 0-1.61.84-2.88 2.51-3.61V5h2.82v1.92c1.23.23 2.31.78 2.91 1.58l-1.23.77c-.45-.63-1.12-1.02-1.68-1.15-.65-.13-1.45-.11-2 .23-.62.37-.87.9-.87 1.44s.3 1.04.89 1.4c.59.36 1.63.63 3.12 1 1.84.45 3.09 1.14 3.73 2.07.63.93.95 2.08.95 3.44.02 1.78-.96 3.08-2.84 3.92z',
      'add_circle': 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11h-4v4h-2v-4H7v-2h4V7h2v4h4v2z'
    };

    return icons[this.icon || ''] || '';
  }
}