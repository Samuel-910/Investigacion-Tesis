import { Routes } from '@angular/router';
import { ArqueoListComponent } from './arqueo-list/arqueo-list.component';
import { CajaChicaListComponent } from './caja-chica/caja-chica-list/caja-chica-list.component';

export const CAJA_ROUTES: Routes = [
    {
        path: 'chica',
        component: CajaChicaListComponent
    },
    {
        path: 'arqueos',
        component: ArqueoListComponent
    },
    {
        path: 'medio-pago',
        loadComponent: () => import('./metodo-pago/medio-pago-page.component').then(m => m.MedioPagoPageComponent)
    },
    {
        path: 'general',
        loadComponent: () => import('./caja-general/caja-general-list/caja-general-list.component').then(m => m.CajaGeneralListComponent)
    }
];
