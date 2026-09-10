import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { environment } from 'environments/environment';

import { EditEvent } from './edit-event';
import { SnackbarService } from '@shared/services/snackbar-service';
import { FileService } from '@shared/services/file-service';
import { EventDetailsFormData } from '@features/events/event-details-form/event-details-form';
import { CategoryResponse, EventResponse } from '@features/events/models/event.models';

describe('EditEvent', () => {
  let component: EditEvent;
  let fixture: ComponentFixture<EditEvent>;
  let httpMock: HttpTestingController;
  let snackbar: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let fileService: { upload: ReturnType<typeof vi.fn> };

  const categories: CategoryResponse[] = [{ id: 1, name: 'Music', description: '' }];

  const existingEvent: EventResponse = {
    externalId: 'evt-1',
    title: 'Concert',
    description: 'Great show',
    categoryName: 'Music',
    categoryId: 1,
    currency: 'KES',
    venueName: 'Arena',
    venueCity: 'Nairobi',
    venueCountry: 'Kenya',
    startTime: '2026-06-01T10:00:00.000Z',
    endTime: '2026-06-01T12:00:00.000Z',
    bannerUrl: 'https://cdn.example.com/existing.png',
    status: 'DRAFT',
    dateCreated: '2026-01-01T00:00:00.000Z',
  };

  const updatedFormData: EventDetailsFormData = {
    title: 'Updated Concert',
    description: 'Even better show',
    categoryId: 1,
    currency: 'KES',
    bannerUrl: existingEvent.bannerUrl,
    bannerFile: null,
    venueName: 'Arena',
    venueCity: 'Nairobi',
    venueCountry: 'Kenya',
    startDate: new Date('2026-06-02T00:00:00Z'),
    startTime: '10:00',
    endDate: new Date('2026-06-02T00:00:00Z'),
    endTime: '12:00',
  };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };
    fileService = { upload: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EditEvent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: SnackbarService, useValue: snackbar },
        { provide: FileService, useValue: fileService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ eventExternalId: 'evt-1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditEvent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushInitialLoad() {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush(categories);
    httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`).flush(existingEvent);
  }

  it('should create', () => {
    flushInitialLoad();
    expect(component).toBeTruthy();
  });

  it('should read the event external id from the route', () => {
    expect(component.eventExternalId()).toBe('evt-1');
  });

  it('should load the event and populate the form data on init', () => {
    flushInitialLoad();
    expect(component.loading()).toBe(false);
    expect(component.hasError()).toBe(false);
    expect(component.eventDetailsFormData()?.title).toBe('Concert');
    expect(component.eventDetailsFormData()?.startTime).toMatch(/^\d{2}:\d{2}$/);
    expect(component.eventDetailsFormData()?.startDate).toEqual(new Date(existingEvent.startTime));
  });

  it('should set hasError when loading the event fails', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush(categories);
    httpMock
      .expectOne(`${environment.apiBaseUrl}/events/evt-1`)
      .flush('not found', { status: 404, statusText: 'Not Found' });

    expect(component.hasError()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should update the event and navigate on success', async () => {
    flushInitialLoad();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const promise = component.onSubmitted(updatedFormData);
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.title).toBe('Updated Concert');
    req.flush({ ...existingEvent, title: 'Updated Concert' });
    await promise;

    expect(snackbar.showSuccess).toHaveBeenCalledWith('Event updated successfully!');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
  });

  it('should show an error message when updating the event fails', async () => {
    flushInitialLoad();
    const promise = component.onSubmitted(updatedFormData);
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`);
    req.flush(
      { detail: 'Cannot update a cancelled event' },
      { status: 409, statusText: 'Conflict' },
    );
    await promise;

    expect(snackbar.showError).toHaveBeenCalledWith('Cannot update a cancelled event');
  });

  it('should reload the event data when retried', () => {
    flushInitialLoad();
    component.loadEventData();
    httpMock.expectOne(`${environment.apiBaseUrl}/events/evt-1`).flush(existingEvent);
  });
});
