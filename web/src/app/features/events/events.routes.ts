import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth-guard';

export const EVENTS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/events-list/events-list').then(m => m.EventsList),
  },
  {
    path: 'create',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/create-event/create-event').then(m => m.CreateEvent),
  },
];
