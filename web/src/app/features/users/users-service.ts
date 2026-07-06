import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { UserResponse } from '@features/auth/models/user.models';
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
}
