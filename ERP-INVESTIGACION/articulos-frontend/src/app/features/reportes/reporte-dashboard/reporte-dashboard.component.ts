import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/services/auth.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { ReporteService, ReporteResumen } from '../services/reporte.service';
import { Router, RouterLink } from '@angular/router';

@Component({
    selector: 'app-reporte-dashboard',
    standalone: true,
    imports: [CommonModule, HeaderComponent, SidebarComponent, BreadcrumbComponent, PageHeaderComponent, RouterLink],
    templateUrl: './reporte-dashboard.component.html'
})
export class ReporteDashboardComponent implements OnInit {
    sidebarService = inject(SidebarService);
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    router = inject(Router);

    loading = signal(false);
    resumen = signal<ReporteResumen | null>(null);

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' }
    ];

    ngOnInit(): void {
        this.cargarDatos();
    }

    cargarDatos() {
        const idSucursal = this.authService.getSucursalIdFromToken();
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.obtenerResumen(idSucursal.toString()).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    this.resumen.set(res.data);
                    this.initCharts();
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    initCharts() {
        // Aquí se inicializarían los gráficos si estuviéramos usando ApexCharts
    }
}


