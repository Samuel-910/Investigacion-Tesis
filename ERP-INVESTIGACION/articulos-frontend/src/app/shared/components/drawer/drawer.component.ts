import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-drawer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drawer.component.html',
  styleUrls: ['./drawer.component.css']
})
export class DrawerComponent {
  @Input() set isOpen(value: boolean) {
    this._isOpen.set(value);
    if (value) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
  }
  
  @Input() title: string = '';
  @Output() onClose = new EventEmitter<void>();

  _isOpen = signal(false);

  close() {
    this.onClose.emit();
  }
}
