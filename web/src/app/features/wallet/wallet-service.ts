import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { WalletResponse } from '@features/wallet/models/wallet.models';
import { AuthService } from '@features/auth/auth-service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WalletService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  wallet = signal<WalletResponse | null>(null);
  walletLoading = signal(false);

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
}
