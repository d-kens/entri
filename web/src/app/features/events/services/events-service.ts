import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { EventResponse } from '@core/models/event.models';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private http = inject(HttpClient);

  createEvent(formData: FormData): Observable<EventResponse> {
    return this.http.post<EventResponse>(`${environment.apiBaseUrl}/events`, formData);
  }
}
