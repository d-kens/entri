export type WalletResponse = {
  walletId: string;
  label: string;
  currency: string;
  currentBalance: number;
  availableBalance: number;
};

export type WithdrawType = 'MPESA_PAYBILL' | 'BANK';

export type WithdrawRequest = {
  type: WithdrawType;
  amount: number;
  account: string;
  accountNumber?: string;
  bankCode?: string;
  recipientName: string;
};

export type WithdrawResponse = {
  trackingId: string;
  status: string;
};

export type BankCodeResponse = {
  bankId: string;
  bankName: string;
};

export type WalletTransactionResponse = {
  id: string;
  type: string;
  amount: number;
  currency: string;
  narrative: string;
  status: string;
  createdAt: string;
};

export type WalletTransactionsPage = {
  content: WalletTransactionResponse[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};
