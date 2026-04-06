import { Routes } from '@angular/router';
import {FEATURE_ROUTES} from '@features/feature.routes';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    children: FEATURE_ROUTES
  }
];
