import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CreateUserRequest, UserResponse} from '../models/user.models';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http: HttpClient = inject(HttpClient);

  createUser(payload: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(
      `${environment.apiBaseUrl}/users`,
      payload
    )
  }
}
