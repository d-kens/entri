import { Routes } from '@angular/router';
import { Layout } from '@layout/dashboard/dashboard';
import { Public } from '@layout/public/public';
import { authGuard } from '@features/auth/guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    component: Public,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('@features/events/browse-events/browse-events').then((m) => m.BrowseEvents),
      },
      {
        path: 'events/:externalId',
        loadComponent: () =>
          import('@features/events/event-view/event-view').then((m) => m.EventView),
      },
      {
        path: 'events/:externalId/checkout/:reservationId',
        loadComponent: () => import('@features/events/checkout/checkout').then((m) => m.Checkout),
      },
      {
        path: 'about',
        loadComponent: () => import('@features/about/about').then((m) => m.About),
      },
      {
        path: 'support',
        loadComponent: () => import('@features/support/support').then((m) => m.Support),
      },
    ],
  },
  {
    path: 'auth',
    loadChildren: () => import('@features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'dashboard',
    component: Layout,
    canActivate: [authGuard],
    children: [
      {
        path: 'summary',
        loadComponent: () => import('@features/summary/summary').then((m) => m.Summary),
      },
      {
        path: 'events',
        loadChildren: () => import('./features/events/events.routes').then((m) => m.EVENTS_ROUTES),
      },
    ],
  },
];
