import {inject, Injectable} from '@angular/core';
import { environment } from 'environments/environment';
import {HttpClient} from '@angular/common/http';
import {ChangePasswordRequest, RegisterMerchantRequest, UpdateProfileRequest, UserResponse} from '../models/user.models';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http: HttpClient = inject(HttpClient);

  registerMerchant(payload: RegisterMerchantRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(
      `${environment.apiBaseUrl}/auth/register`,
      payload
    )
  }

  updateProfile(externalId: string, payload: UpdateProfileRequest): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${environment.apiBaseUrl}/users/${externalId}`, payload);
  }

  changePassword(payload: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/change-password`, payload);
  }
}
