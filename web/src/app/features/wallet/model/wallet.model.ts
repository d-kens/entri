export interface WalletResponse {
  externalId: string;
  balance: number;
  status: WalletStatus;
  createdAt: string;
  updatedAt: string;
}

export interface WalletTransactionResponse {
  externalId: string;
  type: TransactionType;
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  status: TransactionStatus;
  referenceType: string;
  referenceId: string;
  description: string;
  createdAt: string;
  completedAt: string | null;
}

export enum WalletStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

export enum TransactionType {
  CREDIT = 'CREDIT',
  DEBIT = 'DEBIT'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REVERSED = 'REVERSED'
}
