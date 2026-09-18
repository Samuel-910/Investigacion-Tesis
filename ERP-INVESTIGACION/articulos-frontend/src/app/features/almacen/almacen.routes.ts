import { Routes } from '@angular/router';
import { AlmacenListComponent } from './almacenes/almacen-list/almacen-list.component';
import { ProductoServicioListComponent } from './producto/producto-list/Producto-list.component';

export const ALMACEN_ROUTES: Routes = [
    { path: 'productos', component: ProductoServicioListComponent },
    { path: 'almacenes', component: AlmacenListComponent },

];