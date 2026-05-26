import { Routes } from '@angular/router';
import { Layout } from '@features/layout/layout';
import { authGuard } from '@core/guards/auth-guard';

export const FEATURE_ROUTES: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        redirectTo: 'overview',
        pathMatch: 'full'
      },
      {
        path: 'overview',
        canActivate: [authGuard],
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'support',
        canActivate: [authGuard],
        loadComponent: () => import('./support/support').then(m => m.Support)
      },
      {
        path: 'forbidden',
        canActivate: [authGuard],
        loadComponent: () => import('./forbidden/forbidden').then(m => m.Forbidden)
      },
      {
        path: 'events',
        loadChildren: () => import('./events/events.routes').then(m => m.EVENTS_ROUTES)
      }
    ]
  }
]
