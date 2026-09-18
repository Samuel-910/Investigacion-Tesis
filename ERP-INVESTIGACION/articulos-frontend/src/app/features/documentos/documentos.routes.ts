import { Routes } from '@angular/router';

export const DOCUMENTOS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./plantillas/documento-lista/documento-lista.component').then(m => m.DocumentoListaComponent)
    },
    {
        path: 'bloques',
        loadComponent: () => import('./bloque/bloque-lista/bloque-lista.component').then(m => m.BloqueListaComponent)
    },
    {
        path: 'editor',
        loadComponent: () => import('./plantillas/documento-grapejs/documento-grapejs.component').then(m => m.DocumentoGrapejsComponent)
    },
    {
        path: 'editor/:id',
        loadComponent: () => import('./plantillas/documento-grapejs/documento-grapejs.component').then(m => m.DocumentoGrapejsComponent)
    },

    {
        path: 'formatos',
        loadComponent: () => import('./formato/formato-list/formato-list.component').then(m => m.FormatoListComponent)
    },
    {
        path: 'puntos',
        loadComponent: () => import('./punto-documento/punto-documento-list.component').then(m => m.PuntoDocumentoListComponent)
    }
];
