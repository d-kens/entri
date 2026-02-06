import {Routes} from '@angular/router';
import {Layout} from '@features/layout/layout';

export const FEATURE_ROUTES: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: 'deliveries',
        loadChildren: () => import('./deliveries/deliveries.routes').then(m => m.DELIVERIES_ROUTES)
      }
    ]
  }
]
