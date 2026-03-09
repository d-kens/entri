import {Routes} from '@angular/router';
import {Layout} from '@features/layout/layout';
import {Welcome} from '@features/layout/components/welcome/welcome';
import {authGuard} from '@core/guards/auth-guard';
import {TrackDelivery} from '@features/deliveries/track-delivery/track-delivery';

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
