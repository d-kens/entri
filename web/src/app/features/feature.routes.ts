import {Routes} from '@angular/router';
import {Layout} from '@features/layout/layout';
import {Welcome} from '@features/layout/components/welcome/welcome';
import {authGuard} from '@core/guards/auth-guard';
import {TrackDelivery} from '@features/track-delivery/track-delivery';

// TODO: Fix logout issue
// TODO: Fix User Profile Card Issues

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
        path: 'deliveries',
        canActivate: [authGuard],
        loadChildren: () => import('./deliveries/deliveries.routes').then(m => m.DELIVERIES_ROUTES)
      }
    ]
  },
  {
    path: 'track',
    component: TrackDelivery
  }
]
