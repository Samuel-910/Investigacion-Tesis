import { Routes } from '@angular/router';
import { CotizacionListComponent } from './cotizacion-ventas/cotizacion-list.component';
import { VentaRegistroComponent } from './venta-registro/venta-registro.component';
import { VentaListComponent } from './listado-ventas/venta-list.component';

export const VENTAS_ROUTES: Routes = [
    {
        path: 'descuentos',
        loadChildren: () => import('./descuentos/descuento.routes').then(m => m.DESCUENTO_ROUTES)
    },
    { path: 'mantenimiento-descuentos', redirectTo: 'descuentos/mantenimiento' },
    { path: 'venta', component: VentaRegistroComponent },
    { path: 'listado', component: VentaListComponent },
    { path: 'notas-venta', loadComponent: () => import('./notas-venta/nota-venta-list.component').then(m => m.NotaVentaListComponent) },
    { path: 'notas-credito', loadComponent: () => import('./notas-credito/nota-credito-list.component').then(m => m.NotaCreditoListComponent) },
    { path: 'cotizaciones', component: CotizacionListComponent },
    { path: 'devoluciones', loadComponent: () => import('./devolucion-list/devolucion-list.component').then(m => m.DevolucionListComponent) },
    { path: 'reimpresion', loadComponent: () => import('./reimpresion/reimpresion.component').then(m => m.ReimpresionComponent) },
    { path: 'pacientes-documento', loadComponent: () => import('./paciente-documentos/paciente-documentos.component').then(m => m.PacienteDocumentosComponent) },
];
