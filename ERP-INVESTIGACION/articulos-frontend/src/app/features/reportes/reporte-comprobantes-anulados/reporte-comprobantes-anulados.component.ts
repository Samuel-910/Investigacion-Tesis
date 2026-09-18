import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ReporteBaseComponent } from '../../../shared/components/reporte-base/reporte-base.component';
import { VentaRegistroService } from '../../ventas/services/venta-registro.service';
import { VentaDetalleModalComponent } from '../../ventas/listado-ventas/venta-detalle-modal/venta-detalle-modal.component';


@Component({
    selector: 'app-reporte-comprobantes-anulados',
    standalone: true,
    imports: [CommonModule, ReporteBaseComponent, VentaDetalleModalComponent],
    templateUrl: './reporte-comprobantes-anulados.component.html'
})
export class ReporteComprobantesAnuladosComponent implements OnInit {
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    alertService = inject(AlertService);
    ventaService = inject(VentaRegistroService);

    loading = signal(false);
    sucursalId = signal<string | null>(null);
    puntoId = signal<string | null>(null);
    listaAnulados = signal<any[]>([]);
    
    mes = signal(new Date().getMonth() + 1);
    anio = signal(new Date().getFullYear());

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Anulados', url: '/reportes/comprobantes-anulados' }
    ];

    columnConfigs = [
        { key: 'fecha', label: 'Fecha Anulación' },
        { key: 'tipo', label: 'Tipo' },
        { key: 'serie', label: 'Serie' },
        { key: 'numero', label: 'Número' },
        { key: 'cliente', label: 'Cliente' },
        { key: 'motivo', label: 'Motivo' },
        { key: 'monto', label: 'Importe' },
        { key: 'usuario', label: 'Usuario' },
        { key: 'acciones', label: '' }
    ];

    selectedVenta = signal<any>(null);
    isDetalleModalOpen = signal<boolean>(false);

    verDetalle(idVenta: number) {
        this.loading.set(true);
        this.ventaService.obtenerPorId(idVenta).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success && res.data) {
                    this.selectedVenta.set(res.data);
                    this.isDetalleModalOpen.set(true);
                } else {
                    this.alertService.error('Error', 'No se pudieron cargar los detalles del comprobante');
                }
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error', 'No se pudieron cargar los detalles del comprobante');
            }
        });
    }


    ngOnInit(): void {
        this.cargarReporte();
    }

    cargarReporte() {
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        this.loading.set(true);
        this.reporteService.obtenerComprobantesAnulados(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data.length > 0) {
                    this.listaAnulados.set(res.data.detalle);
                } else {
                    this.listaAnulados.set([]);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al cargar reporte de anulados');
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
        this.reporteService.exportarAnulados(idSucursal.toString(), this.mes(), this.anio(), (this.puntoId() || 'TODOS')).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `ReporteAnulados_${this.anio()}_${this.mes()}.xlsx`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.loading.set(false);
                this.alertService.success('Reporte de Comprobantes Anulados generado correctamente');
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al generar el reporte Excel');
            }
        });
    }
}

