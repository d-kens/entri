import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserResponse} from '@core/models/user.models';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private http: HttpClient = inject(HttpClient);


  getUserByExternalKey(externalKey: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${environment.apiBaseUrl}/users/${externalKey}`);
  }

}
