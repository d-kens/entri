import { Routes } from '@angular/router';
import { Layout } from '@layout/dashboard/dashboard';
import { Public } from '@layout/public/public';
import { authGuard } from '@features/auth/guards/auth-guard';
import { roleGuard } from '@features/auth/guards/role-guard';

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
        path: 'tickets/:reservationId',
        loadComponent: () =>
          import('@features/tickets/reservation-tickets/reservation-tickets').then(
            (m) => m.ReservationTickets,
          ),
      },
      {
        path: 'ticket/:externalId',
        loadComponent: () =>
          import('@features/tickets/ticket-view/ticket-view').then((m) => m.TicketView),
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
        canActivate: [roleGuard],
        data: { roles: ['ORGANIZER'] },
        loadComponent: () => import('@features/summary/summary').then((m) => m.Summary),
      },
      {
        path: 'events',
        loadChildren: () => import('./features/events/events.routes').then((m) => m.EVENTS_ROUTES),
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('@features/users/users-list/users-list').then((m) => m.UsersList),
      },
      {
        path: 'categories',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('@features/categories/categories-list/categories-list').then(
            (m) => m.CategoriesList,
          ),
      },
      {
        path: 'payments',
        loadComponent: () => import('@features/payments/payments').then((m) => m.Payments),
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () => import('@features/reports/reports').then((m) => m.Reports),
      },
      {
        path: 'tickets',
        canActivate: [roleGuard],
        data: { roles: ['ORGANIZER'] },
        loadComponent: () => import('@features/tickets/tickets').then((m) => m.Tickets),
      },
      {
        path: 'profile',
        loadComponent: () => import('@features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'change-password',
        loadComponent: () =>
          import('@features/profile/change-password/change-password').then((m) => m.ChangePassword),
      },
    ],
  },
];
