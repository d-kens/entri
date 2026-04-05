import {PaymentStatus} from '@features/payments/models/payment.model';

export type CreateDeliveryDto = {
  toAgent: number;
  fromAgent: number;
  deliveryFee: number;
  packageName: string;
  packagePrice: number;
  recipientName: string;
  recipientPhone: string;
  packageDescription: string;
}

export enum DeliveryStatus {
  PENDING = 'PENDING',
  AT_PICKUP_AGENT = 'AT_PICKUP_AGENT',
  AT_HUB = 'AT_HUB',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  AT_DESTINATION_AGENT = 'AT_DESTINATION_AGENT',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

export type DeliveryResponse = {
  toAgent: string;
  fromAgent: string;
  toZone: string;
  fromZone: string;
  externalId: string;
  packageName: string;
  customerName: string;
  recipientName: string;
  recipientPhone: string;
  trackingNumber: string;
  deliveryFee: number;
  packagePrice: number;
  packageDescription?: string;
  paymentStatus: PaymentStatus;
  deliveryStatus: DeliveryStatus;
};

export type UpdateDeliveryStatusDto = {
  status: DeliveryStatus;
  cancellationReason?: string;
}

export interface DeliveryFilters {
  page?: number;
  size?: number;
  status?: string;
  search?: string;
}

export type TrackDeliveryResponse = {
  externalId: string,
  deliveryStatus: DeliveryStatus,
  trackingNumber: string;
  packageName: string;
  packagePrice: number;
  recipientName: string;
  recipientPhone: string;
  from: LocationInfo;
  to: LocationInfo;
  timeline: TrackingTimeline[];
}

export interface LocationInfo {
  zone: string;
  point: string;
}

export interface TrackingTimeline {
  status: string;
  timestamp: string;
  location: string;
  completed: boolean;
}
