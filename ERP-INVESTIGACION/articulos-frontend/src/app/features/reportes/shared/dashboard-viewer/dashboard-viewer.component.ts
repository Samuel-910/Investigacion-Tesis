import { Component, Input, OnInit, signal, inject, CUSTOM_ELEMENTS_SCHEMA, effect, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { NgApexchartsModule } from 'ng-apexcharts';
import { ReporteService, WidgetConfig } from '../../services/reporte.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ThemeService } from '../../../../core/services/theme.service';

@Component({
    selector: 'app-dashboard-viewer',
    standalone: true,
    imports: [CommonModule, NgApexchartsModule],
    template: `
        <div class="grid grid-cols-12 gap-6 mt-6 items-start">
            <div *ngIf="loading()" class="col-span-12 flex justify-center py-20">
                <i class="fas fa-spinner fa-spin text-4xl text-sky-500"></i>
            </div>
            
            <div *ngFor="let w of widgets(); let i = index"
                [ngClass]="'col-span-12 lg:col-span-' + w.config.columns"
                class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-slate-200 dark:border-slate-700 relative group transition-all duration-300">
                
                <div class="chart-container overflow-visible" [style.height.px]="(w.config.height && w.config.height < 10) ? w.config.height * 175 : (w.config.height || 350)">
                    <!-- Renderizado para CARDS -->
                    <div *ngIf="w.config.tipoGrafico === 'CARD'"
                        class="h-full flex flex-col items-center justify-center text-center p-4">
                        <h4 class="text-slate-500 font-bold uppercase text-xs mb-2">{{w.config.titulo}}</h4>
                        <div class="text-4xl font-black text-sky-600 mb-2">
                            {{w.chartOptions.total | number:'1.2-2'}}{{w.config.metrica === 'RENTABILIDAD' ? '%' : ''}}
                        </div>
                        <p class="text-slate-400 text-xs font-medium">
                            {{w.config.metrica === 'RENTABILIDAD' ? 'Margen de Rentabilidad' : 'Total Acumulado'}}
                        </p>
                    </div>

                    <!-- Renderizado para TABLES -->
                    <div *ngIf="w.config.tipoGrafico === 'TABLE'" class="h-full flex flex-col pt-2">
                        <h4 class="text-slate-800 dark:text-white font-bold text-sm mb-4 px-2">{{w.config.titulo}}</h4>
                        <div class="overflow-auto flex-1 custom-scrollbar">
                            <table class="w-full text-left text-xs">
                                <thead class="sticky top-0 bg-white dark:bg-slate-800 z-10">
                                    <tr class="text-slate-400 border-b border-slate-100 dark:border-slate-700">
                                        <th class="pb-2 font-bold uppercase px-2">Concepto</th>
                                        <th class="pb-2 font-bold uppercase text-right px-2">Monto</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-50 dark:divide-slate-700/50">
                                    <tr *ngFor="let item of w.chartOptions.data" class="hover:bg-slate-50 dark:hover:bg-slate-700/30 transition-colors">
                                        <td class="py-2 px-2 font-medium text-slate-700 dark:text-slate-300">{{item.label}}</td>
                                        <td class="py-2 px-2 text-right font-bold text-sky-600">{{item.value | number:'1.2-2'}}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Renderizado para GRÁFICOS APEX -->
                    <apx-chart *ngIf="w.config.tipoGrafico !== 'CARD' && w.config.tipoGrafico !== 'TABLE'"
                        [series]="w.chartOptions.series" [chart]="w.chartOptions.chart"
                        [xaxis]="w.chartOptions.xaxis" [labels]="w.chartOptions.labels"
                        [title]="w.chartOptions.title" [colors]="w.chartOptions.colors"
                        [plotOptions]="w.chartOptions.plotOptions" [stroke]="w.chartOptions.stroke"
                        [dataLabels]="w.chartOptions.dataLabels" [markers]="w.chartOptions.markers"
                        [theme]="w.chartOptions.theme" [width]="'100%'" [height]="'100%'">
                    </apx-chart>
                </div>
            </div>
        </div>
    `,
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class DashboardViewerComponent implements OnInit {
    private _categoria: string = 'GENERAL';
    @Input() set categoria(val: string) {
        this._categoria = val;
        if (this.idSucursal) {
            this.cargarDashboard();
        }
    }
    get categoria(): string { return this._categoria; }

    private reporteService = inject(ReporteService);
    private authService = inject(AuthService);
    private themeService = inject(ThemeService);
    private cdr = inject(ChangeDetectorRef);

    loading = signal(false);
    widgets = signal<any[]>([]);
    idSucursal = '';
    idPuntoVenta = 'TODOS';

    ngOnInit() {
        const id = this.authService.getSucursalIdFromToken();
        this.idSucursal = id ? id.toString() : '1';
        const idP = this.authService.getPuntoIdFromToken();
        this.idPuntoVenta = idP ? idP.toString() : 'TODOS';
        this.cargarDashboard();

        // Efecto para re-construir opciones de gráficos cuando el tema cambia
        effect(() => {
            const isDark = this.themeService.isDark();
            const currentWidgets = this.widgets();
            if (currentWidgets.length > 0) {
                const updatedWidgets = currentWidgets.map(w => ({
                    ...w,
                    chartOptions: {
                        ...w.chartOptions,
                        theme: { ...w.chartOptions.theme, mode: isDark ? 'dark' : 'light' }
                    }
                }));
                this.widgets.set(updatedWidgets);
            }
        });
    }

    cargarDashboard() {
        if (!this.idSucursal) {
            const id = this.authService.getSucursalIdFromToken();
            this.idSucursal = id ? id.toString() : '1';
        const idP = this.authService.getPuntoIdFromToken();
        this.idPuntoVenta = idP ? idP.toString() : 'TODOS';
        }

        const catUpperCase = (this.categoria || 'GENERAL').toUpperCase();

        this.loading.set(true);
        this.reporteService.obtenerConfiguracion(Number(this.idSucursal), catUpperCase).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    this.fetchWidgetData(res.data.widgets || []);
                } else {
                    this.widgets.set([]);
                    this.loading.set(false);
                }
            },
            error: () => {
                this.widgets.set([]);
                this.loading.set(false);
            }
        });
    }

    fetchWidgetData(widgetConfigs: WidgetConfig[]) {
        const loadedWidgets: any[] = [];
        let count = 0;

        if (widgetConfigs.length === 0) {
            this.loading.set(false);
            return;
        }

        widgetConfigs.forEach(config => {
            const isMultiseries = config.metrica === 'VENTAS_VS_COMPRAS';
            const dataObservable = (isMultiseries
                ? this.reporteService.obtenerDatosMultiseries(config.metrica, config.dimension, this.idSucursal, this.idPuntoVenta)
                : this.reporteService.obtenerDatosDinamicos(config.metrica, config.dimension, this.idSucursal, this.idPuntoVenta)) as Observable<any>;

            dataObservable.subscribe({
                next: (res: any) => {
                    count++;
                    if (res.success && res.data) {
                        const actualData = isMultiseries ? res.data : res.data;
                        loadedWidgets.push({
                            config,
                            chartOptions: this.buildChartOptions(config, actualData)
                        });
                        this.widgets.set([...loadedWidgets].sort((a, b) => a.config.orden - b.config.orden));
                    }
                    if (count === widgetConfigs.length) {
                        this.loading.set(false);
                        this.cdr.detectChanges();
                        setTimeout(() => {
                            window.dispatchEvent(new Event('resize'));
                        }, 100);
                    }
                },
                error: () => {
                    count++;
                    if (count === widgetConfigs.length) {
                        this.loading.set(false);
                        this.cdr.detectChanges();
                    }
                }
            });
        });
    }

    buildChartOptions(config: WidgetConfig, data: any) {
        let labels: string[] = [];
        let series: any[] = [];
        let values: number[] = [];
        const isMultiseries = config.metrica === 'VENTAS_VS_COMPRAS';
        let type: any = config.tipoGrafico.toLowerCase();
        const isDark = this.themeService.isDark();

        if (type === 'column') type = 'bar';
        if (type === 'polar_area') type = 'polarArea';

        if (type === 'card' || type === 'table') {
            const currentValues = isMultiseries && data.series ? data.series[0].data : data.map((d: any) => d.value);
            return {
                data: isMultiseries ? data : data,
                total: currentValues.reduce((a: number, b: number) => a + (Number(b) || 0), 0)
            };
        }

        if (isMultiseries && data.labels) {
            labels = data.labels;
            series = data.series;
            values = data.series[0].data;
        } else {
            labels = data.map((d: any) => d.label);
            values = data.map((d: any) => d.value);

            if (config.tipoGrafico === 'TREEMAP') {
                series = [{ data: data.map((d: any) => ({ x: d.label, y: d.value })) }];
            } else if (['DONUT', 'PIE', 'POLAR_AREA', 'RADIALBAR', 'GAUGE'].includes(config.tipoGrafico)) {
                series = values;
            } else {
                series = [{ name: config.metrica, data: values }];
            }
        }

        return {
            series: series,
            chart: {
                type: type === 'gauge' ? 'radialBar' : type,
                height: (config.height && config.height < 10) ? config.height * 175 : (config.height || 350),
                width: '100%',
                toolbar: { show: false },
                animations: { enabled: true },
                background: 'transparent',
                foreColor: isDark ? '#94a3b8' : '#64748b'
            },
            labels: labels,
            xaxis: {
                categories: labels,
                labels: {
                    show: true,
                    style: { fontSize: '10px', fontWeight: 600 },
                    hideOverlappingLabels: true,
                    rotate: -45,
                    trim: true
                }
            },
            colors: ['#0ea5e9', '#f43f5e', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4'],
            theme: { mode: isDark ? 'dark' : 'light', palette: 'palette1' },
            stroke: { curve: 'smooth', width: config.tipoGrafico === 'BAR' || config.tipoGrafico === 'COLUMN' ? 0 : 3 },
            dataLabels: {
                enabled: true,
                formatter: function (val: any) {
                    if (['PIE', 'DONUT', 'POLAR_AREA'].includes(config.tipoGrafico)) {
                        return val.toFixed(1) + '%';
                    }
                    return val;
                },
                style: { fontSize: '12px', fontFamily: 'Inter, sans-serif', fontWeight: 'bold' }
            },
            title: { text: config.titulo, align: 'left', style: { fontSize: '16px', fontWeight: 'bold' } },
            plotOptions: {
                pie: {
                    donut: {
                        labels: {
                            show: true,
                            total: {
                                show: true,
                                label: 'TOTAL',
                                formatter: () => values.reduce((a, b) => a + b, 0).toFixed(2)
                            }
                        }
                    }
                },
                bar: {
                    horizontal: config.tipoGrafico === 'BAR',
                    borderRadius: 4,
                    dataLabels: { position: 'top' },
                    distributed: config.tipoGrafico === 'BAR' // Mejora visual
                }
            },
            grid: {
                padding: {
                    top: 20,
                    right: 40,
                    bottom: 20,
                    left: 20
                }
            }
        };
    }
}


