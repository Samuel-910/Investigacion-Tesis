import { Component, OnInit, signal, ViewChild, inject, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { ReporteService, ReporteResumen } from '../services/reporte.service';
import { AuthService } from '../../auth/services/auth.service';
import {
    NgApexchartsModule,
    ChartComponent,
    ApexAxisChartSeries,
    ApexChart,
    ApexXAxis,
    ApexDataLabels,
    ApexStroke,
    ApexYAxis,
    ApexTitleSubtitle,
    ApexFill,
    ApexTooltip,
    ApexMarkers,
    ApexPlotOptions,
    ApexResponsive,
    ApexLegend,
    ApexGrid
} from 'ng-apexcharts';

export type ChartOptions = {
    series: ApexAxisChartSeries | any;
    chart: ApexChart;
    xaxis: ApexXAxis;
    stroke: ApexStroke;
    dataLabels: ApexDataLabels;
    plotOptions: ApexPlotOptions;
    yaxis: ApexYAxis;
    title: ApexTitleSubtitle;
    labels: string[];
    legend: ApexLegend;
    fill: ApexFill;
    tooltip: ApexTooltip;
    responsive: ApexResponsive[];
    markers: ApexMarkers;
    grid: ApexGrid;
    colors: string[];
};

@Component({
    selector: 'app-reporte-interactivo',
    standalone: true,
    imports: [
        CommonModule,
        HeaderComponent,
        SidebarComponent,
        BreadcrumbComponent,
        PageHeaderComponent,
        NgApexchartsModule
    ],
    templateUrl: './reporte-interactivo.component.html',
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ReporteInteractivoComponent implements OnInit {
    @ViewChild('chart') chart!: ChartComponent;
    public lineChartOptions!: Partial<ChartOptions>;
    public barChartOptions!: Partial<ChartOptions>;
    public donutChartOptions!: Partial<ChartOptions>;
    public areaChartOptions!: Partial<ChartOptions>;

    sidebarService = inject(SidebarService);
    reporteService = inject(ReporteService);
    authService = inject(AuthService);

    loading = signal(false);
    resumen = signal<ReporteResumen | null>(null);

    breadcrumbItems = [
        { label: 'Inicio', url: '/venta' },
        { label: 'Reportes', url: '/reportes' },
        { label: 'Interactivo', url: '/reportes/interactivo' }
    ];

    constructor() { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    private cargarDatos() {
        this.loading.set(true);
        const id = this.authService.getSucursalIdFromToken();
        const idSucursal = id ? id.toString() : '1';
        this.reporteService.obtenerResumen(idSucursal).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    const resumenData = res.data;
                    this.resumen.set(resumenData);
                    this.initCharts(resumenData);
                }
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    private initCharts(data: ReporteResumen) {
        // 1. Gráfico de Líneas (Tendencia)
        this.lineChartOptions = {
            series: [
                {
                    name: "Ventas",
                    data: data.ventasMensuales.map(v => v.value)
                },
                {
                    name: "Compras",
                    data: data.comprasMensuales.map(c => c.value)
                }
            ],
            chart: {
                height: 350,
                type: "line",
                zoom: { enabled: true },
                toolbar: { show: true },
                fontFamily: 'Inter, sans-serif'
            },
            dataLabels: { enabled: false },
            stroke: {
                width: [4, 4],
                curve: 'smooth',
                dashArray: [0, 8]
            },
            title: {
                text: 'Tendencia Mensual: Ventas vs Compras',
                align: 'left'
            },
            markers: {
                size: 5,
                hover: { sizeOffset: 6 }
            },
            xaxis: {
                categories: data.ventasMensuales.map(v => v.label),
            },
            yaxis: {
                title: { text: 'Monto (S/)' }
            },
            grid: {
                borderColor: '#f1f1f1',
            },
            colors: ['#6366f1', '#f43f5e']
        };

        // 2. Gráfico de Barras (Top Productos)
        this.barChartOptions = {
            series: [{
                name: "Unidades",
                data: data.topProductosVendidos.map(p => p.value)
            }],
            chart: {
                type: 'bar',
                height: 350,
                fontFamily: 'Inter, sans-serif'
            },
            plotOptions: {
                bar: {
                    borderRadius: 10,
                    horizontal: true,
                    distributed: true
                }
            },
            dataLabels: { enabled: false },
            xaxis: {
                categories: data.topProductosVendidos.map(p => p.label)
            },
            colors: ['#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f59e0b', '#10b981', '#06b6d4', '#3b82f6', '#4f46e5', '#6d28d9']
        };

        // 3. Gráfico de Donas (Proporción Ventas)
        this.donutChartOptions = {
            series: data.topProductosVendidos.slice(0, 5).map(p => p.value),
            chart: {
                type: 'donut',
                height: 350,
                fontFamily: 'Inter, sans-serif'
            },
            labels: data.topProductosVendidos.slice(0, 5).map(p => p.label),
            legend: { position: 'bottom' },
            colors: ['#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f59e0b']
        };

        // 4. Gráfico de Área (Deudas por Proveedor)
        this.areaChartOptions = {
            series: [{
                name: "Deuda",
                data: data.deudasPorProveedor.map(d => d.value)
            }],
            chart: {
                type: 'area',
                height: 350,
                fontFamily: 'Inter, sans-serif',
                toolbar: { show: false }
            },
            dataLabels: { enabled: false },
            stroke: { curve: 'smooth' },
            xaxis: {
                categories: data.deudasPorProveedor.map(d => d.label)
            },
            fill: {
                type: 'gradient',
                gradient: {
                    shadeIntensity: 1,
                    opacityFrom: 0.7,
                    opacityTo: 0.9,
                    stops: [0, 90, 100]
                }
            },
            colors: ['#f59e0b']
        };
    }
}


