
import { Component, Input, Output, EventEmitter, forwardRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-switch',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormSwitchComponent),
      multi: true
    }
  ],
  template: `
    <div class="flex flex-col">
      <div class="flex items-center justify-between py-1 gap-3">
        @if (label) {
          <label class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-tight">
            {{ label }} @if (required) { <span class="text-red-500">*</span> }
          </label>
        }
        
        <label class="flex items-center cursor-pointer group">
          @if (description) {
            <span class="mr-2 text-[10px] font-black uppercase tracking-tighter text-slate-500 dark:text-slate-400 group-hover:text-indigo-500 transition-colors whitespace-nowrap text-right min-w-[75px]">
              {{ innerValue() ? onLabel : offLabel }}
            </span>
          }
          <div class="relative">
            <input 
              type="checkbox" 
              class="sr-only peer"
              [checked]="innerValue()"
              (change)="onToggle($event)"
              [disabled]="isDisabled()"
            >
            <div 
              class="w-8 h-4 bg-slate-200 dark:bg-slate-700 rounded-full peer peer-focus:ring-2 peer-focus:ring-indigo-500/30 peer-checked:bg-indigo-600 transition-all duration-300
                     after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-3 after:w-3 after:transition-all after:duration-300 peer-checked:after:translate-x-4 shadow-inner"
            ></div>
          </div>
        </label>
      </div>

      <!-- Reserved space for error -->
      @if (error) {
        <div class="h-4 relative">
          <p class="absolute top-0 left-0 text-[10px] text-red-500 font-semibold animate-fadeIn leading-tight">
            {{ error }}
          </p>
        </div>
      }
    </div>
  `
})
export class FormSwitchComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() description: boolean = true;
  @Input() onLabel: string = 'Activo';
  @Input() offLabel: string = 'Inactivo';
  @Input() required: boolean = false;
  @Input() error: string | null = null;
  @Input() set value(val: any) {
    this.writeValue(val);
  }
  @Output() valueChange = new EventEmitter<boolean>();

  innerValue = signal<boolean>(false);
  isDisabled = signal(false);

  onChange = (value: boolean) => { };
  onTouched = () => { };

  writeValue(value: any): void {
    // Manejar booleanos o strings 'A'/'I' si es necesario, 
    // pero por defecto trabajaremos con booleano.
    if (typeof value === 'string') {
        this.innerValue.set(value === 'A' || value === 'true');
    } else {
        this.innerValue.set(!!value);
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled.set(isDisabled);
  }

  onToggle(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.innerValue.set(checked);
    this.onChange(checked);
    this.valueChange.emit(checked);
    this.onTouched();
  }
}
