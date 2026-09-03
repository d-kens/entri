import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  WalletResponse,
  WalletTransaction,
  WithdrawalRequest,
  WithdrawalResponse,
} from './models/wallet.models';
import { PageResponse } from '@shared/models/common.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private http = inject(HttpClient);

  getWallet(organizerExternalKey: string): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(
      `${environment.apiBaseUrl}/wallet/${organizerExternalKey}`,
    );
  }

  getTransactions(
    organizerExternalKey: string,
    page: number,
    size: number,
  ): Observable<PageResponse<WalletTransaction>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dateCreated,desc');
    return this.http.get<PageResponse<WalletTransaction>>(
      `${environment.apiBaseUrl}/wallet/${organizerExternalKey}/transactions`,
      { params },
    );
  }

  withdraw(
    organizerExternalKey: string,
    request: WithdrawalRequest,
  ): Observable<WithdrawalResponse> {
    return this.http.post<WithdrawalResponse>(
      `${environment.apiBaseUrl}/wallet/${organizerExternalKey}/withdraw`,
      request,
    );
  }
}
