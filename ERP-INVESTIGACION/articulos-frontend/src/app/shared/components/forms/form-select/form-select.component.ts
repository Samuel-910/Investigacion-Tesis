
import { Component, Input, Output, EventEmitter, forwardRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, FormsModule } from '@angular/forms';

export interface SelectOption {
  label: string;
  value: any;
}

@Component({
  selector: 'app-form-select',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormSelectComponent),
      multi: true
    }
  ],
  template: `
    <div class="flex flex-col">
      <div class="relative">
        <label *ngIf="label" 
          class="absolute left-3 px-1 text-slate-500 dark:text-slate-400 text-sm transition-all duration-200 pointer-events-none
                 bg-white dark:bg-slate-800 rounded
                 peer-focus:-top-2.5 peer-focus:left-2 peer-focus:text-xs peer-focus:text-blue-500
                 flex items-center gap-1"
          [ngClass]="(innerValue() !== '' && innerValue() !== null && innerValue() !== undefined) ? '-top-2.5 left-2 text-xs z-10' : 'top-3 z-0'"
          [class.!text-red-500]="error"
          [class.peer-focus:!text-red-500]="error">
          {{ label }} <span *ngIf="required" class="text-red-500">*</span>
        </label>
        
        <select
          [value]="innerValue()"
          (change)="onSelectChange($event)"
          (blur)="onTouched()"
          [disabled]="isDisabled()"
          [class.!border-red-500]="error"
          [class.!focus:ring-red-500]="error"
          class="peer block w-full rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 hover:border-slate-400 dark:hover:border-slate-600 disabled:opacity-50 disabled:bg-slate-50 dark:disabled:bg-slate-800/50 transition-all duration-200 px-4 py-3 text-sm appearance-none font-medium outline-none z-1"
        >
          <option class="bg-white dark:bg-slate-800 text-slate-900 dark:text-white" *ngIf="!innerValue()" value="" disabled [selected]="true">{{ placeholder || '' }}</option>
          <option class="bg-white dark:bg-slate-800 text-slate-900 dark:text-white" *ngFor="let option of options" [value]="option.value">
              {{ option.label }}
          </option>
        </select>

        <!-- Custom Chevron Icon -->
        <div class="absolute inset-y-0 right-0 flex items-center px-4 pointer-events-none text-slate-500 dark:text-slate-400 z-10">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
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
export class FormSelectComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() options: SelectOption[] = [];
  @Input() required: boolean = false;
  @Input() error: string | null = null;
  @Input() set value(val: any) {
    this.writeValue(val);
  }
  @Output() valueChange = new EventEmitter<any>();

  innerValue = signal<any>('');
  isDisabled = signal(false);

  onChange = (value: any) => { };
  onTouched = () => { };

  writeValue(value: any): void {
    this.innerValue.set(value !== null && value !== undefined ? value : '');
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onSelectChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.innerValue.set(value);
    this.onChange(value);
    this.valueChange.emit(value);
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled.set(isDisabled);
  }
}
