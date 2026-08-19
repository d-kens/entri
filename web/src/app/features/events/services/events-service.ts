import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from 'environments/environment';
import {
  CategoryResponse,
  EventRequest,
  EventFilter,
  EventResponse,
  TicketTypeRequest,
  TicketTypeResponse,
  EventTicketReservationRequest,
  EventTicketReservationResponse,
  EventTicketReservationDetailResponse,
} from '@features/events/models/event.models';
import { ApiError, PageResponse } from '@shared/models/common.model';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private http = inject(HttpClient);

  getCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${environment.apiBaseUrl}/categories`);
  }

  createEvent(payload: EventRequest): Observable<EventResponse> {
    return this.http.post<EventResponse>(`${environment.apiBaseUrl}/events`, payload);
  }

  updateEvent(eventExternalId: string, payload: EventRequest): Observable<EventResponse> {
    return this.http.put<EventResponse>(
      `${environment.apiBaseUrl}/events/${eventExternalId}`,
      payload,
    );
  }

  getEvents(filter: EventFilter): Observable<PageResponse<EventResponse>> {
    const params = this.buildEventParams(filter);
    return this.http.get<PageResponse<EventResponse>>(`${environment.apiBaseUrl}/events`, {
      params,
    });
  }

  getEvent(eventExternalId: string): Observable<EventResponse> {
    return this.http.get<EventResponse>(`${environment.apiBaseUrl}/events/${eventExternalId}`);
  }

  publishEvent(eventExternalId: string): Observable<EventResponse> {
    return this.http
      .patch<EventResponse>(`${environment.apiBaseUrl}/events/${eventExternalId}/publish`, {})
      .pipe(catchError(this.toDisplayError('Failed to publish event')));
  }

  getManagedEvents(
    filter: Omit<EventFilter, 'organizerExternalId'>,
  ): Observable<PageResponse<EventResponse>> {
    const params = this.buildEventParams(filter);
    return this.http.get<PageResponse<EventResponse>>(`${environment.apiBaseUrl}/events/manage`, {
      params,
    });
  }

  getTicketTypeById(ticketTypeId: number): Observable<TicketTypeResponse> {
    return this.http.get<TicketTypeResponse>(
      `${environment.apiBaseUrl}/ticket-types/${ticketTypeId}`,
    );
  }

  getEventTicketTypes(eventExternalId: string): Observable<TicketTypeResponse[]> {
    return this.http.get<TicketTypeResponse[]>(
      `${environment.apiBaseUrl}/events/${eventExternalId}/ticket-types`,
    );
  }

  deleteTicketType(ticketTypeId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/ticket-types/${ticketTypeId}`);
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
    ticketTypeId: string,
    ticketType: TicketTypeRequest,
  ): Observable<TicketTypeResponse> {
    return this.http.put<TicketTypeResponse>(
      `${environment.apiBaseUrl}/ticket-types/${ticketTypeId}`,
      ticketType,
    );
  }

  reserveEventTickets(
    externalId: string,
    request: EventTicketReservationRequest,
  ): Observable<EventTicketReservationResponse> {
    return this.http.post<EventTicketReservationResponse>(
      `${environment.apiBaseUrl}/events/${externalId}/reservations`,
      request,
    );
  }

  getEventTicketReservation(
    externalId: string,
    reservationId: string,
  ): Observable<EventTicketReservationDetailResponse> {
    return this.http.get<EventTicketReservationDetailResponse>(
      `${environment.apiBaseUrl}/events/${externalId}/reservations/${reservationId}`,
    );
  }

  private toDisplayError(fallback: string) {
    return (err: HttpErrorResponse) => {
      const detail = (err.error as ApiError | null)?.detail;
      return throwError(() => new Error(typeof detail === 'string' ? detail : fallback));
    };
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
