export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export type InitiatePaymentRequest = {
  amount: number,
  phoneNumber?: string,
  paymentMethod: string,
  paymentReference: string,
  paymentDescription: string,
}
