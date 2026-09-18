import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  route?: string;
  icon?: string;
  queryParams?: any;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="flex items-center text-sm mb-6 bg-white dark:bg-slate-800 w-fit px-4 py-2 rounded-lg border border-slate-200 dark:border-slate-700 transition-colors">
      <svg *ngIf="showIcon" class="w-4 h-4 mr-2.5 text-slate-800 dark:text-slate-200 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>

      <ng-container *ngFor="let item of items; let last = last; let i = index">
        <a *ngIf="item.route && !last" 
           [routerLink]="item.route"
           [queryParams]="item.queryParams"
           class="text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200 font-semibold transition-colors duration-200">
          {{ item.label }}
        </a>
        
        <span *ngIf="!item.route && !last" class="text-slate-500 dark:text-slate-400 font-semibold transition-colors">
          {{ item.label }}
        </span>

        <span *ngIf="last" class="text-slate-800 dark:text-white font-black tracking-tight transition-colors">
          {{ item.label }}
        </span>

        <svg *ngIf="!last" class="w-3.5 h-3.5 mx-2.5 text-slate-300 dark:text-slate-600 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M9 5l7 7-7 7" />
        </svg>
      </ng-container>
    </nav>
  `
})
export class BreadcrumbComponent {
  @Input() items: BreadcrumbItem[] = [];
  @Input() showIcon: boolean = true;
}