import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNativeDateAdapter } from '@angular/material/core';
import { of } from 'rxjs';
import { environment } from 'environments/environment';

import { CreateEvent } from './create-event';
import { SnackbarService } from '@shared/services/snackbar-service';
import { FileService } from '@shared/services/file-service';
import { EventDetailsFormData } from '../event-details-form/event-details-form';
import { CategoryResponse, EventResponse } from '@features/events/models/event.models';
import { combineDateTime } from '@features/events/utils/date-time.utils';

describe('CreateEvent', () => {
  let component: CreateEvent;
  let fixture: ComponentFixture<CreateEvent>;
  let httpMock: HttpTestingController;
  let snackbar: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let fileService: { upload: ReturnType<typeof vi.fn> };

  const categories: CategoryResponse[] = [{ id: 1, name: 'Music', description: '' }];

  const formData: EventDetailsFormData = {
    title: 'Concert',
    description: 'Great show',
    categoryId: 1,
    currency: 'KES',
    bannerUrl: 'https://cdn.example.com/existing.png',
    bannerFile: null,
    venueName: 'Arena',
    venueCity: 'Nairobi',
    venueCountry: 'Kenya',
    startDate: new Date('2026-06-01T00:00:00Z'),
    startTime: '10:00',
    endDate: new Date('2026-06-01T00:00:00Z'),
    endTime: '12:00',
  };

  const createdEvent: EventResponse = {
    externalId: 'evt-1',
    title: formData.title,
    description: formData.description,
    categoryName: 'Music',
    categoryId: 1,
    currency: formData.currency,
    venueName: formData.venueName,
    venueCity: formData.venueCity,
    venueCountry: formData.venueCountry,
    startTime: '2026-06-01T10:00:00.000Z',
    endTime: '2026-06-01T12:00:00.000Z',
    bannerUrl: formData.bannerUrl,
    status: 'DRAFT',
    dateCreated: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(async () => {
    snackbar = { showSuccess: vi.fn(), showError: vi.fn() };
    fileService = { upload: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CreateEvent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNativeDateAdapter(),
        { provide: SnackbarService, useValue: snackbar },
        { provide: FileService, useValue: fileService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateEvent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushCategories() {
    fixture.detectChanges();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/categories`);
    req.flush(categories);
  }

  it('should create', () => {
    flushCategories();
    expect(component).toBeTruthy();
  });

  it('should load categories on init', () => {
    flushCategories();
    expect(component.categories()).toEqual(categories);
  });

  it('should create the event with combined start/end times and navigate on success', async () => {
    flushCategories();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const promise = component.onSubmitted(formData);
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events`);
    expect(req.request.body.startTime).toBe(
      combineDateTime(formData.startDate, formData.startTime),
    );
    expect(req.request.body.endTime).toBe(combineDateTime(formData.endDate, formData.endTime));
    expect(req.request.body.bannerUrl).toBe(formData.bannerUrl);
    req.flush(createdEvent);
    await promise;

    expect(fileService.upload).not.toHaveBeenCalled();
    expect(snackbar.showSuccess).toHaveBeenCalledWith('Event created successfully!');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/events', 'evt-1']);
    expect(component.loading()).toBe(false);
  });

  it('should upload the banner file before creating the event when one is provided', async () => {
    flushCategories();
    const file = new File(['data'], 'banner.png', { type: 'image/png' });
    fileService.upload.mockReturnValue(of('https://cdn.example.com/uploaded.png'));

    const promise = component.onSubmitted({ ...formData, bannerFile: file, bannerUrl: '' });
    await Promise.resolve();
    await Promise.resolve();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events`);
    expect(req.request.body.bannerUrl).toBe('https://cdn.example.com/uploaded.png');
    req.flush(createdEvent);
    await promise;

    expect(fileService.upload).toHaveBeenCalledWith(file);
  });

  it('should show an error message when event creation fails', async () => {
    flushCategories();
    const promise = component.onSubmitted(formData);
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/events`);
    req.flush({ detail: 'Title already used' }, { status: 400, statusText: 'Bad Request' });
    await promise;

    expect(snackbar.showError).toHaveBeenCalledWith('Title already used');
    expect(component.loading()).toBe(false);
  });
});
