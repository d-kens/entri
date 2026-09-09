import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { provideNativeDateAdapter } from '@angular/material/core';
import { environment } from 'environments/environment';

import { CreateTicketType } from './create-ticket-type';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventResponse } from '@features/events/models/event.models';
import { TicketTypeFormData } from '@features/events/ticket-type-form/ticket-type-form';

describe('CreateTicketType', () => {
  let component: CreateTicketType;
  let fixture: ComponentFixture<CreateTicketType>;
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
    startTime: '2026-06-01T10:00:00.000Z',
    endTime: '2026-06-01T12:00:00.000Z',
    bannerUrl: '',
    status: 'DRAFT',
    dateCreated: '2026-01-01T00:00:00.000Z',
  };

  const ticketTypeFormData: TicketTypeFormData = {
    name: 'VIP',
    price: '50',
    quantity: '10',
    description: 'desc',
    salesStartDate: '',
    salesEndDate: '',
    maxTicketsPerOrder: '2',
  };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CreateTicketType],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNativeDateAdapter(),
        { provide: SnackbarService, useValue: snackbar },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ eventExternalId: 'evt-1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTicketType);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushEvent() {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`).flush(event);
  }

  it('should create', () => {
    flushEvent();
    expect(component).toBeTruthy();
  });

  it('should load the event on init', () => {
    flushEvent();
    expect(component.event()).toEqual(event);
    expect(component.isLoadingEvent()).toBe(false);
  });

  it('should set hasError when loading the event fails', () => {
    fixture.detectChanges();
    httpMock
      .expectOne(`${environment.apiBaseUrl}/events/evt-1`)
      .flush('not found', { status: 404, statusText: 'Not Found' });

    expect(component.hasError()).toBe(true);
    expect(component.isLoadingEvent()).toBe(false);
  });

  it('should add the ticket type and navigate back to the event on success', () => {
    flushEvent();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.onSaved(ticketTypeFormData);

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1/ticket-types`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      name: 'VIP',
      price: 50,
      description: 'desc',
      quantity: 10,
      maxTicketsPerOrder: 2,
      saleStartDate: null,
      saleEndDate: null,
    });
    req.flush({ id: 1 });

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Ticket type added successfully');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
    expect(component.isSaving()).toBe(false);
  });

  it('should show an error message when adding the ticket type fails', () => {
    flushEvent();
    component.onSaved(ticketTypeFormData);

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1/ticket-types`);
    req.flush('boom', { status: 500, statusText: 'Server Error' });

    expect(snackbar.showError).toHaveBeenCalledWith('Failed to add ticket type');
    expect(component.isSaving()).toBe(false);
  });

  it('should ignore additional save attempts while a save is in progress', () => {
    flushEvent();
    component.isSaving.set(true);

    component.onSaved(ticketTypeFormData);

    httpMock.expectNone(`${environment.apiBaseUrl}/events/evt-1/ticket-types`);
  });
});
