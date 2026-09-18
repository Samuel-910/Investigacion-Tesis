import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ReporteBaseComponent } from '../../../shared/components/reporte-base/reporte-base.component';

@Component({
    selector: 'app-reporte-cajas',
    standalone: true,
    imports: [CommonModule, ReporteBaseComponent],
    templateUrl: './reporte-cajas.component.html'
})
export class ReporteCajasComponent implements OnInit {
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    alertService = inject(AlertService);

    loading = signal(false);
    sucursalId = signal<string | null>(null);
    puntoId = signal<string | null>(null);
    listaMovimientos = signal<any[]>([]);
    
    mes = signal(new Date().getMonth() + 1);
    anio = signal(new Date().getFullYear());

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Cajas', url: '/reportes/cajas' }
    ];

    columnConfigs = [
        { key: 'nombre', label: 'Caja' },
        { key: 'usuario', label: 'Usuario / Cajero' },
        { key: 'fechaApertura', label: 'Fecha Apertura' },
        { key: 'fechaCierre', label: 'Fecha Cierre / Arqueo' },
        { key: 'saldoInicial', label: 'Saldo Inicial' },
        { key: 'saldoTeorico', label: 'Saldo Teórico' },
        { key: 'saldoReal', label: 'Saldo Real (Arqueo)' },
        { key: 'diferencia', label: 'Diferencia / Descuadre' }
    ];

    ngOnInit(): void {
        this.cargarReporte();
    }

    cargarReporte() {
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.obtenerReporteCajas(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    this.listaMovimientos.set(res.data);
                } else {
                    this.listaMovimientos.set([]);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al cargar reporte de cajas');
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
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.exportarReporteCajas(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `ReporteCajas_${this.anio()}_${this.mes()}.xlsx`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.loading.set(false);
                this.alertService.success('Reporte de Cajas generado correctamente');
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al generar el reporte Excel');
            }
        });
    }
}

