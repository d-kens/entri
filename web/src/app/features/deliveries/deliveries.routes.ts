import { Routes } from '@angular/router';
import {NewDelivery} from '@features/deliveries/new-delivery/new-delivery';
import {DeliveryPayment} from '@features/deliveries/delivery-payment/delivery-payment';
import {DeliveriesList} from '@features/deliveries/deliveries-list/deliveries-list';

export const DELIVERIES_ROUTES: Routes = [
  {
    path: '',
    component: DeliveriesList,
  },
  {
    path: 'new',
    component: NewDelivery,
  },
  {
    path: ':id/payment',
    component: DeliveryPayment,
  },
];
