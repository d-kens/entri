import { Routes } from '@angular/router';
import {NewDelivery} from '@features/deliveries/new-delivery/new-delivery';

export const DELIVERIES_ROUTES: Routes = [
  {
    path: 'new',
    component: NewDelivery,
  },
];
