import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type CheckInResult = 'VALID' | 'ALREADY_USED' | 'INVALID';

export interface VerifyCodeResponse {
  eventExternalId: string;
  eventTitle: string;
}

export interface CheckInResponse {
  result: CheckInResult;
  holderName: string;
  ticketType: string;
  checkedInAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class CheckInService {
  private readonly http = inject(HttpClient);

  verifyCode(code: string): Promise<VerifyCodeResponse> {
    return firstValueFrom(
      this.http
        .post<VerifyCodeResponse>(`${environment.apiBaseUrl}/check-in/verify-code`, { code })
        .pipe(catchError(this.extractError('Invalid or expired code'))),
    );
  }

  checkIn(ticketCode: string, checkInCode: string): Promise<CheckInResponse> {
    return firstValueFrom(
      this.http
        .post<CheckInResponse>(`${environment.apiBaseUrl}/tickets/${ticketCode}/check-in`, {
          checkInCode,
        })
        .pipe(catchError(this.extractError('Something went wrong. Try again.'))),
    );
  }

  private extractError(fallback: string) {
    return (err: HttpErrorResponse) => {
      const message = err.error?.detail ?? fallback;
      return throwError(() => new Error(message));
    };
  }
}
