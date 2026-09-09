import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from 'environments/environment';
import { EventsService } from './events-service';
import { EventFilter } from '@features/events/models/event.models';

describe('EventsService', () => {
  let service: EventsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EventsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EventsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET categories once and share the response across subscribers', () => {
    const categories = [{ id: 1, name: 'Music' }] as never;
    let first: unknown;
    let second: unknown;

    service.getCategories().subscribe((res) => (first = res));
    service.getCategories().subscribe((res) => (second = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    expect(req.request.method).toBe('GET');
    req.flush(categories);

    expect(first).toEqual(categories);
    expect(second).toEqual(categories);
  });

  it('should re-fetch categories after a create/update/delete invalidates the cache', () => {
    service.getCategories().subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([{ id: 1, name: 'Music' }]);

    service.createCategory({ name: 'Sports' } as never).subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush({ id: 2, name: 'Sports' });

    let categories: unknown;
    service.getCategories().subscribe((res) => (categories = res));
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([
      { id: 1, name: 'Music' },
      { id: 2, name: 'Sports' },
    ]);

    expect(categories).toEqual([
      { id: 1, name: 'Music' },
      { id: 2, name: 'Sports' },
    ]);
  });

  it('should re-fetch categories on the next call after a failed load', () => {
    let firstError: unknown;
    service.getCategories().subscribe({ error: (err) => (firstError = err) });
    httpMock
      .expectOne(`${environment.apiBaseUrl}/categories`)
      .flush('boom', { status: 500, statusText: 'Server Error' });
    expect(firstError).toBeTruthy();

    let categories: unknown;
    service.getCategories().subscribe((res) => (categories = res));
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush([{ id: 1, name: 'Music' }]);

    expect(categories).toEqual([{ id: 1, name: 'Music' }]);
  });

  it('should POST to create a category', () => {
    const request = { name: 'Music' } as never;
    service.createCategory(request).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('should map category creation errors to the detail message when present', () => {
    let error: Error | undefined;
    service.createCategory({} as never).subscribe({ error: (err) => (error = err) });
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    req.flush({ detail: 'Category already exists' }, { status: 409, statusText: 'Conflict' });
    expect(error?.message).toBe('Category already exists');
  });

  it('should fall back to the default message when creation error has no detail', () => {
    let error: Error | undefined;
    service.createCategory({} as never).subscribe({ error: (err) => (error = err) });
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    req.flush({}, { status: 500, statusText: 'Server Error' });
    expect(error?.message).toBe('Failed to create category');
  });

  it('should PUT to update a category', () => {
    service.updateCategory(5, { name: 'Sports' } as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories/5`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should DELETE a category', () => {
    service.deleteCategory(5).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should POST to create an event', () => {
    service.createEvent({} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should PUT to update an event', () => {
    service.updateEvent('ext-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should build query params from the given filter for getEvents', () => {
    const filter: EventFilter = {
      page: 1,
      size: 20,
      sortDirection: 'ASC',
      categoryId: 3,
      searchTerm: 'jazz',
      startFrom: '2026-01-01',
      startTo: '2026-02-01',
    } as never;

    service.getEvents(filter).subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/events` &&
        r.params.get('page') === '1' &&
        r.params.get('size') === '20' &&
        r.params.get('sortDirection') === 'ASC' &&
        r.params.get('categoryId') === '3' &&
        r.params.get('searchTerm') === 'jazz' &&
        r.params.get('startFrom') === '2026-01-01' &&
        r.params.get('startTo') === '2026-02-01',
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should omit unset filter fields from the query params for getEvents', () => {
    service.getEvents({} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events`);
    expect(req.request.params.keys().length).toBe(0);
    req.flush({});
  });

  it('should GET a single event by external id', () => {
    service.getEvent('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should PATCH to publish an event', () => {
    service.publishEvent('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/publish`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should PATCH to cancel an event', () => {
    service.cancelEvent('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/cancel`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should POST to generate a check-in code', () => {
    service.generateCheckInCode('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/check-in-code`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should GET managed events with an organizer-less filter', () => {
    service.getManagedEvents({ page: 0, size: 10 } as never).subscribe();
    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/events/manage` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '10',
    );
    req.flush({});
  });

  it('should GET a ticket type by id', () => {
    service.getTicketTypeById(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/7`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should GET ticket types for an event', () => {
    service.getEventTicketTypes('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/ticket-types`);
    req.flush([]);
  });

  it('should DELETE a ticket type', () => {
    service.deleteTicketType(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should POST to add a ticket type to an event', () => {
    service.addEventTicketType('ext-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/ticket-types`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should PUT to update a ticket type', () => {
    service.updateEventTicketType('7', {} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/7`);
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should POST to reserve tickets for an event', () => {
    service.reserveEventTickets('ext-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/reservations`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should GET a reservation detail', () => {
    service.getEventTicketReservation('ext-1', 'res-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/ext-1/reservations/res-1`);
    req.flush({});
  });

  it('should POST to checkout a reservation', () => {
    service.checkout('res-1', {} as never).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/checkout/res-1`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should map checkout errors to the fallback message when no detail is present', () => {
    let error: Error | undefined;
    service.checkout('res-1', {} as never).subscribe({ error: (err) => (error = err) });
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/checkout/res-1`);
    req.flush({}, { status: 500, statusText: 'Server Error' });
    expect(error?.message).toBe('Checkout failed. Please try again.');
  });

  it('should GET paginated tickets for an event with default paging', () => {
    service.getEventTickets('ext-1').subscribe();
    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/events/ext-1/tickets` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '10',
    );
    req.flush({});
  });

  it('should GET tickets for a reservation', () => {
    service.getTicketsByReservation('res-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/reservations/res-1/tickets`);
    req.flush([]);
  });

  it('should GET a single ticket by external id', () => {
    service.getTicket('ext-1').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/tickets/ext-1`);
    req.flush({});
  });

  it('should map ticket-not-found errors to the fallback message', () => {
    let error: Error | undefined;
    service.getTicket('missing').subscribe({ error: (err) => (error = err) });
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/tickets/missing`);
    req.flush({}, { status: 404, statusText: 'Not Found' });
    expect(error?.message).toBe('Ticket not found');
  });
});
