import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Clinica, ClinicaAuditoria } from '../../models/empresa.model';
import { ClinicaService } from '../../services/clinica.service';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../../core/services/alert.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { ClinicaFormComponent } from '../clinica-form/clinica-form.component';

@Component({
    selector: 'app-empresa-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        ModalComponent,
        ClinicaFormComponent
    ],
    templateUrl: './clinica-list.component.html'
})
export class ClinicaListComponent implements OnInit {
    clinica = signal<Clinica | null>(null);
    historial = signal<ClinicaAuditoria[]>([]);
    loading = signal(false);
    showModal = signal(false);
    clinicaSeleccionada: Clinica | null = null;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/configuraciones' },
        { label: 'Datos de la Clínica' }
    ];

    columnsHistorial: Columna[] = [
        { field: 'usuario', header: 'Usuario', tipo: 'text', subField: [] },
        { field: 'fechaCambio', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'operacion', header: 'Acción', tipo: 'badge', subField: [], badgeConfig: { from: 'bg-blue-100', to: 'bg-blue-200', text: 'text-blue-800' } }
    ];

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Razón Social', value: 'RAZON_SOCIAL' },
        { label: 'RUC', value: 'RUC' }
    ];

    constructor(
        private clinicaService: ClinicaService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarClinica();
    }

    cargarClinica(): void {
        this.loading.set(true);
        this.clinicaService.obtenerPrincipal().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.clinica.set(res.data);
                    this.cargarHistorial(res.data.id!);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar datos de la clínica', 'error');
            }
        });
    }

    cargarHistorial(idClinica: number): void {
        this.clinicaService.obtenerHistorial(idClinica).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.historial.set(res.data.content || []);
                }
            },
        });
    }

    onSearch(event: { q: string, type: string }): void {
        this.cargarClinica();
    }

    cambiarPagina(page: number): void {
        this.cargarClinica();
    }

    abrirModalNuevo(): void {
        this.clinicaSeleccionada = null;
        this.showModal.set(true);
    }

    abrirModalEditar(clinica: Clinica): void {
        this.clinicaSeleccionada = clinica;
        this.showModal.set(true);
    }

    guardarClinica(clinica: Clinica): void {
        if (!clinica.id) {
            this.alertService.toast('Error: No se pudo identificar la clínica a actualizar', 'error');
            return;
        }

        this.clinicaService.actualizar(clinica.id, clinica).subscribe({
            next: (res) => {
                this.alertService.toast(res.message || 'Datos actualizados', 'success');
                this.showModal.set(false);
                this.cargarClinica();
            },
            error: () => this.alertService.toast('Error al actualizar datos', 'error')
        });
    }

    obtenerLogoPrincipal(): string | null {
        const c = this.clinica();
        if (!c) return null;
        return c.logoPrincipal === 'RECTANGULAR' ? (c.logoRectangular || null) : (c.logoCuadrado || null);
    }
}
