import {inject, Injectable} from '@angular/core';
import { environment } from 'environments/environment';
import {HttpClient} from '@angular/common/http';
import {RegisterMerchantRequest, UserResponse} from '../models/user.models';
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
}
