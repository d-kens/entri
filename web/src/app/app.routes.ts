import { Routes } from '@angular/router';
import {preventAuthenticatedGuard} from './core/guards/prevent-authenticated-guard';
import {FEATURE_ROUTES} from '@features/feature.routes';
import {Launch} from '@features/launch/launch';
import {authGuard} from '@core/guards/auth-guard';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [preventAuthenticatedGuard],
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  // {
  //   path: '',
  //   children: FEATURE_ROUTES
  // },
  {
    path: '',
    component: Launch,
    canActivate: [authGuard],
  }
];
