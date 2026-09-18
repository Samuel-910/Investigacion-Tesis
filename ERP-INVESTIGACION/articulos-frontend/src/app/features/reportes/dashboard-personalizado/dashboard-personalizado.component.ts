import { Component, OnInit, signal, inject, CUSTOM_ELEMENTS_SCHEMA, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { NgApexchartsModule } from 'ng-apexcharts';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { ReporteService, WidgetConfig, DashboardConfig } from '../services/reporte.service';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../../core/services/alert.service';
import { AuthService } from '../../auth/services/auth.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { ActivatedRoute } from '@angular/router';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
    selector: 'app-dashboard-personalizado',
    standalone: true,
    imports: [
        CommonModule,
        NgApexchartsModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        BreadcrumbComponent
    ],
    templateUrl: './dashboard-personalizado.component.html',
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class DashboardPersonalizadoComponent implements OnInit {
    private reporteService = inject(ReporteService);
    private alertService = inject(AlertService);
    private authService = inject(AuthService);
    public sidebarService = inject(SidebarService);
    private route = inject(ActivatedRoute);
    private themeService = inject(ThemeService);

    isEditing = signal(false);
    widgets = signal<any[]>([]);
    dashboardName = signal('Dashboard Personalizado');
    idSucursal = '';
    idDashboard: number | null = null;
    categoria = signal('GENERAL');
    widgetEnEdicionIndex = signal<number | null>(null);

    // Variables para Drag and Drop
    draggedIndex: number | null = null;

    // Variables para Resizing
    resizingIndex: number | null = null;
    startX = 0;
    startY = 0;
    startColumns = 0;
    startHeight = 0;

    breadcrumbItems = signal<BreadcrumbItem[]>([
        { label: 'Inicio', route: '/venta' },
        { label: 'Reportes', route: '/reportes' },
        { label: 'Dashboard' }
    ]);

    // Opciones para el configurador
    tiposGrafico: { id: WidgetConfig['tipoGrafico'], icon: string, label: string }[] = [
        { id: 'BAR', icon: 'fa-chart-bar', label: 'Barra' },
        { id: 'COLUMN', icon: 'fa-chart-column', label: 'Columna' },
        { id: 'LINE', icon: 'fa-chart-line', label: 'Línea' },
        { id: 'AREA', icon: 'fa-chart-area', label: 'Área' },
        { id: 'PIE', icon: 'fa-chart-pie', label: 'Pastel' },
        { id: 'DONUT', icon: 'fa-circle-notch', label: 'Dona' },
        { id: 'RADAR', icon: 'fa-circle-dot', label: 'Radar' },
        { id: 'POLAR_AREA', icon: 'fa-sun', label: 'Área Polar' },
        { id: 'SCATTER', icon: 'fa-braille', label: 'Dispersión' },
        { id: 'HEATMAP', icon: 'fa-table-cells', label: 'Mapa Calor' },
        { id: 'TREEMAP', icon: 'fa-table-columns', label: 'Treemap' },
        { id: 'GAUGE', icon: 'fa-gauge-high', label: 'Medidor' },
        { id: 'CARD', icon: 'fa-square-poll-vertical', label: 'Tarjeta' },
        { id: 'TABLE', icon: 'fa-table', label: 'Tabla' }
    ];
    metricas: WidgetConfig['metrica'][] = [];
    dimensiones: WidgetConfig['dimension'][] = [];

    // Widget en edición
    nuevoWidget: WidgetConfig = {
        titulo: 'Nuevo Gráfico',
        tipoGrafico: 'BAR',
        metrica: 'VENTAS',
        dimension: 'TIEMPO_MES',
        orden: 0,
        columns: 6,
        height: 350
    };

    ngOnInit() {
        const id = this.authService.getSucursalIdFromToken();
        this.idSucursal = id ? id.toString() : '1';

        this.route.params.subscribe(params => {
            const id = params['id'];
            if (id && !isNaN(id)) {
                this.idDashboard = Number(id);
                this.cargarDashboardPorId(this.idDashboard);
            } else if (params['categoria']) {
                this.categoria.set(params['categoria'].toUpperCase());
                this.actualizarMetadatos();
                this.cargarDashboard();
            } else {
                this.categoria.set('PRINCIPAL');
                this.actualizarMetadatos();
                this.cargarDashboard();
            }
        });

    }

    actualizarMetadatos() {
        const cat = this.categoria();
        let label = 'General';

        if (cat === 'VENTAS') {
            label = 'Ventas';
        }
        else if (cat === 'COMPRAS') {
            label = 'Compras';
        }
        else if (cat === 'PRODUCTOS') {
            label = 'Productos';
        }
        else if (cat === 'FINANZAS' || cat === 'CAJA') {
            label = cat === 'FINANZAS' ? 'Finanzas' : 'Caja';
        }
        else if (cat === 'PRINCIPAL') {
            label = 'Principal';
        }

        // Obtener las opciones desde el backend
        this.reporteService.obtenerOpciones(cat).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.metricas = res.data.metricas;
                    this.dimensiones = res.data.dimensiones;
                }
            }
        });

        this.dashboardName.set(`Dashboard ${label}`);

        const items: any[] = [
            { label: 'Inicio', route: '/dashboard/Principal' }
        ];

        if (cat !== 'PRINCIPAL') {
            items.push({ label: 'Reportes', route: '/reportes' });
        }

        items.push({ label: `Dashboard ${label}` });

        this.breadcrumbItems.set(items);
    }

    cargarDashboard() {
        const idSucursal = this.authService.currentUserValue?.sucursalId ||
            this.authService.getSucursalIdFromToken() ||
            Number(this.idSucursal);

        if (!idSucursal) return;
        this.idSucursal = idSucursal.toString();

        this.reporteService.obtenerConfiguracion(Number(idSucursal), this.categoria()).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    const config = res.data;
                    this.idDashboard = config.id;
                    this.dashboardName.set(config.nombre);
                    this.fetchWidgetData(config.widgets || []);
                } else {
                    this.widgets.set([]);
                }
            },
            error: (err) => {
                console.error('Error al cargar dashboard:', err);
                this.widgets.set([]);
            }
        });
    }

    cargarDashboardPorId(id: number) {
        this.reporteService.obtenerConfiguracionPorId(id).subscribe({
            next: (res) => {
                if (res.success && res.data && res.data) {
                    const config = res.data;
                    this.dashboardName.set(config.nombre);
                    this.actualizarMetadatos();
                    this.fetchWidgetData(config.widgets || []);
                }
            }
        });
    }

    fetchWidgetData(widgetConfigs: WidgetConfig[]) {
        const loadedWidgets: any[] = [];

        widgetConfigs.forEach(config => {
            const isMultiseries = config.metrica === 'VENTAS_VS_COMPRAS';
            const dataObservable = (isMultiseries
                ? this.reporteService.obtenerDatosMultiseries(config.metrica, config.dimension, this.idSucursal.toString(), 'TODOS')
                : this.reporteService.obtenerDatosDinamicos(config.metrica, config.dimension, this.idSucursal.toString(), 'TODOS')) as Observable<any>;

            dataObservable.subscribe({
                next: (res: any) => {
                    if (res.success && res.data) {
                        const actualData = isMultiseries ? res.data : res.data;
                        loadedWidgets.push({
                            config,
                            chartOptions: this.buildChartOptions(config, actualData)
                        });
                        this.widgets.set([...loadedWidgets].sort((a, b) => a.config.orden - b.config.orden));
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
        const isDark = this.themeService.isDark();

        let type: any = config.tipoGrafico.toLowerCase();
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
            // Para totales en cards o donuts multiseries (si aplica)
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

        let options: any = {
            series: series,
            chart: {
                type: type === 'gauge' ? 'radialBar' : type,
                height: config.height || 350,
                width: '100%',
                toolbar: { show: false },
                animations: { enabled: true },
                redrawOnParentResize: true,
                background: 'transparent',
                foreColor: isDark ? '#94a3b8' : '#64748b'
            },
            labels: labels,
            xaxis: {
                categories: labels,
                labels: {
                    show: true,
                    style: {
                        fontSize: '10px',
                        fontWeight: 600
                    },
                    hideOverlappingLabels: true,
                    rotate: -45,
                    trim: true
                }
            },
            colors: ['#6366f1', '#f43f5e', '#f59e0b', '#10b981', '#8b5cf6', '#06b6d4', '#ec4899'],
            theme: { mode: isDark ? 'dark' : 'light', palette: 'palette1' },
            stroke: { curve: 'smooth', width: config.tipoGrafico === 'BAR' || config.tipoGrafico === 'COLUMN' ? 0 : 3 },
            dataLabels: {
                enabled: true,
                formatter: function (val: any, opts: any) {
                    // Si es circular, Apex ya calcula el porcentaje en 'val'
                    if (['PIE', 'DONUT', 'POLAR_AREA'].includes(config.tipoGrafico)) {
                        return val.toFixed(1) + '%';
                    }
                    return val;
                },
                style: {
                    fontSize: '12px',
                    fontFamily: 'Inter, sans-serif',
                    fontWeight: 'bold',
                },
                dropShadow: { enabled: true }
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
                    },
                    dataLabels: {
                        offset: -5 // Mueve la etiqueta adentro del slice
                    }
                },
                bar: {
                    horizontal: config.tipoGrafico === 'BAR',
                    columnWidth: '55%',
                    borderRadius: 4,
                    dataLabels: {
                        position: 'top'
                    },
                    distributed: config.tipoGrafico === 'BAR'
                },
                radialBar: {
                    startAngle: -135,
                    endAngle: 135,
                    dataLabels: {
                        name: { fontSize: '16px', color: undefined, offsetY: 120 },
                        value: {
                            offsetY: 76,
                            fontSize: '22px',
                            color: undefined,
                            formatter: (val: any) => val
                        }
                    }
                },
                radar: {
                    size: (config.height || 350) * 0.35, // Ajuste dinámico de tamaño
                    offsetY: 20,
                    polygons: {
                        strokeColors: isDark ? '#334155' : '#e8e8e8',
                        strokeWidth: 1,
                        connectorColors: isDark ? '#334155' : '#e8e8e8',
                        fill: {
                            colors: isDark ? ['#1e293b', '#0f172a'] : ['#f8f8f8', '#fff']
                        }
                    }
                }
            },
            grid: {
                padding: {
                    top: 20,
                    right: 40,
                    bottom: 20,
                    left: 20
                }
            },
            markers: {
                size: 4,
                colors: ['#6366f1'],
                strokeColor: '#6366f1',
                strokeWidth: 2,
            }
        };

        return options;
    }

    agregarWidget() {
        const config = { ...this.nuevoWidget };
        const isMultiseries = config.metrica === 'VENTAS_VS_COMPRAS';

        const dataObservable = (isMultiseries
            ? this.reporteService.obtenerDatosMultiseries(config.metrica, config.dimension, this.idSucursal.toString(), 'TODOS')
            : this.reporteService.obtenerDatosDinamicos(config.metrica, config.dimension, this.idSucursal.toString(), 'TODOS')) as Observable<any>;

        dataObservable.subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    const actualData = isMultiseries ? res.data : res.data;
                    const nuevoW = {
                        config,
                        chartOptions: this.buildChartOptions(config, actualData)
                    };

                    if (this.widgetEnEdicionIndex() !== null) {
                        // Actualizar existente
                        this.widgets.update(ws => {
                            const newWs = [...ws];
                            newWs[this.widgetEnEdicionIndex()!] = nuevoW;
                            return newWs;
                        });
                        this.widgetEnEdicionIndex.set(null);
                    } else {
                        // Añadir nuevo
                        nuevoW.config.orden = this.widgets().length;
                        this.widgets.update(ws => [...ws, nuevoW]);
                    }

                    // Resetear form
                    this.nuevoWidget = {
                        titulo: 'Nuevo Gráfico',
                        tipoGrafico: 'BAR',
                        metrica: 'VENTAS',
                        dimension: 'TIEMPO_MES',
                        orden: this.widgets().length,
                        columns: 6,
                        height: 350
                    };
                }
            }
        });
    }

    seleccionarParaEditar(index: number) {
        const w = this.widgets()[index];
        this.nuevoWidget = { ...w.config };
        this.widgetEnEdicionIndex.set(index);
    }

    cancelarEdicion() {
        this.widgetEnEdicionIndex.set(null);
        this.nuevoWidget = {
            titulo: 'Nuevo Gráfico',
            tipoGrafico: 'BAR',
            metrica: 'VENTAS',
            dimension: 'TIEMPO_MES',
            orden: this.widgets().length,
            columns: 6,
            height: 350
        };
    }

    moverWidget(index: number, direccion: 'arriba' | 'abajo') {
        const ws = [...this.widgets()];
        const targetIndex = direccion === 'arriba' ? index - 1 : index + 1;

        if (targetIndex >= 0 && targetIndex < ws.length) {
            // Swap
            [ws[index], ws[targetIndex]] = [ws[targetIndex], ws[index]];

            // Actualizar órdenes
            ws.forEach((w, i) => w.config.orden = i);

            this.widgets.set(ws);
        }
    }

    eliminarWidget(index: number) {
        this.widgets.update(ws => {
            const filtered = ws.filter((_, i) => i !== index);
            // Re-ordenar tras eliminar
            filtered.forEach((w, i) => w.config.orden = i);
            return filtered;
        });
    }

    // --- Drag and Drop Nativo ---
    onDragStart(index: number) {
        if (!this.isEditing()) return;
        this.draggedIndex = index;
    }

    onDragEnd() {
        this.draggedIndex = null;
    }

    onDragOver(event: DragEvent) {
        if (!this.isEditing()) return;
        event.preventDefault();
    }

    onDrop(index: number) {
        if (!this.isEditing() || this.draggedIndex === null || this.draggedIndex === index) return;

        const ws = [...this.widgets()];
        const draggedWidget = ws.splice(this.draggedIndex, 1)[0];
        ws.splice(index, 0, draggedWidget);

        // Actualizar órdenes
        ws.forEach((w, i) => w.config.orden = i);
        this.widgets.set(ws);
        this.draggedIndex = null;
    }

    // --- Resizing Interactivo ---
    startResizing(event: MouseEvent, index: number) {
        if (!this.isEditing()) return;
        event.preventDefault();
        event.stopPropagation();

        this.resizingIndex = index;
        this.startX = event.clientX;
        this.startY = event.clientY;
        this.startColumns = this.widgets()[index].config.columns;
        this.startHeight = this.widgets()[index].config.height || 350;

        document.addEventListener('mousemove', this.onResizing);
        document.addEventListener('mouseup', this.stopResizing);
    }

    onResizing = (event: MouseEvent) => {
        if (this.resizingIndex === null) return;

        const dashboardElement = document.querySelector('.grid-cols-12');
        if (!dashboardElement) return;

        // Redimensionamiento Horizontal (Columnas)
        const colWidth = dashboardElement.clientWidth / 12;
        const diffX = event.clientX - this.startX;
        const colDiff = Math.round(diffX / colWidth);
        let newCols = this.startColumns + colDiff;
        newCols = Math.max(1, Math.min(12, newCols));

        // Redimensionamiento Vertical (Altura)
        const diffY = event.clientY - this.startY;
        let newHeight = this.startHeight + diffY;
        newHeight = Math.max(200, Math.min(800, newHeight)); // Límites de altura razonables

        const currentW = this.widgets()[this.resizingIndex];
        if (currentW.config.columns !== newCols || currentW.config.height !== newHeight) {
            this.widgets.update(ws => {
                const newWs = [...ws];
                newWs[this.resizingIndex!].config.columns = newCols;
                newWs[this.resizingIndex!].config.height = newHeight;

                // Actualizar opciones del gráfico para reflejar la nueva altura
                newWs[this.resizingIndex!].chartOptions = {
                    ...newWs[this.resizingIndex!].chartOptions,
                    chart: {
                        ...newWs[this.resizingIndex!].chartOptions.chart,
                        height: newHeight
                    }
                };
                return newWs;
            });
        }
    }

    stopResizing = () => {
        this.resizingIndex = null;
        document.removeEventListener('mousemove', this.onResizing);
        document.removeEventListener('mouseup', this.stopResizing);
    }

    guardarDashboard() {
        const user = this.authService.currentUserValue;
        if (!user) return;

        const payload = {
            nombre: this.dashboardName(),
            widgets: this.widgets().map(w => w.config)
        };

        this.reporteService.guardarConfiguracion(payload, user.id, Number(this.idSucursal), this.categoria()).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success('Dashboard guardado correctamente');
                    this.isEditing.set(false);
                }
            }
        });
    }
}



