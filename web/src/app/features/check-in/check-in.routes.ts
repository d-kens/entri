import { Routes } from '@angular/router';
import { hasCodeGuard } from './guards/has-code-guard';

export const CHECK_IN_ROUTES: Routes = [
  {
    path: '',
    title: 'Check-In',
    loadComponent: () => import('./enter-code/enter-code').then((m) => m.EnterCode),
  },
  {
    path: 'scanner',
    title: 'Scan Ticket',
    loadComponent: () => import('./scanner/scanner').then((m) => m.Scanner),
    canActivate: [hasCodeGuard],
  },
];
