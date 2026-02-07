import { inject, Injectable, signal } from '@angular/core';
import { environment } from 'environments/environment';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import {AccessToken, AuthRequest, ForgotPasswordPayload, ResetPasswordPayload} from '../models/auth.models';
import {UserResponse} from '@core/models/user.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http: HttpClient = inject(HttpClient);
  private readonly ACCESS_TOKEN_KEY = 'access_token';

  private authStatusSignal = signal(!!this.getToken());

  constructor() {}

  isAuthenticated(): boolean {
    const authenticated = !!this.getToken();
    this.authStatusSignal.set(authenticated);
    return authenticated;
  }

  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  login(authRequest: AuthRequest): Observable<AccessToken> {
    return this.http
      .post<AccessToken>(`${environment.apiBaseUrl}/auth/login`, authRequest, {
        withCredentials: true
      })
      .pipe(
        tap({
          next: (response) => {
            localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
            this.authStatusSignal.set(true);
          },
        }),
        catchError((error) => {
          this.authStatusSignal.set(false);
          return throwError(() => error);
        })
      );
  }

  refreshToken(): Observable<AccessToken> {
    return this.http
      .post<AccessToken>(`${environment.apiBaseUrl}/auth/refresh-token`, {}, {withCredentials: true})
      .pipe(
        tap(response => {
          localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
          this.authStatusSignal.set(true);
        })
      );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/logout`, {}, {
      withCredentials: true
    })
      .pipe(
        tap(() => {
          localStorage.removeItem(this.ACCESS_TOKEN_KEY);
          this.authStatusSignal.set(false);
        })
      );
  }

  sendPasswordResetInstructions(payload: ForgotPasswordPayload): Observable<string> {
    return this.http.post<string>(
      `${environment.apiBaseUrl}/auth/forgot-password`, payload
    );
  }

  resetPassword(payload: ResetPasswordPayload): Observable<string> {
    return this.http.post<string>(
      `${environment.apiBaseUrl}/auth/reset-password`,
      payload
    )
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${environment.apiBaseUrl}/auth/me`)
  }
}
