import { Routes } from '@angular/router';
import {authGuard} from './core/guards/auth-guard';
import {preventAuthenticatedGuard} from './core/guards/prevent-authenticated-guard';
import {FEATURE_ROUTES} from '@features/feature.routes';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [preventAuthenticatedGuard],
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    canActivate: [authGuard],
    children: FEATURE_ROUTES
  }
];
