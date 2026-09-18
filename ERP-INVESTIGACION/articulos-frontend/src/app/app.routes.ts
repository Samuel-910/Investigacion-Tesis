import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { CONFIGURACIONES_ROUTES } from './features/configuraciones/configuraciones.routes';
import { ALMACEN_ROUTES } from './features/almacen/almacen.routes';
import { DashboardPersonalizadoComponent } from './features/reportes/dashboard-personalizado/dashboard-personalizado.component';
import { PerfilPageComponent } from './features/perfil/pages/perfil-page/perfil-page.component';

export const routes: Routes = [
    { path: '', redirectTo: 'dashboard/Principal', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'dashboard/Principal', component: DashboardPersonalizadoComponent, canActivate: [AuthGuard] },
    { path: 'perfil', component: PerfilPageComponent, canActivate: [AuthGuard] },
    {
        path: 'configuracion',
        loadChildren: () => CONFIGURACIONES_ROUTES,
        canActivate: [AuthGuard],
    },
    {
        path: 'almacen',
        loadChildren: () => ALMACEN_ROUTES,
        canActivate: [AuthGuard],
    },
    {
        path: 'venta',
        loadChildren: () => import('./features/ventas/ventas.routes').then(m => m.VENTAS_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'compra',
        loadChildren: () => import('./features/compra/compra.routes').then(m => m.COMPRAS_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'caja',
        loadChildren: () => import('./features/caja/caja.routes').then(m => m.CAJA_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'consultas',
        loadChildren: () => import('./features/consultas/consultas.routes').then(m => m.CONSULTAS_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'documentos',
        loadChildren: () => import('./features/documentos/documentos.routes').then(m => m.DOCUMENTOS_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'reportes',
        loadChildren: () => import('./features/reportes/reportes.routes').then(m => m.REPORTES_ROUTES),
        canActivate: [AuthGuard],
    },
    {
        path: 'procesos',
        loadChildren: () => import('./features/procesos/procesos.routes').then(m => m.PROCESOS_ROUTES),
        canActivate: [AuthGuard],
    },
];
