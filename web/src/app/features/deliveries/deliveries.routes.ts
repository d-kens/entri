import { Routes } from '@angular/router';
import {NewDelivery} from '@features/deliveries/new-delivery/new-delivery';
import {DeliveryPayment} from '@features/deliveries/delivery-payment/delivery-payment';

export const DELIVERIES_ROUTES: Routes = [
  {
    path: 'new',
    component: NewDelivery,
  },
  {
    path: ':id/payment',
    component: DeliveryPayment,
  },
];
