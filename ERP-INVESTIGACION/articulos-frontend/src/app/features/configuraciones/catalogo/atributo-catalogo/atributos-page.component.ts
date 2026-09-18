import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import {
    CategoriaService,
    LaboratorioService,
    PrincipioActivoService,
    AccionTerapeuticaService,
    UbicacionService
} from '../../../almacen/service/atributo.service';
import { AtributoListComponent } from '../../../../shared/atributos/components/atributo-list/atributo-list.component';
@Component({
    selector: 'app-atributos-page',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        AtributoListComponent,
        BreadcrumbComponent,
        PageHeaderComponent
    ],
    templateUrl: './atributos-page.component.html'
})
export class AtributosPageComponent {
    activeTab = signal('CATEGORIAS');

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Almacén', route: '/almacen' },
        { label: 'Atributos' }
    ];

    tabs = [
        { id: 'CATEGORIAS', label: 'Categorías' },
        { id: 'LABORATORIOS', label: 'Laboratorios' },
        { id: 'PRINCIPIOS', label: 'Principios Activos' },
        { id: 'ACCIONES', label: 'Acciones Terapéuticas' },
        { id: 'UBICACIONES', label: 'Ubicaciones' }
    ];

    constructor(
        public sidebarService: SidebarService,
        public categoriaService: CategoriaService,
        public laboratorioService: LaboratorioService,
        public principioActivoService: PrincipioActivoService,
        public accionTerapeuticaService: AccionTerapeuticaService,
        public ubicacionService: UbicacionService
    ) { }

    setActiveTab(tabId: string) {
        this.activeTab.set(tabId);
    }
}
