import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col sm:flex-row justify-between items-center mt-2 gap-2 animate-in fade-in duration-500">
      
      <div class="flex items-center gap-1 bg-slate-50 dark:bg-slate-900/50 px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700/50 transition-all">
        <label class="text-[10px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-wider">Mostrar</label>
        <select 
          [ngModel]="pageSize" 
          (ngModelChange)="onPageSizeChange($event)"
          class="border-none rounded-lg px-2 py-0.5 text-[10px] font-black focus:ring-0 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 cursor-pointer shadow-sm">
          <option *ngFor="let size of pageSizeOptions" [ngValue]="size">{{ size }}</option>
        </select>
        <span class="text-[10px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-wider">{{ recordsLabel }}</span>
      </div>

      <nav class="flex items-center gap-2">
        
        <button type="button"
          (click)="onPageChange(currentPage - 1)" 
          [disabled]="currentPage === 0"
          class="p-2 text-[10px] font-black border border-slate-200 dark:border-slate-700/50 rounded-xl bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-400 disabled:opacity-30 disabled:cursor-not-allowed transition-all shadow-sm">
          <i class="fas fa-chevron-left"></i>
        </button>

        <div class="flex items-center gap-1">
          <button
            *ngFor="let page of getVisiblePages()"
            (click)="onPageChange(page)" 
            [class]="page === currentPage 
              ? 'bg-slate-900 dark:bg-sky-500 text-white shadow-lg shadow-sky-500/20 border-transparent' 
              : 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700/50 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700'"
            class="min-w-[32px] h-8 flex items-center justify-center text-[10px] font-black rounded-xl border transition-all duration-300">
            {{ page + 1 }}
          </button>
        </div>

        <button type="button"
          (click)="onPageChange(currentPage + 1)" 
          [disabled]="currentPage >= totalPages - 1"
          class="p-2 text-[10px] font-black border border-slate-200 dark:border-slate-700/50 rounded-xl bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-400 disabled:opacity-30 disabled:cursor-not-allowed transition-all shadow-sm">
          <i class="fas fa-chevron-right"></i>
        </button>
      </nav>
    </div>
  `
})
export class PaginationComponent {
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 0;
  @Input() pageSize: number = 10;
  @Input() pageSizeOptions: number[] = [10, 25, 50, 100];
  @Input() maxVisiblePages: number = 5;

  @Input() recordsLabel: string = 'registros';
  @Input() previousLabel: string = 'Anterior';
  @Input() nextLabel: string = 'Siguiente';

  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.pageChange.emit(page);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSizeChange.emit(newSize);
  }

  getVisiblePages(): number[] {
    if (this.totalPages <= 0) return [];

    const halfVisible = Math.floor(this.maxVisiblePages / 2);
    let startPage = Math.max(0, this.currentPage - halfVisible);
    let endPage = Math.min(this.totalPages - 1, startPage + this.maxVisiblePages - 1);

    if (endPage - startPage < this.maxVisiblePages - 1) {
      startPage = Math.max(0, endPage - this.maxVisiblePages + 1);
    }

    const pages: number[] = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }
}