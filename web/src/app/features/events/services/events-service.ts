import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';
import {
  CategoryResponse,
  EventRequest,
  EventDetailResponse,
  EventFilter,
  EventResponse,
  TicketTypeRequest,
  TicketTypeResponse,
} from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private http = inject(HttpClient);

  getCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${environment.apiBaseUrl}/categories`);
  }

  uploadBanner(file: File): Observable<{ url: string }> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ url: string }>(`${environment.apiBaseUrl}/media/upload`, fd);
  }

  deleteBanner(url: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/media/upload`, { params: { url } });
  }

  createEvent(payload: EventRequest): Observable<EventResponse> {
    return this.http.post<EventResponse>(`${environment.apiBaseUrl}/events`, payload);
  }

  getEvents(filter: EventFilter): Observable<PageResponse<EventResponse>> {
    const params = this.buildEventParams(filter);
    return this.http.get<PageResponse<EventResponse>>(`${environment.apiBaseUrl}/events`, {
      params,
    });
  }

  getEvent(id: string): Observable<EventDetailResponse> {
    return this.http.get<EventDetailResponse>(`${environment.apiBaseUrl}/events/${id}`);
  }

  updateEvent(id: string, payload: EventRequest): Observable<EventResponse> {
    return this.http.put<EventResponse>(`${environment.apiBaseUrl}/events/${id}`, payload);
  }

  getManagedEvents(
    filter: Omit<EventFilter, 'organizerExternalId'>,
  ): Observable<PageResponse<EventResponse>> {
    const params = this.buildEventParams(filter);
    return this.http.get<PageResponse<EventResponse>>(`${environment.apiBaseUrl}/events/manage`, {
      params,
    });
  }

  addEventTicketType(
    eventExternalId: string,
    ticketType: TicketTypeRequest,
  ): Observable<TicketTypeResponse> {
    return this.http.post<TicketTypeResponse>(
      `${environment.apiBaseUrl}/events/${eventExternalId}/ticket-types`,
      ticketType,
    );
  }

  updateEventTicketType(
    eventExternalId: string,
    ticketTypeId: string,
    ticketType: TicketTypeRequest,
  ): Observable<TicketTypeResponse> {
    return this.http.put<TicketTypeResponse>(
      `${environment.apiBaseUrl}/events/${eventExternalId}/ticket-types/${ticketTypeId}`,
      ticketType,
    );
  }

  private buildEventParams(
    filter: Omit<EventFilter, 'organizerExternalId'> & { organizerExternalId?: string },
  ): HttpParams {
    let params = new HttpParams();
    if (filter.page !== undefined) params = params.set('page', filter.page);
    if (filter.size !== undefined) params = params.set('size', filter.size);
    if (filter.sortDirection) params = params.set('sortDirection', filter.sortDirection);
    if (filter.categoryId !== undefined) params = params.set('categoryId', filter.categoryId);
    if (filter.searchTerm) params = params.set('searchTerm', filter.searchTerm);
    if (filter.organizerExternalId)
      params = params.set('organizerExternalId', filter.organizerExternalId);
    if (filter.startFrom) params = params.set('startFrom', filter.startFrom);
    if (filter.startTo) params = params.set('startTo', filter.startTo);
    return params;
  }
}
