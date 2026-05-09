import { inject, Injectable, signal } from '@angular/core';
import { environment } from 'environments/environment';
import { HttpClient } from '@angular/common/http';
import { catchError, finalize, Observable, tap, throwError } from 'rxjs';
import {
  AuthResponse,
  AuthRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  RegisterUserRequest
} from '../models/auth.models';
import { UserResponse } from '@core/models/user.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http: HttpClient = inject(HttpClient);
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly EXTERNAL_ID_KEY = 'external_id';

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

  register(registerUserRequest: RegisterUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${environment.apiBaseUrl}/auth/register`, registerUserRequest);
  }

  login(authRequest: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, authRequest, {
        withCredentials: true
      })
      .pipe(
        tap((response) => {
          localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
          localStorage.setItem(this.EXTERNAL_ID_KEY, response.user.externalKey);
          this.authStatusSignal.set(true);
        }),
        catchError((error) => {
          this.authStatusSignal.set(false);
          return throwError(() => error);
        })
      );
  }

  refreshToken(): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/refresh-token`, {}, {withCredentials: true})
      .pipe(
        tap(response => {
          localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
          this.authStatusSignal.set(true);
        })
      );
  }

  getExternalId(): string | null {
    return localStorage.getItem(this.EXTERNAL_ID_KEY);
  }

  clearSession(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.EXTERNAL_ID_KEY);
    this.authStatusSignal.set(false);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/logout`, {}, {
      withCredentials: true
    })
      .pipe(
        finalize(() => {
          localStorage.removeItem(this.ACCESS_TOKEN_KEY);
          localStorage.removeItem(this.EXTERNAL_ID_KEY);
          this.authStatusSignal.set(false);
        })
      );
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles: string[] = payload.roles ?? [];
      if (roles.includes('ADMIN')) return 'ADMIN';
      if (roles.includes('AGENT')) return 'AGENT';
      if (roles.includes('MERCHANT')) return 'MERCHANT';
      return roles[0] ?? null;
    } catch {
      return null;
    }
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/reset-password`, request);
  }
}
