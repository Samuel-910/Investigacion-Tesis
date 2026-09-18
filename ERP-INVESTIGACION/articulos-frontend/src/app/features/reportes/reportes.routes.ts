import { Routes } from '@angular/router';
import { ReporteDashboardComponent } from './reporte-dashboard/reporte-dashboard.component';
import { ReporteVentasComponent } from './reporte-ventas/reporte-ventas.component';
import { ReporteComprasComponent } from './reporte-compras/reporte-compras.component';
import { ReporteCuentasPagarComponent } from './reporte-cuentas-pagar/reporte-cuentas-pagar.component';
import { StockValorizadoComponent } from '../almacen/reportes/stock-valorizado/stock-valorizado.component';
import { StockValorizadoDetalleComponent } from '../almacen/reportes/stock-valorizado/detalle/stock-valorizado-detalle.component';
import { CorrelatividadListComponent } from '../ventas/reportes/correlatividad-list/correlatividad-list.component';
import { ReporteCajasComponent } from './reporte-cajas/reporte-cajas.component';
import { ReporteDescuentoComponent } from './reporte-descuento/reporte-descuento.component';
import { ReporteComprobantesAnuladosComponent } from './reporte-comprobantes-anulados/reporte-comprobantes-anulados.component';
import { ReporteNotaCreditoComponent } from './reporte-nota-credito/reporte-nota-credito.component';
import { KardexListComponent } from '../consultas/kardex/kardex-list/kardex-list.component';

export const REPORTES_ROUTES: Routes = [
    { path: '', component: ReporteDashboardComponent },
    { path: 'ventas', component: ReporteVentasComponent },
    { path: 'compras', component: ReporteComprasComponent },
    { path: 'cuentas-pagar', component: ReporteCuentasPagarComponent },
    { path: 'stock-valorizado', component: StockValorizadoComponent },
    { path: 'stock-valorizado/:id', component: StockValorizadoDetalleComponent },
    { path: 'correlatividad', component: CorrelatividadListComponent },
    { path: 'cajas', component: ReporteCajasComponent },
    { path: 'kardex', component: KardexListComponent },
    { path: 'descuento', component: ReporteDescuentoComponent },
    { path: 'comprobantes-anulados', component: ReporteComprobantesAnuladosComponent },
    { path: 'nota-credito', component: ReporteNotaCreditoComponent },
    {
        path: 'producto',
        loadComponent: () => import('./reporte-productos/reporte-productos.component').then(m => m.ReporteProductosComponent)
    },
    {
        path: 'interactivo',
        loadComponent: () => import('./reporte-interactivo/reporte-interactivo.component').then(m => m.ReporteInteractivoComponent)
    },
    {
        path: ':id',
        loadComponent: () => import('./dashboard-personalizado/dashboard-personalizado.component').then(m => m.DashboardPersonalizadoComponent)
    },
    {
        path: 'personalizado/:categoria',
        loadComponent: () => import('./dashboard-personalizado/dashboard-personalizado.component').then(m => m.DashboardPersonalizadoComponent)
    }
];
