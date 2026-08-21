import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { WalletResponse } from '@features/wallet/models/wallet.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WalletService {
  private http = inject(HttpClient);

  wallet = signal<WalletResponse | null>(null);
  walletLoading = signal(false);

  getWallet(): Observable<WalletResponse> {
    this.walletLoading.set(true);
    return this.http.get<WalletResponse>(`${environment.apiBaseUrl}/users/me/wallet`).pipe(
      tap((wallet) => this.wallet.set(wallet)),
      finalize(() => this.walletLoading.set(false)),
    );
  }
}
