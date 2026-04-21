import {Routes} from '@angular/router';
import {Layout} from '@features/layout/layout';
import {Welcome} from '@features/layout/components/welcome/welcome';
import {authGuard} from '@core/guards/auth-guard';

export const FEATURE_ROUTES: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        canActivate: [authGuard],
        component: Welcome
      },
      {
        path: 'support',
        canActivate: [authGuard],
        loadComponent: () => import('./support/support').then(m => m.Support)
      },
      {
        path: 'forbidden',
        canActivate: [authGuard],
        loadComponent: () => import('./forbidden/forbidden').then(m => m.Forbidden)
      }
    ]
  }
]
