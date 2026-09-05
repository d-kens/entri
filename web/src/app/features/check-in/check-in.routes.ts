import { Routes } from '@angular/router';
import { hasCodeGuard } from './guards/has-code-guard';

export const CHECK_IN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./enter-code/enter-code').then((m) => m.EnterCode),
  },
  {
    path: 'scanner',
    loadComponent: () => import('./scanner/scanner').then((m) => m.Scanner),
    canActivate: [hasCodeGuard],
  },
];
