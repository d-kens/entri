import { Routes } from '@angular/router';
import { hasCodeGuard } from './guards/has-code.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'enter-code',
    pathMatch: 'full',
  },
  {
    path: 'enter-code',
    loadComponent: () =>
      import('./pages/enter-code/enter-code.page').then((m) => m.EnterCodePage),
  },
  {
    path: 'scanner',
    loadComponent: () =>
      import('./pages/scanner/scanner.page').then((m) => m.ScannerPage),
    canActivate: [hasCodeGuard],
  },
];
