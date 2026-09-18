// sidebar.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SidebarService {
    private isOpen = new BehaviorSubject<boolean>(this.isDesktop());
    isOpen$ = this.isOpen.asObservable();

    private isDesktop(): boolean {
        return typeof window !== 'undefined' && window.innerWidth >= 768;
    }

    toggle() {
        this.isOpen.next(!this.isOpen.value);
    }

    close() {
        this.isOpen.next(false);
    }

    open() {
        this.isOpen.next(true);
    }

    get isOpenValue(): boolean {
        return this.isOpen.value;
    }
}
