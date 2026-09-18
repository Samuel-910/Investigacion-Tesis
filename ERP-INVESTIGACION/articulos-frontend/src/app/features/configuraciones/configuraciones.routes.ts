import { Routes } from '@angular/router';

import { CambioClaveComponent } from './seguridad/cambio-clave/cambio-clave.component';
import { SEGURIDAD_ROUTES } from './seguridad/seguridad.routes';

import { PuntoListComponent } from './puntos/punto-list/punto-list.component';
import { GestionAprobacionesComponent } from '../procesos/solicitud-anulacion/gestion-aprobaciones.component';
import { SucursalListComponent } from './sucursales/sucursal-list/sucursal-list.component';
import { PuntoAtributosPageComponent } from './puntos/atributos-puntos/punto-atributos-page.component';
import { ClinicaListComponent } from './datos-clinica/clinica-list/clinica-list.component';
import { CatalogoListComponent } from './catalogo/catalogo-list/catalogo-list.component';
import { AtributosPageComponent } from './catalogo/atributo-catalogo/atributos-page.component';

export const CONFIGURACIONES_ROUTES: Routes = [
    { path: 'puntos-venta', component: PuntoListComponent },
    { path: 'puntos-atributos', component: PuntoAtributosPageComponent },
    { path: 'empresas', component: ClinicaListComponent },
    { path: 'sucursales', component: SucursalListComponent },
    { path: 'catalogo-productos', component: CatalogoListComponent },
    { path: 'atributos-producto', component: AtributosPageComponent },
    {
        path: 'seguridad',
        loadChildren: () => SEGURIDAD_ROUTES
    }
];