import { Routes } from '@angular/router';
import { FEATURE_ROUTES } from '@features/feature.routes';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    children: FEATURE_ROUTES
  },
  {
    path: '',
    loadChildren: () => import('./features/public/public.routes').then(m => m.PUBLIC_ROUTES)
  }
];
