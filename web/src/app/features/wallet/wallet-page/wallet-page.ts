import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { WalletService } from '@features/wallet/wallet-service';
import { WalletTransactionsPage } from '@features/wallet/models/wallet.models';
import { WithdrawDialog } from '@features/wallet/withdraw-dialog/withdraw-dialog';
import { SnackbarService } from '@shared/services/snackbar-service';
import { DataTable } from '@shared/components/data-table/data-table';

@Component({
  selector: 'app-wallet-page',
  standalone: true,
  imports: [
    DecimalPipe,
    DatePipe,
    TitleCasePipe,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule,
    DataTable,
  ],
  templateUrl: './wallet-page.html',
  styleUrl: './wallet-page.css',
})
export class WalletPage implements OnInit {
  private walletService = inject(WalletService);
  private dialog = inject(MatDialog);
  private snackbar = inject(SnackbarService);

  wallet = this.walletService.wallet;
  walletLoading = this.walletService.walletLoading;

  txLoading = signal(true);
  txError = signal(false);
  page = signal<WalletTransactionsPage>({
    content: [],
    pageNumber: 1,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  readonly columns = ['type', 'narrative', 'amount', 'status', 'createdAt'];

  ngOnInit(): void {
    this.walletService.getWallet().subscribe();
    this.loadTransactions(1, 10);
  }

  onPage(event: PageEvent): void {
    this.loadTransactions(event.pageIndex + 1, event.pageSize);
  }

  retryTransactions(): void {
    this.loadTransactions(this.page().pageNumber, this.page().pageSize);
  }

  openWithdraw(): void {
    const walletSnapshot = this.wallet();
    if (!walletSnapshot) return;

    const ref = this.dialog.open(WithdrawDialog, {
      data: { wallet: walletSnapshot },
      width: '460px',
      disableClose: true,
    });

    ref.afterClosed().subscribe((result) => {
      if (result) {
        this.snackbar.showSuccess('Withdrawal initiated successfully');
        this.walletService.getWallet().subscribe();
        this.loadTransactions(1, this.page().pageSize);
      }
    });
  }

  private loadTransactions(page: number, pageSize: number): void {
    this.txLoading.set(true);
    this.txError.set(false);
    this.walletService.getTransactions(page, pageSize).subscribe({
      next: (res) => {
        this.page.set(res);
        this.txLoading.set(false);
      },
      error: () => {
        this.txError.set(true);
        this.txLoading.set(false);
      },
    });
  }
}
