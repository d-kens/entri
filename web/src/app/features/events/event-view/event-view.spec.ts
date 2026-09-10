import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { EventView } from './event-view';
import { EventsService } from '../services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventResponse, TicketTypeResponse } from '@features/events/models/event.models';

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

function ticketType(overrides: Partial<TicketTypeResponse> = {}): TicketTypeResponse {
  return {
    id: 1,
    name: 'General',
    price: 10,
    quantity: 100,
    availableQuantity: 3,
    maxTicketsPerOrder: 2,
    saleStatus: 'ON_SALE',
    availabilityStatus: 'AVAILABLE',
    ...overrides,
  };
}

describe('EventView', () => {
  let component: EventView;
  let fixture: ComponentFixture<EventView>;
  let eventsService: {
    getEvent: ReturnType<typeof vi.fn>;
    getEventTicketTypes: ReturnType<typeof vi.fn>;
    reserveEventTickets: ReturnType<typeof vi.fn>;
  };
  let snackbar: { showError: ReturnType<typeof vi.fn> };
  let router: Router;

  async function setup() {
    await TestBed.configureTestingModule({
      imports: [EventView],
      providers: [
        { provide: EventsService, useValue: eventsService },
        { provide: SnackbarService, useValue: snackbar },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ externalId: 'evt-1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventView);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  }

  beforeEach(() => {
    eventsService = {
      getEvent: vi.fn(() => of(event)),
      getEventTicketTypes: vi.fn(() => of([ticketType()])),
      reserveEventTickets: vi.fn(),
    };
    snackbar = { showError: vi.fn() };
  });

  it('should create and load event data on init', async () => {
    await setup();
    component.ngOnInit();

    expect(eventsService.getEvent).toHaveBeenCalledWith('evt-1');
    expect(eventsService.getEventTicketTypes).toHaveBeenCalledWith('evt-1');
    expect(component.event()).toEqual(event);
    expect(component.ticketTypes()).toEqual([ticketType()]);
    expect(component.loading()).toBe(false);
  });

  it('should set error state when loading fails', async () => {
    eventsService.getEvent.mockReturnValue(throwError(() => new Error('boom')));
    await setup();
    component.ngOnInit();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should increment ticket quantity up to maxQty', async () => {
    await setup();
    component.ngOnInit();

    component.increment(ticketType());
    expect(component.qty(1)).toBe(1);
    component.increment(ticketType());
    expect(component.qty(1)).toBe(2);
    component.increment(ticketType());
    expect(component.qty(1)).toBe(2);
  });

  it('should decrement ticket quantity and remove entry at zero', async () => {
    await setup();
    component.ngOnInit();
    component.increment(ticketType());

    component.decrement(1);
    expect(component.qty(1)).toBe(0);
    component.decrement(1);
    expect(component.qty(1)).toBe(0);
  });

  it('should compute totalTickets and totalPrice from selections', async () => {
    await setup();
    component.ngOnInit();

    component.increment(ticketType());
    component.increment(ticketType());

    expect(component.totalTickets()).toBe(2);
    expect(component.totalPrice()).toBe(20);
  });

  it('should compute maxQty as the min of availableQuantity and maxTicketsPerOrder', async () => {
    await setup();
    expect(component.maxQty(ticketType({ availableQuantity: 5, maxTicketsPerOrder: 2 }))).toBe(2);
    expect(component.maxQty(ticketType({ availableQuantity: 1, maxTicketsPerOrder: 5 }))).toBe(1);
  });

  it('should not reserve tickets when nothing is selected', async () => {
    await setup();
    component.ngOnInit();

    component.buyTickets();

    expect(eventsService.reserveEventTickets).not.toHaveBeenCalled();
  });

  it('should reserve tickets and navigate to checkout on success', async () => {
    eventsService.reserveEventTickets.mockReturnValue(
      of({
        expiresAt: '2026-01-01T00:00:00Z',
        reservationId: 'res-1',
        totalAmount: 10,
        externalEventId: 'evt-1',
      }),
    );
    await setup();
    component.ngOnInit();
    component.increment(ticketType());

    component.buyTickets();

    expect(eventsService.reserveEventTickets).toHaveBeenCalledWith('evt-1', {
      itemRequests: [{ ticketTypeId: 1, quantity: 1 }],
    });
    expect(router.navigate).toHaveBeenCalledWith(['/events', 'evt-1', 'checkout', 'res-1']);
  });

  it('should show an error with a retry action when reservation fails', async () => {
    eventsService.reserveEventTickets.mockReturnValue(
      throwError(() => ({ error: { detail: 'Sold out' } })),
    );
    await setup();
    component.ngOnInit();
    component.increment(ticketType());

    component.buyTickets();

    expect(snackbar.showError).toHaveBeenCalledWith(
      'Sold out',
      expect.objectContaining({ label: 'Retry' }),
    );
    expect(component.loading()).toBe(false);
  });

  it('should fall back to a generic error message when reservation error has no detail', async () => {
    eventsService.reserveEventTickets.mockReturnValue(throwError(() => ({})));
    await setup();
    component.ngOnInit();
    component.increment(ticketType());

    component.buyTickets();

    expect(snackbar.showError).toHaveBeenCalledWith(
      'Could not reserve tickets. Please try again.',
      expect.anything(),
    );
  });
});
