import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import {
  form,
  FormField,
  hidden,
  required,
  submit as submitForm,
  validate,
} from '@angular/forms/signals';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { DataTable } from '@shared/components/data-table/data-table';
import { PageResponse } from '@shared/models/common.model';
import { WalletService } from './wallet-service';
import { WalletResponse, WalletTransaction } from './models/wallet.models';

type AccountType = 'PAYBILL' | 'TILL_NUMBER' | 'BANK';

interface WithdrawData {
  amount: number | null;
  accountType: AccountType;
  name: string;
  account: string;
  narrative: string;
  accountReference: string;
  bankCode: string;
}

const DEFAULT_WITHDRAW: WithdrawData = {
  amount: null,
  accountType: 'PAYBILL',
  name: '',
  account: '',
  narrative: '',
  accountReference: '',
  bankCode: '',
};

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    TitleCasePipe,
    ReactiveFormsModule,
    FormField,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    EntriButton,
    DataTable,
  ],
  templateUrl: './wallet.html',
})
export class Wallet implements OnInit {
  private walletService = inject(WalletService);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  wallet = signal<WalletResponse | null>(null);
  walletLoading = signal(true);

  transactionsPage = signal<PageResponse<WalletTransaction>>({
    content: [],
    number: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });
  transactionsLoading = signal(true);
  transactionsError = signal(false);

  showWithdrawForm = signal(false);
  withdrawLoading = signal(false);

  readonly columns = ['date', 'type', 'amount', 'status'];

  private withdrawData = signal<WithdrawData>({ ...DEFAULT_WITHDRAW });

  readonly accountType = computed(() => this.withdrawData().accountType);

  withdrawForm = form(this.withdrawData, (fields) => {
    validate(fields.amount, (ctx) => {
      const v = ctx.value();
      if (v === null || v === undefined) return { kind: 'required', message: 'Required' };
      if (Number(v) < 100) return { kind: 'min', message: 'Minimum withdrawal is KES 100' };
      return null;
    });
    required(fields.accountType, { message: 'Required' });
    required(fields.name, { message: 'Required' });
    required(fields.account, { message: 'Required' });
    required(fields.narrative, { message: 'Required' });
    hidden(fields.accountReference, () => this.withdrawData().accountType !== 'PAYBILL');
    required(fields.accountReference, { message: 'Required' });
    hidden(fields.bankCode, () => this.withdrawData().accountType !== 'BANK');
    required(fields.bankCode, { message: 'Required' });
  });

  private externalKey = '';

  ngOnInit(): void {
    this.externalKey = this.authService.getExternalId() ?? '';
    this.loadWallet();
    this.loadTransactions(0, 20);
  }

  toggleWithdrawForm(): void {
    this.showWithdrawForm.update((v) => !v);
    if (!this.showWithdrawForm()) {
      this.withdrawData.set({ ...DEFAULT_WITHDRAW });
    }
  }

  onPage(event: PageEvent): void {
    this.loadTransactions(event.pageIndex, event.pageSize);
  }

  async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submitForm(this.withdrawForm, async () => {
      const v = this.withdrawData();
      this.withdrawLoading.set(true);
      this.walletService
        .withdraw(this.wallet()!.externalId, {
          amount: v.amount!,
          accountType: v.accountType,
          name: v.name,
          account: v.account,
          narrative: v.narrative,
          accountReference: v.accountReference || undefined,
          bankCode: v.bankCode || undefined,
        })
        .subscribe({
          next: () => {
            this.snackbarService.showSuccess('Withdrawal initiated successfully');
            this.withdrawLoading.set(false);
            this.showWithdrawForm.set(false);
            this.withdrawData.set({ ...DEFAULT_WITHDRAW });
            this.loadWallet();
            this.loadTransactions(0, 20);
          },
          error: (err) => {
            const msg = err?.error?.detail || 'Withdrawal failed. Please try again.';
            this.snackbarService.showError(msg);
            this.withdrawLoading.set(false);
          },
        });
    });
  }

  private loadWallet(): void {
    this.walletLoading.set(true);
    this.walletService.getWallet(this.externalKey).subscribe({
      next: (w) => {
        this.wallet.set(w);
        this.walletLoading.set(false);
      },
      error: () => this.walletLoading.set(false),
    });
  }

  private loadTransactions(page: number, size: number): void {
    this.transactionsLoading.set(true);
    this.transactionsError.set(false);
    this.walletService.getTransactions(this.externalKey, page, size).subscribe({
      next: (res) => {
        this.transactionsPage.set(res);
        this.transactionsLoading.set(false);
      },
      error: () => {
        this.transactionsError.set(true);
        this.transactionsLoading.set(false);
      },
    });
  }
}
