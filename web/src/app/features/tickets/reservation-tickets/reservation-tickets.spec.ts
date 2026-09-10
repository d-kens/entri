import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ReservationTickets } from './reservation-tickets';
import { EventsService } from '@features/events/services/events-service';
import { TicketResponse } from '@features/events/models/event.models';

function buildTicket(overrides: Partial<TicketResponse> = {}): TicketResponse {
  return {
    ticketExternalId: 't1',
    ticketCode: '123456789012',
    reservationExternalId: 'res-1',
    eventExternalId: 'evt-1',
    eventTitle: 'Music Night',
    eventStartTime: '2026-01-01T18:00:00Z',
    eventEndTime: '2026-01-01T22:00:00Z',
    venueName: 'Arena',
    venueCity: 'Nairobi',
    currency: 'KES',
    ticketTypeName: 'VIP',
    ticketPrice: 1000,
    status: 'VALID',
    holderFirstName: 'Jane',
    holderLastName: 'Doe',
    ...overrides,
  };
}

describe('ReservationTickets', () => {
  let component: ReservationTickets;
  let fixture: ComponentFixture<ReservationTickets>;
  let eventsService: { getTicketsByReservation: ReturnType<typeof vi.fn> };

  function setup() {
    return TestBed.configureTestingModule({
      imports: [ReservationTickets],
      providers: [
        { provide: EventsService, useValue: eventsService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ reservationId: 'res-1' }) } },
        },
      ],
    }).compileComponents();
  }

  beforeEach(() => {
    eventsService = { getTicketsByReservation: vi.fn() };
  });

  it('should load tickets immediately when they already exist', async () => {
    eventsService.getTicketsByReservation.mockReturnValue(of([buildTicket()]));
    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(eventsService.getTicketsByReservation).toHaveBeenCalledWith('res-1');
    expect(component.tickets().length).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should set the error state when the initial load fails', async () => {
    eventsService.getTicketsByReservation.mockReturnValue(throwError(() => new Error('boom')));
    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should poll until tickets become available', async () => {
    vi.useFakeTimers();
    eventsService.getTicketsByReservation
      .mockReturnValueOnce(of([]))
      .mockReturnValueOnce(of([]))
      .mockReturnValueOnce(of([buildTicket()]));

    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await Promise.resolve();

    expect(component.loading()).toBe(true);

    await vi.advanceTimersByTimeAsync(3000);
    expect(component.tickets().length).toBe(0);
    expect(component.loading()).toBe(true);

    await vi.advanceTimersByTimeAsync(3000);

    expect(component.tickets().length).toBe(1);
    expect(component.loading()).toBe(false);
    expect(component.processingTimeout()).toBe(false);
    expect(eventsService.getTicketsByReservation).toHaveBeenCalledTimes(3);

    vi.useRealTimers();
  });

  it('should build the qr url from the ticket code', async () => {
    eventsService.getTicketsByReservation.mockReturnValue(of([buildTicket()]));
    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.qrUrl('ABC123')).toBe(
      'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=ABC123&qzone=1',
    );
  });

  it('should format the ticket code into groups', async () => {
    eventsService.getTicketsByReservation.mockReturnValue(of([buildTicket()]));
    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.formatCode('123456789012')).toBe('1234-5678-9012');
  });

  it('should join the holder first and last name', async () => {
    eventsService.getTicketsByReservation.mockReturnValue(of([buildTicket()]));
    await setup();
    fixture = TestBed.createComponent(ReservationTickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      component.holderName(buildTicket({ holderFirstName: 'Jane', holderLastName: 'Doe' })),
    ).toBe('Jane Doe');
    expect(component.holderName(buildTicket({ holderFirstName: 'Jane', holderLastName: '' }))).toBe(
      'Jane',
    );
  });
});
