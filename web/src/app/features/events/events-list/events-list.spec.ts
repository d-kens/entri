import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from 'environments/environment';

import { EventsList } from './events-list';
import { SnackbarService } from '@shared/services/snackbar-service';
import { UsersService } from '@features/users/users-service';
import { EventResponse } from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';
import { UserResponse } from '@features/users/models/user.models';

describe('EventsList', () => {
  let component: EventsList;
  let fixture: ComponentFixture<EventsList>;
  let httpMock: HttpTestingController;
  let snackbar: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  const event: EventResponse = {
    externalId: 'evt-1',
    title: 'Concert',
    description: 'desc',
    categoryName: 'Music',
    categoryId: 1,
    currency: 'KES',
    venueName: 'Arena',
    venueCity: 'Nairobi',
    venueCountry: 'Kenya',
    startTime: '2026-01-01T10:00:00Z',
    endTime: '2026-01-01T12:00:00Z',
    bannerUrl: '',
    status: 'DRAFT',
    dateCreated: '2025-12-01T00:00:00Z',
  };

  const page: PageResponse<EventResponse> = {
    content: [event],
    number: 0,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EventsList],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventsList);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushInitialLoad() {
    fixture.detectChanges();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/events/manage`);
    req.flush(page);
  }

  it('should create', () => {
    flushInitialLoad();
    expect(component).toBeTruthy();
  });

  it('should load managed events on init', () => {
    flushInitialLoad();
    expect(component.page()).toEqual(page);
    expect(component.loading()).toBe(false);
  });

  it('should request the first page with a size of 10 on init', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/events/manage`);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush(page);
  });

  it('should set error state when the load fails', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/events/manage`);
    req.flush('boom', { status: 500, statusText: 'Server Error' });
    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should reload with the new pagination on page change', () => {
    flushInitialLoad();
    component.onPage({ pageIndex: 1, pageSize: 25 } as never);
    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/events/manage`);
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('25');
    req.flush({ ...page, number: 1, size: 25 });
  });

  it('should show a success message and reload after publishing an event', () => {
    flushInitialLoad();
    component.publishEvent('evt-1');

    const publishReq = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1/publish`);
    expect(publishReq.request.method).toBe('PATCH');
    publishReq.flush({ ...event, status: 'PUBLISHED' });

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Event published');

    const reloadReq = httpMock.expectOne(
      (r) => r.url === `${environment.apiBaseUrl}/events/manage`,
    );
    reloadReq.flush(page);
  });

  it('should show an error message when publishing fails', () => {
    flushInitialLoad();
    component.publishEvent('evt-1');

    const publishReq = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1/publish`);
    publishReq.flush({ detail: 'Cannot publish' }, { status: 400, statusText: 'Bad Request' });

    expect(snackbar.showError).toHaveBeenCalledWith('Cannot publish');
  });

  it('should show a success message and reload after cancelling an event', () => {
    flushInitialLoad();
    component.cancelEvent('evt-1');

    const cancelReq = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1/cancel`);
    expect(cancelReq.request.method).toBe('PATCH');
    cancelReq.flush({ ...event, status: 'CANCELLED' });

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Event cancelled');

    const reloadReq = httpMock.expectOne(
      (r) => r.url === `${environment.apiBaseUrl}/events/manage`,
    );
    reloadReq.flush(page);
  });

  it('should navigate to the event detail page', () => {
    flushInitialLoad();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.goToEvent('evt-1');

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
  });

  it('should reload the current page on retry', () => {
    flushInitialLoad();
    component.retry();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/events/manage`);
    req.flush(page);
  });

  it('should reflect admin role from the current user', () => {
    const usersService = TestBed.inject(UsersService);
    expect(component.isAdmin()).toBe(false);

    usersService.currentUser.set({ role: 'ADMIN' } as UserResponse);
    expect(component.isAdmin()).toBe(true);

    flushInitialLoad();
  });
});
