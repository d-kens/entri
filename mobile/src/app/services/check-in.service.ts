import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

export type CheckInResult = 'VALID' | 'ALREADY_USED' | 'INVALID';

export interface CheckInResponse {
  result: CheckInResult;
  holderName: string;
  ticketType: string;
  checkedInAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class CheckInService {
  private readonly http = inject(HttpClient);

  // TODO: replace with real API call when backend is ready
  verifyCode(code: string): Promise<void> {
    return new Promise((resolve, reject) =>
      setTimeout(() => code === '1234' ? resolve() : reject(new Error('Invalid or expired code')), 800)
    );
  }

  checkIn(ticketCode: string, checkInCode: string): Promise<CheckInResponse> {
    return firstValueFrom(
      this.http.post<CheckInResponse>(
        `${environment.apiUrl}/tickets/${ticketCode}/check-in`,
        { checkInCode }
      )
    );
  }
}
