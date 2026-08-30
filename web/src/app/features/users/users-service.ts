import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  ChangePasswordRequest,
  UpdateUserRequest,
  UserResponse,
} from '@features/users/models/user.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private http: HttpClient = inject(HttpClient);

  currentUser = signal<UserResponse | null>(null);

  getUserByExternalKey(externalKey: string): Observable<UserResponse> {
    return this.http
      .get<UserResponse>(`${environment.apiBaseUrl}/users/${externalKey}`)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  updateUser(externalKey: string, request: UpdateUserRequest): Observable<UserResponse> {
    return this.http
      .put<UserResponse>(`${environment.apiBaseUrl}/users/${externalKey}`, request)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  changePassword(externalKey: string, request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(
      `${environment.apiBaseUrl}/users/${externalKey}/change-password`,
      request,
    );
  }
}
