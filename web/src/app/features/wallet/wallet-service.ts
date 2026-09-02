import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { catchError, finalize, tap, throwError } from 'rxjs';
import { Observable } from 'rxjs';
import {
  BankCodeResponse,
  WalletResponse,
  WalletTransactionsPage,
  WithdrawRequest,
  WithdrawResponse,
} from '@features/wallet/models/wallet.models';
import { AuthService } from '@features/auth/auth-service';
import { ApiError } from '@shared/models/common.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WalletService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  wallet = signal<WalletResponse | null>(null);
  walletLoading = signal(false);
  bankCodes = signal<BankCodeResponse[]>([]);

  getWallet(): Observable<WalletResponse> {
    const externalKey = this.authService.getExternalId();
    this.walletLoading.set(true);
    return this.http
      .get<WalletResponse>(`${environment.apiBaseUrl}/users/${externalKey}/wallet`)
      .pipe(
        tap((wallet) => this.wallet.set(wallet)),
        finalize(() => this.walletLoading.set(false)),
      );
  }

  getTransactions(page = 1, pageSize = 10): Observable<WalletTransactionsPage> {
    const externalKey = this.authService.getExternalId();
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http
      .get<WalletTransactionsPage>(
        `${environment.apiBaseUrl}/users/${externalKey}/wallet/transactions`,
        { params },
      )
      .pipe(catchError(this.toDisplayError('Failed to load transactions')));
  }

  withdraw(request: WithdrawRequest): Observable<WithdrawResponse> {
    const externalKey = this.authService.getExternalId();
    return this.http
      .post<WithdrawResponse>(
        `${environment.apiBaseUrl}/users/${externalKey}/wallet/withdraw`,
        request,
      )
      .pipe(catchError(this.toDisplayError('Withdrawal failed. Please try again.')));
  }

  getBankCodes(): Observable<BankCodeResponse[]> {
    return this.http
      .get<BankCodeResponse[]>(`${environment.apiBaseUrl}/users/wallet/bank-codes`)
      .pipe(
        tap((codes) => this.bankCodes.set(codes)),
        catchError(this.toDisplayError('Failed to load bank codes')),
      );
  }

  private toDisplayError(fallback: string) {
    return (err: HttpErrorResponse) => {
      const detail = (err.error as ApiError | null)?.detail;
      return throwError(() => new Error(typeof detail === 'string' ? detail : fallback));
    };
  }
}
