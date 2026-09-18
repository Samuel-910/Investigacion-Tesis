import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { SireVenta } from '../services/reporte.service';
import { EstadoGeneral } from '../../../core/interfaces/estado-general.interface';
import { ReporteBaseComponent } from '../../../shared/components/reporte-base/reporte-base.component';

@Component({
    selector: 'app-reporte-ventas',
    standalone: true,
    imports: [CommonModule, ReporteBaseComponent],
    templateUrl: './reporte-ventas.component.html'
})
export class ReporteVentasComponent implements OnInit {
    reporteService = inject(ReporteService);
    authService = inject(AuthService);
    alertService = inject(AlertService);

    loading = signal(false);
    sucursalId = signal<string | null>(null);
    puntoId = signal<string | null>(null);
    listaSireVentas = signal<SireVenta[]>([]);
    
    mes = signal(new Date().getMonth() + 1);
    anio = signal(new Date().getFullYear());

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Ventas', url: '/reportes/ventas' }
    ];

    columnConfigs = [
        { key: 'nroDocCli', label: 'RUC' },
        { key: 'cliente', label: 'NOMBRES Y APELLIDOS' },
        { key: 'periodo', label: 'PERIODO' },
        { key: 'carSunat', label: 'CAR-SUNAT' },
        { key: 'fecha', label: 'FECHA DE EMISION' },
        { key: 'fechaVencimiento', label: 'FECHA DE VENCIMIENTO' },
        { key: 'tipoDoc', label: 'TIPO' },
        { key: 'serie', label: 'SERIE' },
        { key: 'numero', label: 'NUMERO' },
        { key: 'numeroFinal', label: 'NUMERO FINAL' },
        { key: 'tipoRef', label: 'TIPO (REF)' },
        { key: 'numeroRef', label: 'NUMERO (REF)' },
        { key: 'exportacion', label: 'EXPORTACION' },
        { key: 'baseImponible', label: 'BASE IMPONIBLE' },
        { key: 'descuentoBase', label: 'DESC. BASE IMP.' },
        { key: 'igv', label: 'IGV' },
        { key: 'descuentoIgv', label: 'DESC. IGV' },
        { key: 'exonerado', label: 'EXONERADO' },
        { key: 'inafecto', label: 'INAFECTO' },
        { key: 'isc', label: 'I.S.C' },
        { key: 'baseArroz', label: 'BASE ARROZ' },
        { key: 'igvArroz', label: 'IGV ARROZ' },
        { key: 'icbper', label: 'ICBPER' },
        { key: 'otrosConceptos', label: 'OTROS CONCEPTOS' },
        { key: 'total', label: 'IMPORTE TOTAL' },
        { key: 'moneda', label: 'MONEDA' },
        { key: 'tipoCambio', label: 'T.C.' },
        { key: 'fechaRef', label: 'FECHA REF' },
        { key: 'tipoRef_ext', label: 'TIPO REF' },
        { key: 'serieRef_ext', label: 'SERIE REF' },
        { key: 'numeroRef_ext', label: 'NUMERO REF' },
        { key: 'estado', label: 'Estado' }
    ];

    ngOnInit(): void {
        this.cargarSire();
    }

    cargarSire() {
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        const m = this.mes().toString().padStart(2, '0');
        const start = `${this.anio()}-${m}-01`;
        
        const ultimoDia = new Date(this.anio(), this.mes(), 0).getDate();
        const end = `${this.anio()}-${m}-${ultimoDia.toString().padStart(2, '0')}`;

        this.reporteService.obtenerSireVentas(idSucursal.toString(), start, end, idPuntoVenta).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.listaSireVentas.set(res.data);
                }
            }
        });
    }

    onFiltroChange(event: { mes: number, anio: number, idSucursal?: string, idPuntoVenta?: string }) {
        if (event.idSucursal) this.sucursalId.set(event.idSucursal);
        if (event.idPuntoVenta) this.puntoId.set(event.idPuntoVenta);
        this.mes.set(event.mes);
        this.anio.set(event.anio);
        this.cargarSire();
    }

    exportarExcel(event?: { idSucursal?: string, idPuntoVenta?: string }) {
        if (event?.idSucursal) this.sucursalId.set(event.idSucursal);
        if (event?.idPuntoVenta) this.puntoId.set(event.idPuntoVenta);
        const idSucursal = this.sucursalId() || this.authService.getSucursalIdFromToken()?.toString();
        const idPuntoVenta = this.puntoId() || 'TODOS';
        if (!idSucursal) return;

        const m = this.mes().toString().padStart(2, '0');
        const start = `${this.anio()}-${m}-01`;
        
        const ultimoDia = new Date(this.anio(), this.mes(), 0).getDate();
        const end = `${this.anio()}-${m}-${ultimoDia.toString().padStart(2, '0')}`;

        this.loading.set(true);
        this.reporteService.exportarRVIE(idSucursal.toString(), start, end, idPuntoVenta).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `RVIE_${this.anio()}_${m}.xlsx`;
                a.click();
                window.URL.revokeObjectURL(url);
                this.loading.set(false);
                this.alertService.success('Reporte RVIE generado correctamente');
            },
            error: () => {
                this.loading.set(false);
                this.alertService.error('Error al generar el reporte Excel');
            }
        });
    }

    isVigente(estado: string | EstadoGeneral): boolean {
        if (!estado) return false;
        if (typeof estado === 'string') return estado === 'V';
        return (estado as any).valor === 1;
    }
}

