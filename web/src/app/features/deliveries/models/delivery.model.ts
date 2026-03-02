import {PaymentStatus} from '@features/payments/models/payment.model';

export type CreateDeliveryDto = {
  toAgent: number;
  fromAgent: number;
  cashAmount?: number;
  deliveryFee: number;
  packageName: string;
  packagePrice: number;
  collectCash: boolean;
  recipientName: string;
  recipientPhone: string;
  packageDescription: string;
}

export enum DeliveryStatus {
  PENDING = 'PENDING',
  DROPPED_AT_PICKUP_AGENT = 'DROPPED_AT_PICKUP_AGENT',
  AT_HUB = 'AT_HUB',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export type DeliveryResponse = {
  toAgent: string;
  fromAgent: string;
  toZone: string;
  fromZone: string;
  externalId: string;
  packageName: string;
  collectCash: boolean;
  customerName: string;
  recipientName: string;
  recipientPhone: string;
  trackingNumber: string;
  cashAmount?: number;
  cashCollected: boolean;
  deliveryFee: number;
  packagePrice: number;
  paymentReference?: string;
  packageDescription?: string;
  paymentStatus: PaymentStatus;
  paymentMethod?: string;
  deliveryStatus: DeliveryStatus;
};

export interface DeliveryFilters {
  page?: number;
  size?: number;
  status?: string;
  search?: string;
}
