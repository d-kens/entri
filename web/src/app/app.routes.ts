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
        title: 'Discover Events',
        loadComponent: () =>
          import('@features/events/browse-events/browse-events').then((m) => m.BrowseEvents),
      },
      {
        path: 'events/:externalId',
        title: 'Event Details',
        loadComponent: () =>
          import('@features/events/event-view/event-view').then((m) => m.EventView),
      },
      {
        path: 'events/:externalId/checkout/:reservationId',
        title: 'Checkout',
        loadComponent: () => import('@features/events/checkout/checkout').then((m) => m.Checkout),
      },
      {
        path: 'tickets/:reservationId',
        title: 'Your Tickets',
        loadComponent: () =>
          import('@features/tickets/reservation-tickets/reservation-tickets').then(
            (m) => m.ReservationTickets,
          ),
      },
      {
        path: 'ticket/:externalId',
        title: 'Ticket',
        loadComponent: () =>
          import('@features/tickets/ticket-view/ticket-view').then((m) => m.TicketView),
      },
      {
        path: 'check-in',
        loadChildren: () =>
          import('@features/check-in/check-in.routes').then((m) => m.CHECK_IN_ROUTES),
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
        title: 'Summary',
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
        title: 'Manage Users',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('@features/users/users-list/users-list').then((m) => m.UsersList),
      },
      {
        path: 'categories',
        title: 'Manage Categories',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('@features/categories/categories-list/categories-list').then(
            (m) => m.CategoriesList,
          ),
      },
      {
        path: 'payments',
        title: 'Payments',
        loadComponent: () => import('@features/payments/payments').then((m) => m.Payments),
      },
      {
        path: 'wallet',
        title: 'Wallet',
        canActivate: [roleGuard],
        data: { roles: ['ORGANIZER'] },
        loadComponent: () => import('@features/wallet/wallet').then((m) => m.Wallet),
      },
      {
        path: 'profile',
        title: 'Profile',
        loadComponent: () => import('@features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'change-password',
        title: 'Change Password',
        loadComponent: () =>
          import('@features/profile/change-password/change-password').then((m) => m.ChangePassword),
      },
    ],
  },
  {
    path: '**',
    title: 'Page Not Found',
    loadComponent: () => import('@features/not-found/not-found').then((m) => m.NotFound),
  },
];
