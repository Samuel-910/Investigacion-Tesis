import { Routes } from '@angular/router';
import { DescuentoListComponent } from './descuento/descuento-list/descuento-list.component';
import { ConvenioListComponent } from './convenio/convenio-list/convenio-list.component';
import { ConvenioDetalleComponent } from './convenio/convenio-detalle/convenio-detalle.component';

export const DESCUENTO_ROUTES: Routes = [
    {
        path: 'lista',
        component: DescuentoListComponent
    },
    {
        path: 'convenio',
        component: ConvenioListComponent
    },
    {
        path: 'convenio/:id',
        component: ConvenioDetalleComponent
    }
];
