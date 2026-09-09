import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { TicketView } from './ticket-view';
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

describe('TicketView', () => {
  let component: TicketView;
  let fixture: ComponentFixture<TicketView>;
  let eventsService: { getTicket: ReturnType<typeof vi.fn> };

  function setup() {
    return TestBed.configureTestingModule({
      imports: [TicketView],
      providers: [
        { provide: EventsService, useValue: eventsService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ externalId: 't1' }) } },
        },
      ],
    }).compileComponents();
  }

  beforeEach(() => {
    eventsService = { getTicket: vi.fn() };
  });

  it('should load the ticket and compute the back link', async () => {
    eventsService.getTicket.mockReturnValue(of(buildTicket()));
    await setup();
    fixture = TestBed.createComponent(TicketView);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(eventsService.getTicket).toHaveBeenCalledWith('t1');
    expect(component.ticket()).toEqual(buildTicket());
    expect(component.backLink()).toEqual(['/tickets', 'res-1']);
    expect(component.loading()).toBe(false);
  });

  it('should set the error state when the ticket cannot be loaded', async () => {
    eventsService.getTicket.mockReturnValue(throwError(() => new Error('not found')));
    await setup();
    fixture = TestBed.createComponent(TicketView);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
    expect(component.backLink()).toBeNull();
  });

  it('should build the larger qr url from the ticket code', async () => {
    eventsService.getTicket.mockReturnValue(of(buildTicket()));
    await setup();
    fixture = TestBed.createComponent(TicketView);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.qrUrl('ABC123')).toBe(
      'https://api.qrserver.com/v1/create-qr-code/?size=560x560&data=ABC123&qzone=1',
    );
  });

  it('should format the ticket code into groups', async () => {
    eventsService.getTicket.mockReturnValue(of(buildTicket()));
    await setup();
    fixture = TestBed.createComponent(TicketView);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.formatCode('123456789012')).toBe('1234-5678-9012');
  });

  it('should join the holder first and last name, omitting blanks', async () => {
    eventsService.getTicket.mockReturnValue(of(buildTicket()));
    await setup();
    fixture = TestBed.createComponent(TicketView);
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
