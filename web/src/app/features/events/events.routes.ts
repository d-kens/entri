import { Routes } from '@angular/router';

export const EVENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/events-list/events-list').then((m) => m.EventsList),
  },
  {
    path: 'create',
    loadComponent: () => import('./pages/create-event/create-event').then((m) => m.CreateEvent),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/event-details/event-details').then((m) => m.EventDetails),
  },
];
