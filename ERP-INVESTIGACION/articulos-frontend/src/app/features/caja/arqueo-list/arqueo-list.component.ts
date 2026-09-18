import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaChicaService } from '../services/caja-chica.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../core/services/alert.service';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { CajaChica } from '../models/caja-chica.model';
import { CajaMovimientosModalComponent } from '../caja-chica/caja-movimientos-modal/caja-movimientos-modal.component';
import { CajaArqueoModalComponent } from './caja-arqueo-modal/caja-arqueo-modal.component';

@Component({
    selector: 'app-arqueo-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        SearchGenericComponent,
        BreadcrumbComponent,
        CajaArqueoModalComponent,
        CajaMovimientosModalComponent
    ],
    templateUrl: './arqueo-list.component.html'
})
export class ArqueoListComponent implements OnInit {
    cajas = signal<CajaChica[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);
    searchTerm = signal('');
    showArqueoModal = signal(false);
    showMovimientosModal = signal(false);
    selectedCajaId = signal<number | null>(null);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja', route: '/caja' },
        { label: 'Arqueo de Caja' }
    ];

    columns: Columna[] = [
        { field: 'nombre', header: 'Caja', tipo: 'text', subField: [] },
        { field: 'createdAt', header: 'Apertura', tipo: 'date', subField: [] },
        { field: 'idUsuarioCajero', header: 'Usuario', tipo: 'text', subField: [] },
        { field: 'saldoInicial', header: 'Saldo Inicial', tipo: 'currency', subField: [] },
        { field: 'saldoActual', header: 'Saldo Actual', tipo: 'currency', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre', value: 'NOMBRE' }
    ];

    constructor(
        private cajaService: CajaChicaService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarCajas();
    }

    cargarCajas(): void {
        this.loading.set(true);
        this.cajaService.listarCajas().subscribe({
            next: (res) => {
                if (res.success) {
                    this.cajas.set(res.data);
                    this.totalElements.set(res.data.length);
                    this.totalPages.set(1);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar cajas', 'error');
            }
        });
    }

    onSearch(event: { q: string, type: string }): void {
        this.searchTerm.set(event.q);
        this.cargarCajas();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarCajas();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(Number(size));
        this.currentPage.set(0);
        this.cargarCajas();
    }

    cerrarCaja(caja: CajaChica): void {
        if (!caja.id) return;
        this.selectedCajaId.set(caja.id);
        this.showArqueoModal.set(true);
    }

    onArqueoDone(success: boolean): void {
        this.showArqueoModal.set(false);
        if (success) {
            this.cargarCajas();
        }
    }

    verMovimientos(caja: CajaChica): void {
        if (!caja.id) return;
        this.selectedCajaId.set(caja.id);
        this.showMovimientosModal.set(true);
    }
}
