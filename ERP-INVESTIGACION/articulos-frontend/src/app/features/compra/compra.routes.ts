import { Routes } from '@angular/router';
import { ProveedorListComponent } from './proveedores/proveedor-list/proveedor-list.component';

export const COMPRAS_ROUTES: Routes = [
    { path: 'proveedores', component: ProveedorListComponent },
    { path: 'proveedores/:id/cuenta', loadComponent: () => import('./proveedores/cuenta-proveedor/cuenta-proveedor-list/cuenta-proveedor-list.component').then(m => m.CuentaProveedorListComponent) },
    { path: 'pagos-pendientes', loadComponent: () => import('./proveedores/pagos-pendientes/pagos-pendientes-list.component').then(m => m.PagosPendientesListComponent) },
    { path: 'orden', loadComponent: () => import('./orden/orden-list/orden-list.component').then(m => m.OrdenListComponent) },
    { path: 'registrar', loadComponent: () => import('./registro/registro-list/registro-list.component').then(m => m.RegistroListComponent) },
    { path: 'intercambio', loadComponent: () => import('./intercambio/intercambio.component').then(m => m.IntercambioComponent) },
];
