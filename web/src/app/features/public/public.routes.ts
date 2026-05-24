import { Routes } from '@angular/router';
import { PublicLayout } from './layout/public-layout';
import { noAuthGuard } from '@core/guards/no-auth-guard';

export const PUBLIC_ROUTES: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        loadComponent: () => import('./landing/landing').then(m => m.Landing),
      },
      {
        path: 'events',
        loadComponent: () => import('@features/events/pages/events-listing/events-listing').then(m => m.EventsListing),
      },
    ],
  },
];
