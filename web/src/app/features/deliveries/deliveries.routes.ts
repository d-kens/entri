import { Routes } from '@angular/router';
import { NewDelivery } from '@features/deliveries/new-delivery/new-delivery';
import { DeliveryPayment } from '@features/deliveries/delivery-payment/delivery-payment';
import { DeliveriesList } from '@features/deliveries/deliveries-list/deliveries-list';
import { TrackDelivery } from '@features/deliveries/track-delivery/track-delivery';
import { DeliveryDetail } from '@features/deliveries/delivery-detail/delivery-detail';
import { permissionGuard } from '@core/guards/permission-guard';
import { Permissions } from '@core/constants/permissions';

export const DELIVERIES_ROUTES: Routes = [
  {
    path: '',
    component: DeliveriesList,
    canActivate: [permissionGuard(Permissions.DELIVERY.READ)],
  },
  {
    path: 'new',
    component: NewDelivery,
    canActivate: [permissionGuard(Permissions.DELIVERY.CREATE)],
  },
  {
    path: 'track',
    component: TrackDelivery
  },
  {
    path: ':externalId/payment',
    component: DeliveryPayment,
    canActivate: [permissionGuard(Permissions.DELIVERY.READ)],
  },
  {
    path: ':externalId',
    component: DeliveryDetail,
    canActivate: [permissionGuard(Permissions.DELIVERY.READ)],
  },
];
