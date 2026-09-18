import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; import { MetodoPagoService } from '../../almacen/service/atributo.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { AtributoListComponent } from '../../../shared/atributos/components/atributo-list/atributo-list.component';

@Component({
    selector: 'app-medio-pago-page',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        AtributoListComponent,
        BreadcrumbComponent,
        PageHeaderComponent
    ],
    templateUrl: './medio-pago-page.component.html'
})
export class MedioPagoPageComponent {
    public sidebarService = inject(SidebarService);
    public metodoPagoService = inject(MetodoPagoService);
    totalMediosPago = signal(0);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja', route: '/caja/arqueos' },
        { label: 'Medios de Pago' }
    ];
}
