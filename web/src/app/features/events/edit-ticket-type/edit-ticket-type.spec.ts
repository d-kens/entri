import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { environment } from 'environments/environment';

import { EditTicketType } from './edit-ticket-type';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventResponse, TicketTypeResponse } from '@features/events/models/event.models';
import { TicketTypeFormData } from '@features/events/ticket-type-form/ticket-type-form';

describe('EditTicketType', () => {
  let component: EditTicketType;
  let fixture: ComponentFixture<EditTicketType>;
  let httpMock: HttpTestingController;
  let snackbar: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let dialog: { open: ReturnType<typeof vi.fn> };

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

  const ticketType: TicketTypeResponse = {
    id: 5,
    name: 'VIP',
    description: 'desc',
    price: 50,
    quantity: 10,
    availableQuantity: 10,
    maxTicketsPerOrder: 2,
    saleStatus: 'UPCOMING',
    availabilityStatus: 'AVAILABLE',
  };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };
    dialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EditTicketType],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNativeDateAdapter(),
        { provide: SnackbarService, useValue: snackbar },
        { provide: MatDialog, useValue: dialog },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ eventExternalId: 'evt-1', ticketTypeId: '5' }),
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditTicketType);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushInitialLoad() {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`).flush(event);
    httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/5`).flush(ticketType);
  }

  it('should create', () => {
    flushInitialLoad();
    expect(component).toBeTruthy();
  });

  it('should load the event and ticket type on init', () => {
    flushInitialLoad();
    expect(component.event()).toEqual(event);
    expect(component.ticketTypeFormData()).toEqual({
      name: 'VIP',
      description: 'desc',
      price: '50',
      quantity: '10',
      maxTicketsPerOrder: 2,
      salesStartDate: '',
      salesEndDate: '',
    });
    expect(component.loading()).toBe(false);
  });

  it('should set hasError when loading fails', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`).flush(event);
    httpMock
      .expectOne(`${environment.apiBaseUrl}/ticket-types/5`)
      .flush('not found', { status: 404, statusText: 'Not Found' });

    expect(component.hasError()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should update the ticket type and navigate back to the event on save', () => {
    flushInitialLoad();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const formData: TicketTypeFormData = {
      name: 'VIP Updated',
      price: '60',
      quantity: '5',
      description: 'new desc',
      salesStartDate: '',
      salesEndDate: '',
      maxTicketsPerOrder: '3',
    };
    component.onSaved(formData);

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.name).toBe('VIP Updated');
    req.flush(ticketType);

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Ticket type updated successfully');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
  });

  it('should show an error message when updating fails', () => {
    flushInitialLoad();
    component.onSaved({
      name: 'VIP',
      price: '50',
      quantity: '10',
      description: 'desc',
      salesStartDate: '',
      salesEndDate: '',
      maxTicketsPerOrder: '2',
    });

    httpMock
      .expectOne(`${environment.apiBaseUrl}/ticket-types/5`)
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(snackbar.showError).toHaveBeenCalledWith('Failed to update ticket type');
    expect(component.loading()).toBe(false);
  });

  it('should delete the ticket type and navigate back when the delete dialog is confirmed', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.openDeleteDialog();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/ticket-types/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Ticket type deleted');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
  });

  it('should not delete the ticket type when the delete dialog is cancelled', () => {
    flushInitialLoad();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });

    component.openDeleteDialog();

    httpMock.expectNone(`${environment.apiBaseUrl}/ticket-types/5`);
  });
});
