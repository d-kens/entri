import {Routes} from '@angular/router';
import {Layout} from '@features/layout/layout';
import {Welcome} from '@features/layout/components/welcome/welcome';

// TODO: Fix logout issue
// TODO: Fix User Profile Card Issues

export const FEATURE_ROUTES: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        component: Welcome
      },
      {
        path: 'deliveries',
        loadChildren: () => import('./deliveries/deliveries.routes').then(m => m.DELIVERIES_ROUTES)
      }
    ]
  }
]
