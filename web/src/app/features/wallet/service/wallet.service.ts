import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {WalletResponse, WalletTransactionResponse} from '@features/wallet/model/wallet.model';
import {PageResponse} from '@core/models/common.model';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/wallet`;

  getMyWallet(): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(this.apiUrl);
  }

  getMyTransactions(page: number = 0, size: number = 20): Observable<PageResponse<WalletTransactionResponse>> {
    return this.http.get<PageResponse<WalletTransactionResponse>>(`${this.apiUrl}/transactions`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }
}
