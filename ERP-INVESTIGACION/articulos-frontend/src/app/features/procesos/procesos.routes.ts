import { Routes } from '@angular/router';
import { MovimientoMixtoComponent } from './movimientos/movimiento-mixto/movimiento-mixto.component';
import { ClasificacionListComponent } from './clasificaciones/clasificacion-list/clasificacion-list.component';
import { MovimientoHistorialComponent } from './movimientos/movimiento-historial/movimiento-historial.component';
import { TransferenciaListComponent } from './transferencia/transferencia-list/transferencia-list.component';
import { GestionAprobacionesComponent } from './solicitud-anulacion/gestion-aprobaciones.component';

export const PROCESOS_ROUTES: Routes = [
    { path: 'atributos-diverso', component: ClasificacionListComponent },
    { path: 'nuevo-movimiento', component: MovimientoMixtoComponent },
    { path: 'historial-movimientos', component: MovimientoHistorialComponent },

    { path: 'transferencias', component: TransferenciaListComponent },
    { path: 'aprobaciones', component: GestionAprobacionesComponent },

    // Rutas referenciadas en el menú lateral de procesos
    { path: 'reimprimir-comprobantes', loadComponent: () => import('../ventas/reimpresion/reimpresion.component').then(m => m.ReimpresionComponent) },
    { path: 'detalle-documentos', loadComponent: () => import('../ventas/reportes/detalle-documentos/detalle-documentos.component').then(m => m.DetalleDocumentosComponent) },
    { path: 'documento-paciente', loadComponent: () => import('../ventas/paciente-documentos/paciente-documentos.component').then(m => m.PacienteDocumentosComponent) },
];