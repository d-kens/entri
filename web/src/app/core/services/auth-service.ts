import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import {AccessToken, AuthRequest, JWTPayload} from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http: HttpClient = inject(HttpClient);
  private readonly ACCESS_TOKEN_KEY = 'access_token';

  private authStatusSignal = signal(this.hasValidToken());

  constructor() {}

  isAuthenticated(): boolean {
    const valid = this.hasValidToken();
    if (this.authStatusSignal() !== valid) {
      this.authStatusSignal.set(valid);
    }
    return valid;
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded = jwtDecode<JWTPayload>(token);
      if (!decoded.exp) return true;
      return Date.now() >= decoded.exp * 1000;
    } catch (error) {
      return true;
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  login(authRequest: AuthRequest): Observable<AccessToken> {
    return this.http
      .post<AccessToken>(`${environment.apiBaseUrl}/auth/login`, authRequest)
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

  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    this.authStatusSignal.set(false);
  }

  sendPasswordResetInstructions(email: string): Observable<null> {
    return this.http.post<null>(
      `${environment.apiBaseUrl}/auth/forgot-password`, email
    );
  }

}
