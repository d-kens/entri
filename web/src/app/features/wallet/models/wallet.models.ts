export type WalletResponse = {
  externalId: string;
  balance: number;
};

export type WalletTransaction = {
  externalId: string;
  type: 'CREDIT' | 'DEBIT';
  amount: number;
  currency: string;
  referenceId: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  dateCreated: string;
};

export type WithdrawalRequest = {
  amount: number;
  accountType: 'PAYBILL' | 'TILL_NUMBER' | 'BANK';
  account: string;
  name: string;
  narrative: string;
  accountReference?: string;
  bankCode?: string;
};

export type WithdrawalResponse = {
  externalId: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
};
