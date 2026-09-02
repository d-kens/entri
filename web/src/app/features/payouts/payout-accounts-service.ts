import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PayoutAccountRequest, PayoutAccountResponse } from './models/payout.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PayoutAccountsService {
  private http = inject(HttpClient);

  getAccounts(organizerKey: string): Observable<PayoutAccountResponse[]> {
    return this.http.get<PayoutAccountResponse[]>(
      `${environment.apiBaseUrl}/users/${organizerKey}/payout-accounts`,
    );
  }

  addAccount(
    organizerKey: string,
    request: PayoutAccountRequest,
  ): Observable<PayoutAccountResponse> {
    return this.http.post<PayoutAccountResponse>(
      `${environment.apiBaseUrl}/users/${organizerKey}/payout-accounts`,
      request,
    );
  }

  setDefault(organizerKey: string, accountId: string): Observable<PayoutAccountResponse> {
    return this.http.put<PayoutAccountResponse>(
      `${environment.apiBaseUrl}/users/${organizerKey}/payout-accounts/${accountId}/default`,
      {},
    );
  }

  deleteAccount(organizerKey: string, accountId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiBaseUrl}/users/${organizerKey}/payout-accounts/${accountId}`,
    );
  }
}
