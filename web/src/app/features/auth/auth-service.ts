import { inject, Injectable, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { catchError, finalize, Observable, tap, throwError } from 'rxjs';
import {
  AuthResponse,
  AccessTokenResponse,
  AuthRequest,
  ResetPasswordRequest,
  RegisterUserRequest,
} from './models/auth.models';
import { UserResponse } from '@features/users/models/user.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http: HttpClient = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly ACCESS_TOKEN_KEY = 'access_token';

  private authStatusSignal = signal(!!this.getToken());

  constructor() {}

  isAuthenticated(): boolean {
    const authenticated = !!this.getToken();
    this.authStatusSignal.set(authenticated);
    return authenticated;
  }

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  private decodeToken(): Record<string, string> | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    return this.decodeToken()?.['role'] ?? null;
  }

  getExternalId(): string | null {
    return this.decodeToken()?.['sub'] ?? null;
  }

  register(registerUserRequest: RegisterUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(
      `${environment.apiBaseUrl}/auth/register`,
      registerUserRequest,
    );
  }

  login(authRequest: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, authRequest, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken.token);
          this.authStatusSignal.set(true);
        }),
        catchError((error) => {
          this.authStatusSignal.set(false);
          return throwError(() => error);
        }),
      );
  }

  refreshToken(): Observable<AccessTokenResponse> {
    return this.http
      .post<AccessTokenResponse>(
        `${environment.apiBaseUrl}/auth/refresh-token`,
        {},
        { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          localStorage.setItem(this.ACCESS_TOKEN_KEY, response.token);
          this.authStatusSignal.set(true);
        }),
      );
  }

  clearSession(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    }
    this.authStatusSignal.set(false);
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(
        `${environment.apiBaseUrl}/auth/logout`,
        {},
        {
          withCredentials: true,
        },
      )
      .pipe(
        finalize(() => {
          localStorage.removeItem(this.ACCESS_TOKEN_KEY);
          this.authStatusSignal.set(false);
        }),
      );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/forgot-password`, { email });
  }

  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/reset-password`, request);
  }
}
