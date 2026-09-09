import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { DOCUMENT } from '@angular/common';
import { of, throwError } from 'rxjs';

import { Checkout } from './checkout';
import { EventsService } from '../services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import {
  EventResponse,
  EventTicketReservationDetailResponse,
} from '@features/events/models/event.models';

const event: EventResponse = {
  externalId: 'evt-1',
  title: 'Sample Event',
  description: 'desc',
  categoryName: 'Music',
  categoryId: 1,
  currency: 'USD',
  venueName: 'Venue',
  venueCity: 'City',
  venueCountry: 'Country',
  startTime: '2026-01-01T10:00:00Z',
  endTime: '2026-01-01T12:00:00Z',
  bannerUrl: '',
  status: 'PUBLISHED',
  dateCreated: '2025-12-01T00:00:00Z',
};

const FIXED_EXPIRY = '2026-06-01T00:05:00.000Z';

function reservation(
  overrides: Partial<EventTicketReservationDetailResponse> = {},
): EventTicketReservationDetailResponse {
  return {
    expiresAt: FIXED_EXPIRY,
    reservationId: 'res-1',
    totalAmount: 20,
    externalEventId: 'evt-1',
    status: 'PENDING',
    reservationItems: [{ quantity: 2, ticketType: 'General', unitPrice: 10, totalAmount: 20 }],
    ...overrides,
  };
}

describe('Checkout', () => {
  let component: Checkout;
  let fixture: ComponentFixture<Checkout>;
  let eventsService: {
    getEvent: ReturnType<typeof vi.fn>;
    getEventTicketReservation: ReturnType<typeof vi.fn>;
    checkout: ReturnType<typeof vi.fn>;
  };
  let snackbarService: { showError: ReturnType<typeof vi.fn> };
  let fakeDocument: { location: { href: string } };

  async function setup() {
    const fakeLocation = { href: '' };
    const documentOverride = new Proxy(document, {
      get(target, prop, receiver) {
        if (prop === 'location') return fakeLocation;
        const value = Reflect.get(target, prop, target);
        return typeof value === 'function' ? value.bind(target) : value;
      },
    }) as unknown as Document;
    fakeDocument = { location: fakeLocation };

    await TestBed.configureTestingModule({
      imports: [Checkout],
      providers: [
        provideRouter([]),
        { provide: EventsService, useValue: eventsService },
        { provide: SnackbarService, useValue: snackbarService },
        { provide: DOCUMENT, useValue: documentOverride },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ externalId: 'evt-1', reservationId: 'res-1' }),
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
  }

  beforeEach(() => {
    eventsService = {
      getEvent: vi.fn(() => of(event)),
      getEventTicketReservation: vi.fn(() => of(reservation())),
      checkout: vi.fn(),
    };
    snackbarService = { showError: vi.fn() };
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should create and read route params', async () => {
    await setup();
    expect(component).toBeTruthy();
    expect(component.externalId).toBe('evt-1');
    expect(component.reservationId).toBe('res-1');
  });

  it('should load event and reservation on init', async () => {
    await setup();
    fixture.detectChanges();

    expect(eventsService.getEvent).toHaveBeenCalledWith('evt-1');
    expect(eventsService.getEventTicketReservation).toHaveBeenCalledWith('evt-1', 'res-1');
    expect(component.event()).toEqual(event);
    expect(component.reservation()).toEqual(reservation());
    expect(component.loading()).toBe(false);
  });

  it('should set error state when loading fails', async () => {
    eventsService.getEvent.mockReturnValue(throwError(() => new Error('boom')));
    await setup();
    fixture.detectChanges();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should compute totalTickets and isFree from the reservation', async () => {
    await setup();
    fixture.detectChanges();

    expect(component.totalTickets()).toBe(2);
    expect(component.isFree()).toBe(false);
  });

  it('should mark isFree true when totalAmount is zero', async () => {
    eventsService.getEventTicketReservation.mockReturnValue(of(reservation({ totalAmount: 0 })));
    await setup();
    fixture.detectChanges();

    expect(component.isFree()).toBe(true);
  });

  it('should not call checkout and should mark form touched when contact form is invalid', async () => {
    await setup();
    fixture.detectChanges();

    component.pay();

    expect(eventsService.checkout).not.toHaveBeenCalled();
    expect(component.contactForm.get('email')?.touched).toBe(true);
  });

  it('should call checkout and redirect on success when form is valid', async () => {
    eventsService.checkout.mockReturnValue(of({ checkoutUrl: 'https://pay.example.com/x' }));
    await setup();
    fixture.detectChanges();

    component.contactForm.setValue({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
    });

    component.pay();

    expect(eventsService.checkout).toHaveBeenCalledWith('res-1', {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
    });
    expect(fakeDocument.location.href).toBe('https://pay.example.com/x');
  });

  it('should show an error and stop paying when checkout fails', async () => {
    eventsService.checkout.mockReturnValue(throwError(() => new Error('Checkout failed')));
    await setup();
    fixture.detectChanges();

    component.contactForm.setValue({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
    });

    component.pay();

    expect(snackbarService.showError).toHaveBeenCalledWith('Checkout failed');
    expect(component.paying()).toBe(false);
  });

  it('should start a countdown timer for a pending reservation and expire it', async () => {
    eventsService.getEventTicketReservation.mockReturnValue(
      of(reservation({ expiresAt: new Date(Date.now() + 1000).toISOString() })),
    );
    await setup();
    fixture.detectChanges();

    expect(component.expired()).toBe(false);

    await new Promise((resolve) => setTimeout(resolve, 1200));

    expect(component.expired()).toBe(true);
  }, 3000);

  it('should compute confirmed when reservation status is CONFIRMED', async () => {
    eventsService.getEventTicketReservation.mockReturnValue(
      of(reservation({ status: 'CONFIRMED' })),
    );
    await setup();
    fixture.detectChanges();

    expect(component.confirmed()).toBe(true);
  });
});
