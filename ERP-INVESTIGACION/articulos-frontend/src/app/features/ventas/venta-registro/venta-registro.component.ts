import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { VentaSidebarComponent } from './components/venta-sidebar/venta-sidebar.component';
import { VentaMainComponent } from './components/venta-main/venta-main.component';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { UserResponse } from '../../../core/services/user.service';

@Component({
    selector: 'app-venta-registro',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        VentaSidebarComponent,
        VentaMainComponent
    ],
    templateUrl: './venta-registro.component.html'
})
export class VentaRegistroComponent {
    selectedPatient = signal<UserResponse | null>(null);

    constructor(public sidebarService: SidebarService) { }

    handleUserSelection(user: UserResponse): void {
        this.selectedPatient.set(user);
    }
}
