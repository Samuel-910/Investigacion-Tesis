
import { Component, Input, forwardRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-textarea',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormTextareaComponent),
      multi: true
    }
  ],
  template: `
    <div class="relative mt-2">
      <textarea
        [value]="value()"
        (input)="onInput($event)"
        (blur)="onTouched()"
        [rows]="rows"
        placeholder=" "
        [disabled]="isDisabled()"
        [class.!border-red-500]="error"
        [class.!focus:ring-red-500]="error"
        class="peer block w-full rounded-xl border-2 border-slate-300 dark:border-slate-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 disabled:opacity-50 disabled:bg-slate-50 dark:disabled:bg-slate-800/50 transition-all duration-200 px-4 py-3 text-sm placeholder:text-transparent font-medium outline-none resize-none"
      ></textarea>

      <label *ngIf="label" 
        class="absolute left-3 top-3 px-1 text-slate-500 dark:text-slate-400 text-sm transition-all duration-200 pointer-events-none
               bg-white dark:bg-slate-800 rounded
               peer-focus:-top-2.5 peer-focus:left-2 peer-focus:text-xs peer-focus:text-blue-500
               peer-[:not(:placeholder-shown)]:-top-2.5 peer-[:not(:placeholder-shown)]:left-2 peer-[:not(:placeholder-shown)]:text-xs
               flex items-center gap-1"
        [class.!text-red-500]="error"
        [class.peer-focus:!text-red-500]="error">
        {{ label }} <span *ngIf="required" class="text-red-500">*</span>
      </label>

      <p *ngIf="error" class="mt-1 text-xs text-red-500 font-medium animate-fadeIn">
        {{ error }}
      </p>
    </div>
  `
})
export class FormTextareaComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() rows: number = 3;
  @Input() placeholder: string = '';
  @Input() required: boolean = false;
  @Input() error: string | null = null;

  value = signal<any>('');
  isDisabled = signal(false);

  onChange = (value: any) => { };
  onTouched = () => { };

  writeValue(value: any): void {
    this.value.set(value || '');
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
    const value = (event.target as HTMLTextAreaElement).value;
    this.value.set(value);
    this.onChange(value);
  }
}
