export enum PayableType {
  DELIVERY = 'DELIVERY'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export type InitiatePaymentRequest = {
  amount: number,
  phoneNumber?: string,
  paymentDescription: string,
  payableType: PayableType,
  payableId: string,
  paymentMethod: string
}

export type InitiatePaymentResponse = {
  paymentId: string,
  payableId: string
}





