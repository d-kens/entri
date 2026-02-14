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
  paymentStatus: string;
  paymentMethod?: string;
  deliveryStatus: string;
};





