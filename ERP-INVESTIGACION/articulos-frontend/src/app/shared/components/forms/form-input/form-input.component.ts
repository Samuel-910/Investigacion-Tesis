
import { Component, Input, Output, EventEmitter, forwardRef, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormInputComponent),
      multi: true
    }
  ],
  template: `
    <div class="flex flex-col">
      <div class="relative">
        <!-- Icono Precedente (Opcional) -->
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none z-10 transition-colors h-5 flex items-center">
          <ng-content select="[icon]"></ng-content>
        </div>

        <input #inputElement
          [type]="isPassword && showPassword() ? 'text' : type"
          [value]="innerValue()"
          (input)="onInput($event)"
          (keydown)="onKeyDown($event)"
          (blur)="onTouched()"
          placeholder=" "
          [disabled]="isDisabled()"
          [min]="min"
          [max]="max"
          [attr.maxlength]="maxlength"
          [step]="step"
          [class.pr-10]="isPassword || suffix || hasSuffixContent"
          [class.pl-10]="prefix || hasPrefixContent || hasIcon" 
          [class.!border-red-500]="error"
          [class.!focus:ring-red-500]="error"
          [readonly]="readonly"
          [attr.tabindex]="readonly ? -1 : 0"
          [ngClass]="readonly ? 'bg-slate-100 dark:bg-slate-800 pointer-events-none' : 'bg-white dark:bg-slate-800'"
          [class.text-center]="centerText"
          class="peer block w-full rounded-xl border-2 border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 hover:border-slate-400 dark:hover:border-slate-600 disabled:opacity-50 disabled:bg-slate-50 dark:disabled:bg-slate-800/50 transition-all duration-200 px-3 py-3 text-sm placeholder:text-transparent font-medium outline-none"
        />
        
        <label *ngIf="label" 
          class="absolute top-3 px-1 text-slate-500 dark:text-slate-400 text-sm transition-all duration-200 pointer-events-none
                bg-white dark:bg-slate-800 rounded whitespace-nowrap
                peer-focus:-top-2.5 peer-focus:left-2 peer-focus:text-xs peer-focus:text-blue-500
                peer-[:not(:placeholder-shown)]:-top-2.5 peer-[:not(:placeholder-shown)]:left-2 peer-[:not(:placeholder-shown)]:text-xs
                flex items-center gap-1"
          [class.left-9]="prefix || hasPrefixContent || hasIcon"
          [class.left-3]="!prefix && !hasPrefixContent && !hasIcon"
          [class.!text-red-500]="error"
          [class.peer-focus:!text-red-500]="error">
          {{ label }} <span *ngIf="required" class="text-red-500">*</span>
        </label>

        <!-- Prefix Content -->
        <div *ngIf="prefix || hasPrefixContent || hasIcon" class="absolute left-3 top-1/2 -translate-y-1/2 flex items-center z-10 pointer-events-none">
          <span *ngIf="prefix" class="text-slate-500 dark:text-slate-400 text-sm font-bold mr-1">
            {{ prefix }}
          </span>
          <ng-content select="[prefix]"></ng-content>
        </div>

        <!-- Suffix Content (con Password Toggle) -->
        <div class="absolute right-3 top-1/2 -translate-y-1/2 flex items-center z-10">
          <button *ngIf="isPassword" 
                  type="button"
                  (click)="togglePassword()"
                  class="p-1.5 text-slate-400 hover:text-blue-500 dark:hover:text-blue-400 focus:outline-none transition-colors">
            <i class="fas" [ngClass]="showPassword() ? 'fa-eye-slash' : 'fa-eye'"></i>
          </button>
          <span *ngIf="suffix" class="text-slate-500 dark:text-slate-400 text-sm font-medium ml-1">
              {{ suffix }}
          </span>
          <ng-content select="[suffix]"></ng-content>
        </div>
      </div>

      <!-- Reserved space for error to avoid shifting -->
      <div class="h-4 relative mt-0.5">
        <p *ngIf="error" class="absolute top-0 left-0 text-[10px] text-red-500 font-semibold animate-fadeIn leading-tight">
          {{ error }}
        </p>
      </div>
    </div>
  `,
  styles: [`
    /* Ocultar flechas en Chrome, Safari, Edge, Opera */
    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }

    /* Ocultar flechas en Firefox */
    input[type=number] {
      -moz-appearance: textfield;
    }

    :host-context(.dark) input[type="date"]::-webkit-calendar-picker-indicator {
      filter: invert(0.8) brightness(1.2);
      cursor: pointer;
    }
    input[type="date"]::-webkit-calendar-picker-indicator {
      cursor: pointer;
    }
  `]
})
export class FormInputComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() type: string = 'text';
  @Input() placeholder: string = '';
  @Input() required: boolean = false;
  @Input() error: string | null = null;
  @Input() min: string | number | null = null;
  @Input() max: string | number | null = null;
  @Input() maxlength: number | null = null;
  @Input() step: string | number | null = null;
  @Input() suffix: string | null = null;
  @Input() prefix: string | null = null;
  @Input() hasPrefixContent: boolean = false;
  @Input() hasSuffixContent: boolean = false;
  @Input() hasIcon: boolean = false;
  @Input() readonly: boolean = false;
  @Input() onlyNumbers: boolean = false;
  @Input() centerText: boolean = false;
  @Input() set disabled(val: boolean) {
    this.setDisabledState(val);
  }
  @Output() valueChange = new EventEmitter<string>();
  @Output() keydown = new EventEmitter<KeyboardEvent>();

  @ViewChild('inputElement') inputElement!: ElementRef<HTMLInputElement>;

  focus() {
    this.inputElement?.nativeElement?.focus();
  }

  onKeyDown(event: KeyboardEvent) {
    this.keydown.emit(event);
    if (this.onlyNumbers) {
      // Permitir: Teclas de control
      const allowedKeys = [
        'Backspace', 'Delete', 'Tab', 'Escape', 'Enter', 
        'ArrowLeft', 'ArrowRight', 'Home', 'End'
      ];
      if (allowedKeys.includes(event.key)) {
        return;
      }
      
      // Bloquear si no es número ni punto decimal
      if ((event.key < '0' || event.key > '9') && event.key !== '.') {
        event.preventDefault();
      }
    }
  }

  @Input() set value(val: any) {
    this.writeValue(val);
  }

  innerValue = signal<any>('');
  isDisabled = signal(false);
  showPassword = signal(false);

  get isPassword(): boolean {
    return this.type === 'password';
  }

  togglePassword() {
    this.showPassword.update(v => !v);
  }

  onChange = (value: any) => { };
  onTouched = () => { };

  writeValue(value: any): void {
    const val = value !== null && value !== undefined ? value : '';
    this.innerValue.set(val);
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

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.innerValue.set(value);
    this.onChange(value);
    this.valueChange.emit(value);
  }
}
