import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { PageEvent } from '@angular/material/paginator';
import { of, throwError } from 'rxjs';

import { Tickets } from './tickets';
import { EventsService } from '@features/events/services/events-service';
import { PageResponse } from '@shared/models/common.model';
import { TicketResponse } from '@features/events/models/event.models';

function buildPage(
  overrides: Partial<PageResponse<TicketResponse>> = {},
): PageResponse<TicketResponse> {
  return {
    content: [],
    number: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    ...overrides,
  };
}

describe('Tickets', () => {
  let component: Tickets;
  let fixture: ComponentFixture<Tickets>;
  let eventsService: { getEventTickets: ReturnType<typeof vi.fn> };

  function setup() {
    return TestBed.configureTestingModule({
      imports: [Tickets],
      providers: [
        { provide: EventsService, useValue: eventsService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ eventExternalId: 'evt-1' }) } },
        },
      ],
    }).compileComponents();
  }

  beforeEach(() => {
    eventsService = { getEventTickets: vi.fn() };
  });

  it('should load the first page of tickets on init', async () => {
    eventsService.getEventTickets.mockReturnValue(of(buildPage({ totalElements: 3 })));
    await setup();
    fixture = TestBed.createComponent(Tickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(eventsService.getEventTickets).toHaveBeenCalledWith('evt-1', 0, 10);
    expect(component.page().totalElements).toBe(3);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe(false);
  });

  it('should set the error state when loading fails', async () => {
    eventsService.getEventTickets.mockReturnValue(throwError(() => new Error('boom')));
    await setup();
    fixture = TestBed.createComponent(Tickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should reload with the requested page on page change', async () => {
    eventsService.getEventTickets.mockReturnValue(of(buildPage()));
    await setup();
    fixture = TestBed.createComponent(Tickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const pageEvent = { pageIndex: 2, pageSize: 25 } as PageEvent;
    component.onPage(pageEvent);

    expect(eventsService.getEventTickets).toHaveBeenCalledWith('evt-1', 2, 25);
  });

  it('should retry with the current page and size', async () => {
    eventsService.getEventTickets.mockReturnValue(of(buildPage({ number: 1, size: 20 })));
    await setup();
    fixture = TestBed.createComponent(Tickets);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    component.retry();

    expect(eventsService.getEventTickets).toHaveBeenLastCalledWith('evt-1', 1, 20);
  });
});
