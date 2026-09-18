import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, signal, HostListener, ElementRef, inject, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-search-generic',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="relative max-w-2xl z-20 w-full">
        <div class="flex items-center space-x-2 bg-white dark:bg-slate-800 p-2 rounded-lg border border-slate-200 dark:border-slate-700 transition-colors shadow-sm focus-within:ring-2 focus-within:ring-blue-500/20 focus-within:border-blue-500/50">
            <svg class="w-5 h-5 text-slate-400 dark:text-slate-500 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>

            <input type="text"
                [(ngModel)]="query"
                (input)="onInput()"
                class="flex-1 bg-transparent border-none text-slate-800 dark:text-slate-200 text-sm focus:ring-0 outline-none placeholder-slate-400 dark:placeholder-slate-500"
                [placeholder]="placeholder">

            <button *ngIf="query" 
                (click)="clearSearch()"
                class="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full transition-colors mr-1">
                <svg class="w-4 h-4 text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>

            <div *ngIf="options.length > 0" class="h-6 w-px bg-slate-200 dark:bg-slate-700"></div>

            <button *ngIf="options.length > 0" (click)="toggleDropdown($event)"
                class="flex items-center px-3 py-1 text-sm text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition relative font-medium group">
                <span class="mr-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{{ getCurrentLabel() }}</span>
                <svg class="w-4 h-4 transition-transform duration-200"
                    [class.rotate-180]="isOpen()" fill="currentColor" viewBox="0 0 20 20">
                    <path
                        d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z">
                    </path>
                </svg>
            </button>
        </div>

        <!-- Dropdown Menu -->
        <div *ngIf="isOpen()"
            class="absolute right-0 top-full mt-2 w-64 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-xl z-30 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
            
            <!-- Buscador interno de opciones -->
            <div *ngIf="options.length > 10" class="p-2 border-b border-slate-100 dark:border-slate-700">
                <div class="relative">
                    <input type="text"
                        [ngModel]="optionFilter()"
                        (ngModelChange)="optionFilter.set($event)"
                        (click)="$event.stopPropagation()"
                        class="w-full pl-8 pr-3 py-2 text-xs bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg outline-none focus:ring-2 focus:ring-blue-500/20"
                        placeholder="Escribe para buscar...">
                    <svg class="w-3 h-3 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                    </svg>
                </div>
            </div>

            <div class="py-1 max-h-60 overflow-y-auto">
                <button
                    *ngFor="let opt of filteredOptions()"
                    (click)="changeType(opt.value); isOpen.set(false)"
                    class="w-full text-left px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 hover:text-blue-600 dark:hover:text-blue-400 transition-colors flex items-center justify-between group">
                    <span class="font-medium">{{ opt.label }}</span>
                    <svg *ngIf="currentType === opt.value" class="w-4 h-4 text-blue-500 opacity-100" fill="none"
                        viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M5 13l4 4L19 7" />
                    </svg>
                </button>
                <div *ngIf="filteredOptions().length === 0" class="px-4 py-3 text-xs text-slate-400 text-center italic">
                    Sin resultados
                </div>
            </div>
        </div>
    </div>
  `,
})
export class SearchGenericComponent implements OnInit, OnDestroy {
  @Input() options: { label: string; value: string }[] = [];
  @Input() placeholder: string = 'Buscar...';
  @Output() onSearch = new EventEmitter<{ q: string; type: string }>();

  query: string = '';
  currentType: string = 'ALL';
  isOpen = signal(false);
  optionFilter = signal('');

  filteredOptions = computed(() => {
    const filter = this.optionFilter().toLowerCase();
    if (!filter) return this.options;
    return this.options.filter(opt => 
      opt.label.toLowerCase().includes(filter) || 
      opt.value.toLowerCase().includes(filter)
    );
  });

  private searchSubject = new Subject<string>();
  private sub?: Subscription;
  private el = inject(ElementRef);

  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    if (!this.el.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  ngOnInit() {
    if (this.options.length > 0) this.currentType = this.options[0].value;

    this.sub = this.searchSubject
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((q) => this.onSearch.emit({ q, type: this.currentType }));
  }

  onInput() {
    this.isOpen.set(false); // Retraer al escribir
    this.searchSubject.next(this.query);
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    const nextState = !this.isOpen();
    this.isOpen.set(nextState);
    if (nextState) {
      this.optionFilter.set(''); // Limpiar filtro al abrir
    }
  }

  changeType(type: string) {
    this.currentType = type;
    this.onSearch.emit({ q: this.query, type: this.currentType });
  }

  getCurrentLabel(): string {
    const option = this.options.find(opt => opt.value === this.currentType);
    return option ? option.label : 'Filtrar por';
  }


  clearSearch() {
    this.query = '';
    this.isOpen.set(false);
    this.onInput();
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
