import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import { CreateEventRequest, EventResponse } from '@core/models/event.models';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private http = inject(HttpClient);

  uploadBanner(file: File): Observable<{ url: string }> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ url: string }>(`${environment.apiBaseUrl}/media/upload`, fd);
  }

  deleteBanner(url: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/media/upload`, { params: { url } });
  }

  createEvent(payload: CreateEventRequest): Observable<EventResponse> {
    return this.http.post<EventResponse>(`${environment.apiBaseUrl}/events`, payload);
  }
}
