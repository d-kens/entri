import { Routes } from '@angular/router';
import { Layout } from '@layout/app-shell/app-shell';
import { PublicLayout } from '@layout/public-shell/public-shell';
import { authGuard } from '@core/guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        loadComponent: () => import('@features/events/pages/browse-events/browse-events').then(m => m.BrowseEvents),
      },
      {
        path: 'discover',
        redirectTo: '',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    children: [
      {
        path: 'overview',
        loadComponent: () => import('./features/overview/overview').then(m => m.Overview),
      },
      {
        path: 'support',
        loadComponent: () => import('./features/support/support').then(m => m.Support),
      },
      {
        path: 'events',
        loadChildren: () => import('./features/events/events.routes').then(m => m.EVENTS_ROUTES),
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./features/public/not-found/not-found').then(m => m.NotFound),
  },
];
