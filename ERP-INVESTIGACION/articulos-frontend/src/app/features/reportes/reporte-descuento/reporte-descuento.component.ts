import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ReporteBaseComponent } from '../../../shared/components/reporte-base/reporte-base.component';

@Component({
    selector: 'app-reporte-descuento',
    standalone: true,
    imports: [CommonModule, ReporteBaseComponent],
    templateUrl: './reporte-descuento.component.html'
})
export class ReporteDescuentoComponent implements OnInit {
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    alertService = inject(AlertService);

    loading = signal(false);
    sucursalId = signal<string | null>(null);
    puntoId = signal<string | null>(null);
    listaDescuentos = signal<any[]>([]);
    
    mes = signal(new Date().getMonth() + 1);
    anio = signal(new Date().getFullYear());

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Descuentos', url: '/reportes/descuento' }
    ];

    columnConfigs = [
        { key: 'fecha', label: 'Fecha' },
        { key: 'comprobante', label: 'Documento' },
        { key: 'cliente', label: 'Cliente' },
        { key: 'motivo', label: 'Sustento / Cupón' },
        { key: 'base', label: 'Venta Base' },
        { key: 'descuento', label: 'Mto. Desc.' }
    ];

    ngOnInit(): void {
        this.cargarReporte();
    }

    cargarReporte() {
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.obtenerReporteDescuento(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data.length > 0) {
                    this.listaDescuentos.set(res.data.detalle);
                } else {
                    this.listaDescuentos.set([]);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al cargar reporte de descuentos');
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
        this.reporteService.exportarReporteDescuentos(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `ReporteDescuentos_${this.anio()}_${this.mes()}.xlsx`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.loading.set(false);
                this.alertService.success('Reporte de Descuentos generado correctamente');
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al generar el reporte Excel');
            }
        });
    }
}

