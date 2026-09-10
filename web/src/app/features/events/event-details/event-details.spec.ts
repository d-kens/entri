import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { EventDetails } from './event-details';
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
  status: 'DRAFT',
  dateCreated: '2025-12-01T00:00:00Z',
};

const ticketType: TicketTypeResponse = {
  id: 1,
  name: 'General',
  price: 10,
  quantity: 100,
  availableQuantity: 100,
  saleStatus: 'ON_SALE',
  availabilityStatus: 'AVAILABLE',
};

describe('EventDetails', () => {
  let component: EventDetails;
  let fixture: ComponentFixture<EventDetails>;
  let eventsService: {
    getEvent: ReturnType<typeof vi.fn>;
    getEventTicketTypes: ReturnType<typeof vi.fn>;
    deleteTicketType: ReturnType<typeof vi.fn>;
    publishEvent: ReturnType<typeof vi.fn>;
    cancelEvent: ReturnType<typeof vi.fn>;
    generateCheckInCode: ReturnType<typeof vi.fn>;
  };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let dialog: { open: ReturnType<typeof vi.fn> };
  let router: Router;

  async function setup() {
    await TestBed.configureTestingModule({
      imports: [EventDetails],
      providers: [
        { provide: EventsService, useValue: eventsService },
        { provide: SnackbarService, useValue: snackbarService },
        { provide: MatDialog, useValue: dialog },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ eventExternalId: 'evt-1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDetails);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  }

  beforeEach(() => {
    eventsService = {
      getEvent: vi.fn(() => of(event)),
      getEventTicketTypes: vi.fn(() => of([ticketType])),
      deleteTicketType: vi.fn(() => of(undefined)),
      publishEvent: vi.fn(() => of({ ...event, status: 'PUBLISHED' })),
      cancelEvent: vi.fn(() => of({ ...event, status: 'CANCELLED' })),
      generateCheckInCode: vi.fn(() =>
        of({ code: '123456', eventExternalId: 'evt-1', expiresAt: '2026-01-01T00:00:00Z' }),
      ),
    };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };
    dialog = { open: vi.fn() };
  });

  it('should create', async () => {
    await setup();
    expect(component).toBeTruthy();
  });

  it('should load the event and its ticket types on init', async () => {
    await setup();
    component.ngOnInit();

    expect(eventsService.getEvent).toHaveBeenCalledWith('evt-1');
    expect(eventsService.getEventTicketTypes).toHaveBeenCalledWith('evt-1');
    expect(component.event()).toEqual(event);
    expect(component.ticketTypes()).toEqual([ticketType]);
    expect(component.isLoading()).toBe(false);
  });

  it('should set hasError when loading fails', async () => {
    eventsService.getEvent.mockReturnValue(throwError(() => new Error('boom')));
    await setup();
    component.ngOnInit();

    expect(component.hasError()).toBe(true);
    expect(component.isLoading()).toBe(false);
  });

  it('should navigate to edit event', async () => {
    await setup();
    component.navigateToEditEvent('evt-1');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/events', 'evt-1', 'edit']);
  });

  it('should navigate to add ticket type', async () => {
    await setup();
    component.navigateToAddTicket('evt-1');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/events', 'evt-1', 'ticket-types']);
  });

  it('should navigate to edit ticket type', async () => {
    await setup();
    component.navigateToEditTicketType('evt-1', 5);
    expect(router.navigate).toHaveBeenCalledWith([
      '/dashboard/events',
      'evt-1',
      'ticket-types',
      5,
      'edit',
    ]);
  });

  it('should delete a ticket type and reload after confirming the dialog', async () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    await setup();
    const loadSpy = vi.spyOn(component, 'loadEvent');

    component.openDeleteTicketTypeDialog(1, 'General');

    expect(eventsService.deleteTicketType).toHaveBeenCalledWith(1);
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Ticket type deleted');
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should not delete a ticket type when the dialog is dismissed', async () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    await setup();

    component.openDeleteTicketTypeDialog(1, 'General');

    expect(eventsService.deleteTicketType).not.toHaveBeenCalled();
  });

  it('should show an error when deleting a ticket type fails', async () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    eventsService.deleteTicketType.mockReturnValue(throwError(() => new Error('fail')));
    await setup();

    component.openDeleteTicketTypeDialog(1, 'General');

    expect(snackbarService.showError).toHaveBeenCalledWith('Failed to delete ticket type');
  });

  it('should publish the event', async () => {
    await setup();
    component.publishEvent('evt-1');

    expect(component.event()).toEqual({ ...event, status: 'PUBLISHED' });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Event published');
  });

  it('should show an error when publishing fails', async () => {
    eventsService.publishEvent.mockReturnValue(throwError(() => new Error('Cannot publish')));
    await setup();

    component.publishEvent('evt-1');

    expect(snackbarService.showError).toHaveBeenCalledWith('Cannot publish');
  });

  it('should cancel the event', async () => {
    await setup();
    component.cancelEvent('evt-1');

    expect(component.event()).toEqual({ ...event, status: 'CANCELLED' });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Event cancelled');
  });

  it('should generate a check-in code and open the dialog', async () => {
    await setup();
    component.generateCheckInCode('evt-1');

    expect(dialog.open).toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({
        data: { code: '123456', eventExternalId: 'evt-1', expiresAt: '2026-01-01T00:00:00Z' },
      }),
    );
  });

  it('should show an error when generating a check-in code fails', async () => {
    eventsService.generateCheckInCode.mockReturnValue(throwError(() => new Error('nope')));
    await setup();

    component.generateCheckInCode('evt-1');

    expect(snackbarService.showError).toHaveBeenCalledWith('nope');
  });
});
