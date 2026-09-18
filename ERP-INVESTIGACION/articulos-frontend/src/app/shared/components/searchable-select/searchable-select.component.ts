import { Component, Input, forwardRef, signal, computed, effect, ElementRef, HostListener, SimpleChanges, OnChanges, Output, EventEmitter, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';

@Component({
    selector: 'app-searchable-select',
    standalone: true,
    imports: [CommonModule, FormsModule],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SearchableSelectComponent),
            multi: true
        }
    ],
    template: `
    <div class="flex flex-col" #searchContainer (focusout)="onFocusOut($event)" (keydown.escape)="closeDropdown()">
      <div class="relative">
        <!-- Icono Precedente (Opcional) -->
        <div *ngIf="hasIcon" class="absolute left-4 top-[22px] -translate-y-1/2 text-slate-400 pointer-events-none z-10 transition-colors h-5 flex items-center">
          <ng-content select="[icon]"></ng-content>
        </div>

        <input
          #searchInput
          type="text"
          [placeholder]="placeholder"
          [value]="searchTerm()"
          (input)="onSearch($event)"
          (focus)="onFocus()"
          (keydown)="handleKeyDown($event)"
          [disabled]="isDisabled()"
          [class.!border-red-500]="error"
          [class.pl-11]="hasIcon"
          class="peer block w-full rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 disabled:opacity-50 disabled:bg-slate-50 dark:disabled:bg-slate-800/50 transition-all duration-200 px-4 py-3 text-sm placeholder:text-transparent font-medium outline-none"
        />

        <label *ngIf="label" 
          class="absolute px-1 text-slate-500 dark:text-slate-400 text-sm transition-all duration-200 pointer-events-none
                 bg-white dark:bg-slate-800 rounded
                 peer-focus:-top-2.5 peer-focus:left-2 peer-focus:text-xs peer-focus:text-blue-500
                 peer-[:not(:placeholder-shown)]:-top-2.5 peer-[:not(:placeholder-shown)]:left-2 peer-[:not(:placeholder-shown)]:text-xs
                 flex items-center gap-1"
          [ngClass]="(selectedItem() !== null || searchTerm() !== '' || showDropdown()) ? '-top-2.5 left-2 text-xs' : 'top-3'"
          [class.left-10]="hasIcon && !(selectedItem() !== null || searchTerm() !== '' || showDropdown())"
          [class.left-3]="!hasIcon && !(selectedItem() !== null || searchTerm() !== '' || showDropdown())"
          [class.!text-red-500]="error"
          [class.peer-focus:!text-red-500]="error">
          {{ label }} <span *ngIf="required" class="text-red-500">*</span>
        </label>

        <!-- Selection Icon / Chevron -->
        <div class="absolute right-3 top-[22px] -translate-y-1/2 flex items-center pointer-events-none text-slate-400">
          <svg class="w-4 h-4 transition-transform duration-200" [class.rotate-180]="showDropdown()" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </div>

        <!-- Dropdown -->
        <div *ngIf="showDropdown()" 
             class="absolute top-full left-0 z-50 w-full bg-white dark:bg-slate-800 border-2 border-slate-100 dark:border-slate-700 rounded-xl shadow-xl max-h-48 overflow-y-auto animate-in fade-in slide-in-from-top-2 duration-200">
          <ng-container *ngIf="filteredData().length > 0; else noData">
              <div *ngFor="let item of filteredData(); let i = index"
                   (mousedown)="selectItem(item); $event.preventDefault()"
                   [class.bg-blue-50]="activeIndex() === i"
                   [class.dark:bg-slate-700]="activeIndex() === i"
                   class="px-4 py-2.5 text-sm hover:bg-blue-50 dark:hover:bg-slate-700 cursor-pointer transition-colors border-b border-slate-50 dark:border-slate-700 last:border-b-0 text-slate-800 dark:text-slate-200 font-medium">
                {{ item[bindLabel] }}
              </div>
          </ng-container>

          <ng-template #noData>
              <div class="px-4 py-4 text-center text-slate-500 dark:text-slate-400 text-xs italic">
                  <i class="fas fa-info-circle mr-2"></i>
                  {{ dataSignal().length === 0 ? 'Sin datos disponibles' : 'Sin resultados encontrados' }}
              </div>
          </ng-template>
        </div>
      </div>

      <!-- Reserved space for error to avoid shifting -->
      <div class="h-4 relative mt-0.5">
        <p *ngIf="error" class="absolute top-0 left-0 text-[10px] text-red-500 font-semibold animate-fadeIn leading-tight">
          {{ error }}
        </p>
      </div>
    </div>
  `
})
export class SearchableSelectComponent implements ControlValueAccessor, OnChanges {
    @ViewChild('searchContainer') searchContainer!: ElementRef;
    @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

    // Inputs reactivos mediante señales
    protected dataSignal = signal<any[]>([]);
    protected labelSignal = signal('Seleccionar');
    protected bindLabelSignal = signal('nombre');
    protected bindValueSignal = signal<string | null>(null);

    @Input() set data(v: any[]) { this.dataSignal.set(v || []); }
    @Input() set label(v: string) { this.labelSignal.set(v || 'Seleccionar'); }
    get label() { return this.labelSignal(); }

    @Input() set bindLabel(v: string) { this.bindLabelSignal.set(v || 'nombre'); }
    get bindLabel() { return this.bindLabelSignal(); }

    @Input() set bindValue(v: string | null) { this.bindValueSignal.set(v); }
    get bindValue() { return this.bindValueSignal(); }

    @Input() required: boolean = false;
    @Input() error: string | null = null;
    @Input() hasIcon: boolean = false;
    @Input() placeholder: string = ' ';
    @Output() selected = new EventEmitter<any>();

    protected isDisabled = signal(false);
    protected searchTerm = signal('');
    protected showDropdown = signal(false);
    protected _valueSignal = signal<any>(null);
    protected activeIndex = signal<number>(-1);

    // Selección calculada
    selectedItem = computed(() => {
        const data = this.dataSignal();
        const val = this._valueSignal();
        const bValue = this.bindValueSignal();

        if (val === undefined || val === null || val === '') return null;

        if (bValue) {
            return data.find(item => item[bValue] == val) || null;
        }
        return val;
    });

    // Datos filtrados para el dropdown
    filteredData = computed(() => {
        const term = this.searchTerm().toLowerCase();
        const data = this.dataSignal();
        const bLabel = this.bindLabelSignal();

        if (!term) return data;
        return data.filter(item => {
            const val = item[bLabel]?.toString().toLowerCase() || '';
            return val.includes(term);
        });
    });

    // CVA Callbacks
    onChange: any = () => { };
    onTouched: any = () => { };

    constructor(private elementRef: ElementRef) {
        // Sincronizar searchTerm con el item seleccionado cuando no se está editando activamente
        effect(() => {
            const selected = this.selectedItem();
            const bLabel = this.bindLabelSignal();
            const isDropdownOpen = this.showDropdown();

            // Solo sincronizamos si el dropdown NO está abierto (no se está buscando)
            // o si el searchTerm está vacío y hay una selección
            if (!isDropdownOpen) {
                if (selected) {
                    this.searchTerm.set(selected[bLabel]);
                } else {
                    this.searchTerm.set('');
                }
            }
        }, { allowSignalWrites: true });
    }

    @HostListener('document:click', ['$event'])
    onClickOutside(event: Event) {
        if (this.searchContainer && !this.searchContainer.nativeElement.contains(event.target)) {
            this.closeDropdown();
        }
    }

    onFocus() {
        this.showDropdown.set(true);
        this.activeIndex.set(-1);
    }

    onFocusOut(event: FocusEvent) {
        if (this.searchContainer && !this.searchContainer.nativeElement.contains(event.relatedTarget as Node)) {
            this.closeDropdown();
        }
    }

    toggleDropdown() {
        this.showDropdown.update(v => !v);
    }

    closeDropdown() {
        this.showDropdown.set(false);

        // Si el usuario borró todo el texto manualmente, limpiamos la selección
        if (this.searchTerm().trim() === '') {
            this.clearSelection();
            return;
        }

        const selected = this.selectedItem();
        const bLabel = this.bindLabelSignal();

        // Revertir el término de búsqueda al valor seleccionado si no coincide
        if (selected) {
            this.searchTerm.set(selected[bLabel]);
        } else {
            this.searchTerm.set('');
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        // ngOnChanges ya no es necesario para data si usamos setters, pero lo dejamos por compatibilidad si se quitan setters
    }

    // Eliminamos updateSelection ya que ahora es un computed signal (selectedItem)


    onSearch(event: any) {
        this.searchTerm.set(event.target.value);
        this.activeIndex.set(-1);
        if (!this.showDropdown()) {
            this.showDropdown.set(true);
        }
    }

    handleKeyDown(event: KeyboardEvent) {
        if (!this.showDropdown()) {
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                this.showDropdown.set(true);
            }
            return;
        }

        const data = this.filteredData();
        const maxIndex = data.length - 1;

        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                this.activeIndex.update(idx => (idx < maxIndex ? idx + 1 : idx));
                this.scrollToActiveItem();
                break;
            case 'ArrowUp':
                event.preventDefault();
                this.activeIndex.update(idx => (idx > 0 ? idx - 1 : 0));
                this.scrollToActiveItem();
                break;
            case 'Enter':
                event.preventDefault();
                const currentIdx = this.activeIndex();
                if (currentIdx >= 0 && currentIdx <= maxIndex) {
                    this.selectItem(data[currentIdx]);
                }
                break;
            case 'Escape':
                this.closeDropdown();
                break;
            case 'Tab':
                this.closeDropdown();
                break;
        }
    }

    private scrollToActiveItem() {
        // Ejecutar después de que Angular actualice el DOM
        setTimeout(() => {
            const dropdown = this.elementRef.nativeElement.querySelector('.max-h-48');
            if (!dropdown) return;

            const activeItem = dropdown.querySelectorAll('div')[this.activeIndex()];
            if (!activeItem) return;

            const dropdownRect = dropdown.getBoundingClientRect();
            const itemRect = activeItem.getBoundingClientRect();

            if (itemRect.bottom > dropdownRect.bottom) {
                dropdown.scrollTop += (itemRect.bottom - dropdownRect.bottom);
            } else if (itemRect.top < dropdownRect.top) {
                dropdown.scrollTop -= (dropdownRect.top - itemRect.top);
            }
        });
    }

    selectItem(item: any) {
        const bValue = this.bindValueSignal();
        const bLabel = this.bindLabelSignal();
        const value = bValue ? item[bValue] : item;

        this._valueSignal.set(value);
        this.searchTerm.set(item[bLabel]);
        this.showDropdown.set(false);

        this.onChange(value);
        this.selected.emit(item);

        // Desenfocar el input para que no se quede el cursor parpadeando
        if (this.searchInput) {
            this.searchInput.nativeElement.blur();
        }
    }

    clearSelection() {
        this._valueSignal.set(null);
        this.searchTerm.set('');
        this.onChange(null);
    }

    // --- ControlValueAccessor Implementation ---

    writeValue(obj: any): void {
        this._valueSignal.set(obj);
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.isDisabled.set(isDisabled);
    }


}
