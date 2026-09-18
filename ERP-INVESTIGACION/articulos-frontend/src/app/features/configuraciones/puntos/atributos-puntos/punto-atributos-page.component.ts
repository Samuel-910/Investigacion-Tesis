import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; import { TipoService, ProcesoService } from '../../../almacen/service/atributo.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from "../../../../shared/components/page-header/page-header";
import { BreadcrumbComponent, BreadcrumbItem } from "../../../../shared/components/breadcrumb/breadcrumb";
import { AtributoListComponent } from '../../../../shared/atributos/components/atributo-list/atributo-list.component';

@Component({
    selector: 'app-punto-atributos-page',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        AtributoListComponent,
        PageHeaderComponent,
        BreadcrumbComponent
    ],
    templateUrl: './punto-atributos-page.component.html'
})
export class PuntoAtributosPageComponent {
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuración', route: '/configuraciones' },
        { label: 'Puntos', route: '/configuraciones/puntos' },
        { label: 'Atributos' },
    ];

    activeTab = signal('TIPOS');

    tabs = [
        { id: 'TIPOS', label: 'Tipos' },
        { id: 'PROCESOS', label: 'Procesos' }
    ];

    constructor(
        public sidebarService: SidebarService,
        public tipoService: TipoService,
        public procesoService: ProcesoService
    ) { }

    setActiveTab(tabId: string) {
        this.activeTab.set(tabId);
    }
}
