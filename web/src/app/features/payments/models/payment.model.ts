export enum PaymentType {
  WITHDRAWAL = '  WITHDRAWAL',
  DELIVERY_FEE = 'DELIVERY_FEE',
  PAYMENT_ON_DELIVERY_COLLECTION = 'PAYMENT_ON_DELIVERY_COLLECTION'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
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





