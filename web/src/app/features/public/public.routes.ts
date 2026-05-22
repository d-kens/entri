import { Routes } from '@angular/router';
import { PublicLayout } from './layout/public-layout';

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
        loadComponent: () => import('./events/events-listing/events-listing').then(m => m.EventsListing),
      },
    ],
  },
];
