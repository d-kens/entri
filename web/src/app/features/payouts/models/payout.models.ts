export type PayoutMethod = 'MPESA_PAYBILL' | 'MPESA_TILL' | 'BANK';

export type PayoutAccountResponse = {
  externalId: string;
  method: PayoutMethod;
  recipientName: string;
  account: string;
  accountReference: string | null;
  bankCode: string | null;
  isDefault: boolean;
};

export type PayoutAccountRequest = {
  method: PayoutMethod;
  recipientName: string;
  account: string;
  accountReference?: string;
  bankCode?: string;
  isDefault: boolean;
};
