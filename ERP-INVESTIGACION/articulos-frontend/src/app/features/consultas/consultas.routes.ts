import { Routes } from '@angular/router';
import { KardexListComponent } from './kardex/kardex-list/kardex-list.component';

export const CONSULTAS_ROUTES: Routes = [
    {
        path: 'kardex',
        component: KardexListComponent
    }
];
