import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ReporteBaseComponent } from '../../../shared/components/reporte-base/reporte-base.component';

@Component({
    selector: 'app-reporte-nota-credito',
    standalone: true,
    imports: [CommonModule, ReporteBaseComponent],
    templateUrl: './reporte-nota-credito.component.html'
})
export class ReporteNotaCreditoComponent implements OnInit {
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    alertService = inject(AlertService);

    loading = signal(false);
    sucursalId = signal<string | null>(null);
    puntoId = signal<string | null>(null);
    listaNotas = signal<any[]>([]);
    
    mes = signal(new Date().getMonth() + 1);
    anio = signal(new Date().getFullYear());

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Notas de Crédito', url: '/reportes/nota-credito' }
    ];

    columnConfigs = [
        { key: 'fecha', label: 'Fecha Emisión' },
        { key: 'nc', label: 'Nro. Nota' },
        { key: 'docRef', label: 'Doc. Referencia' },
        { key: 'cliente', label: 'Cliente' },
        { key: 'motivo', label: 'Motivo SUNAT' },
        { key: 'monto', label: 'Monto NC' }
    ];

    ngOnInit(): void {
        this.cargarReporte();
    }

    cargarReporte() {
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.obtenerNotasCredito(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data.length > 0) {
                    this.listaNotas.set(res.data.detalle);
                } else {
                    this.listaNotas.set([]);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al cargar reporte de notas de crédito');
            }
        });
    }

    onFiltroChange(event: { mes: number, anio: number, idSucursal?: string, idPuntoVenta?: string }) {
        if (event.idSucursal) this.sucursalId.set(event.idSucursal);
        if (event.idPuntoVenta) this.puntoId.set(event.idPuntoVenta);
        this.mes.set(event.mes);
        this.anio.set(event.anio);
        this.cargarReporte();
    }

    exportarExcel(event?: { idSucursal?: string, idPuntoVenta?: string }) {
        if (event?.idSucursal) this.sucursalId.set(event.idSucursal);
        if (event?.idPuntoVenta) this.puntoId.set(event.idPuntoVenta);
        this.alertService.info('Exportación de notas de crédito en desarrollo');
    }
}

