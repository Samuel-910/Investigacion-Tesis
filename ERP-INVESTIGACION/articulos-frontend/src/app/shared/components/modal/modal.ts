import { Component, Input, Output, EventEmitter, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../features/auth/services/auth.service';

type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl' | '5xl' | '6xl' | '7xl';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="isOpen"
      class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50 p-2 md:p-4 animate-fadeIn"
      (click)="onBackdropClick($event)">
      
      <div [class]="getModalClasses()" (click)="$event.stopPropagation()">
        
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700">
          <div class="flex justify-between items-center w-full">
            <div class="flex items-center gap-3">
              <div *ngIf="showIcon" class="bg-indigo-50 dark:bg-slate-700/50 p-2 rounded-xl text-indigo-600 dark:text-indigo-400">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" [attr.d]="getIconPath()" />
                </svg>
              </div>
              <h3 class="text-lg font-bold text-slate-900 dark:text-white tracking-tight">{{ title }}</h3>
            </div>

            <!-- Información de Sucursal (Lado derecho, antes del botón cerrar si está activo) -->
            <div class="flex items-center gap-4">
              <div *ngIf="showSucursal" 
                class="hidden md:flex items-center gap-2 px-3 py-1.5 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 rounded-full border border-blue-100 dark:border-blue-800/50 scale-95 origin-right">
                <i class="fas fa-hospital text-xs"></i>
                <span class="text-[11px] font-bold uppercase tracking-wider">Sucursal {{ sucursalLabel() }}</span>
              </div>
              
              <button *ngIf="showCloseButton" (click)="close()"
                class="text-slate-400 hover:text-red-500 hover:bg-red-50 dark:text-slate-500 dark:hover:text-red-400 dark:hover:bg-red-900/20 p-2 rounded-lg transition-all ml-1">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div [class]="allowOverflow ? 'p-5 md:p-6 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100' : 'p-5 md:p-6 max-h-[calc(100vh-140px)] overflow-y-auto custom-scroll bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100'" 
             class="transition-colors duration-300" [class.!p-0]="noPadding">
          <ng-content></ng-content>
        </div>

      </div>
    </div>
  `,
  styles: [`
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideUp { 
      from { transform: translateY(15px); opacity: 0; } 
      to { transform: translateY(0); opacity: 1; } 
    }
    .animate-fadeIn { animation: fadeIn 0.2s ease-out; }
    .animate-slideUp { animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
    
    .custom-scroll::-webkit-scrollbar { width: 5px; }
    .custom-scroll::-webkit-scrollbar-track { background: transparent; }
    .custom-scroll::-webkit-scrollbar-thumb { 
      background: #CBD5E1; 
      border-radius: 10px; 
    }
    .dark .custom-scroll::-webkit-scrollbar-thumb {
      background: #475569;
    }
    .custom-scroll::-webkit-scrollbar-thumb:hover {
      background: #94A3B8;
    }
    .dark .custom-scroll::-webkit-scrollbar-thumb:hover {
      background: #64748B;
    }
  `]
})
export class ModalComponent implements OnInit {
  @Input() isOpen: boolean = false;
  @Input() title: string = '';
  @Input() size: ModalSize = '2xl';
  @Input() showCloseButton: boolean = true;
  @Input() showIcon: boolean = true;
  @Input() iconType: 'document' | 'edit' | 'delete' | 'info' | 'warning' | 'success' | 'custom' = 'document';
  @Input() customIconPath: string = '';
  @Input() closeOnBackdrop: boolean = false;
  @Input() allowOverflow: boolean = false;
  @Input() showFooter: boolean = false;
  @Input() noPadding: boolean = false;
  @Input() showSucursal: boolean = false;
  @Output() modalClose = new EventEmitter<void>();

  protected sucursalLabel = signal<string>('');

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    if (this.showSucursal) {
      this.sucursalLabel.set(this.authService.getSucursalFromToken() || 'Sin Sucursal');
    }
  }

  close(): void { this.modalClose.emit(); }

  onBackdropClick(event: MouseEvent): void {
    if (this.closeOnBackdrop) this.close();
  }

  getModalClasses(): string {
    const overflowClass = this.allowOverflow ? 'overflow-visible' : 'overflow-hidden';
    const baseClasses = `bg-white dark:bg-slate-800 rounded-2xl shadow-2xl w-[95%] md:w-full ${overflowClass} animate-slideUp border border-slate-200 dark:border-slate-700 transition-colors duration-300`;

    const sizeClasses: Record<ModalSize, string> = {
      'sm': 'max-w-md',
      'md': 'max-w-2xl',
      'lg': 'max-w-4xl',
      'xl': 'max-w-6xl',
      '2xl': 'max-w-7xl',
      '3xl': 'max-w-8xl',
      '4xl': 'max-w-9xl',
      '5xl': 'max-w-10xl',
      '6xl': 'max-w-11xl',
      '7xl': 'max-w-12xl'
    };

    return `${baseClasses} ${sizeClasses[this.size]}`;
  }

  getIconPath(): string {
    if (this.iconType === 'custom' && this.customIconPath) return this.customIconPath;
    const icons: Record<string, string> = {
      'document': 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      'edit': 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z',
      'delete': 'M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16',
      'info': 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      'warning': 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      'success': 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
    };
    return icons[this.iconType] || icons['document'];
  }
}