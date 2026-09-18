
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-form-label',
    standalone: true,
    imports: [CommonModule],
    template: `
    <label class="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
      <ng-content></ng-content>
      <span *ngIf="required" class="text-red-500 ml-0.5">*</span>
    </label>
  `
})
export class FormLabelComponent {
    @Input() required: boolean = false;
}
