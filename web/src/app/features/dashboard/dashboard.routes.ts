import {Routes} from '@angular/router';
import {DashboardLayout} from './page/dashboard-layout/dashboard-layout';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: DashboardLayout,
    children: []
  }
]
