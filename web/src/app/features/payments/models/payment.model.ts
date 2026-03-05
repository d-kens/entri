export enum PaymentType {
  WITHDRAWAL = '  WITHDRAWAL',
  DELIVERY_FEE = 'DELIVERY_FEE',
  PAYMENT_ON_DELIVERY_COLLECTION = 'PAYMENT_ON_DELIVERY_COLLECTION'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

export type InitiatePaymentRequest = {
  amount: number,
  phoneNumber?: string,
  paymentDescription: string,
  paymentType: PaymentType,
  referenceId: string,
  paymentMethod: string
}

export type InitiatePaymentResponse = {
  paymentId: string,
  referenceId: string
}

export interface PaymentEvent {
  paymentId: string;
  paymentType: string;
  referenceId: string;
  status: PaymentStatus;
  amount: number | null;
  providerReference: string | null;
  failureReason: string | null;
  transactionDate: string | null;
  timestamp: string;
}





