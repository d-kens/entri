import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { BrowseEvents } from './browse-events';
import { EventsService } from '../services/events-service';
import { CategoryResponse, EventResponse } from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';

const category = (id: number, name: string): CategoryResponse => ({ id, name, description: '' });

const event = (overrides: Partial<EventResponse> = {}): EventResponse => ({
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
  ...overrides,
});

const page = (content: EventResponse[]): PageResponse<EventResponse> => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  number: 0,
  size: 12,
  first: true,
  last: true,
});

describe('BrowseEvents', () => {
  let component: BrowseEvents;
  let fixture: ComponentFixture<BrowseEvents>;
  let eventsService: {
    getCategories: ReturnType<typeof vi.fn>;
    getEvents: ReturnType<typeof vi.fn>;
  };

  async function setup() {
    await TestBed.configureTestingModule({
      imports: [BrowseEvents],
      providers: [provideRouter([]), { provide: EventsService, useValue: eventsService }],
    }).compileComponents();

    fixture = TestBed.createComponent(BrowseEvents);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  beforeEach(() => {
    eventsService = {
      getCategories: vi.fn(() => of([category(1, 'Music'), category(2, 'Sports')])),
      getEvents: vi.fn(() => of(page([event()]))),
    };
  });

  it('should create', async () => {
    await setup();
    expect(component).toBeTruthy();
  });

  it('should load categories on init', async () => {
    await setup();
    expect(eventsService.getCategories).toHaveBeenCalled();
    expect(component.categories()).toEqual([category(1, 'Music'), category(2, 'Sports')]);
  });

  it('should load the first page of events on init', async () => {
    await setup();
    expect(eventsService.getEvents).toHaveBeenCalledWith({
      page: 0,
      size: component.pageSize,
      sortDirection: 'ASC',
    });
    expect(component.events()).toEqual([event()]);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();
  });

  it('should set an error and stop loading when fetching events fails', async () => {
    eventsService.getEvents.mockReturnValue(throwError(() => new Error('boom')));
    await setup();

    expect(component.error()).toBe('Failed to load events. Please try again.');
    expect(component.loading()).toBe(false);
  });

  it('should debounce search input and refetch with the search term', async () => {
    await setup();
    eventsService.getEvents.mockClear();

    component.onSearch({ target: { value: 'jazz' } } as unknown as Event);
    expect(component.searchQuery()).toBe('jazz');
    expect(eventsService.getEvents).not.toHaveBeenCalled();

    await new Promise((resolve) => setTimeout(resolve, 450));

    expect(eventsService.getEvents).toHaveBeenCalledWith({
      page: 0,
      size: component.pageSize,
      sortDirection: 'ASC',
      searchTerm: 'jazz',
    });
  }, 2000);

  it('should reset page and refetch when category changes', async () => {
    await setup();
    eventsService.getEvents.mockClear();

    component.setCategory(2);

    expect(component.activeCategoryId()).toBe(2);
    expect(component.currentPage()).toBe(0);
    expect(eventsService.getEvents).toHaveBeenCalledWith({
      page: 0,
      size: component.pageSize,
      sortDirection: 'ASC',
      categoryId: 2,
    });
  });

  it('should refetch with new sort direction', async () => {
    await setup();
    eventsService.getEvents.mockClear();

    component.setSortDirection('DESC');

    expect(component.sortDirection()).toBe('DESC');
    expect(eventsService.getEvents).toHaveBeenCalledWith({
      page: 0,
      size: component.pageSize,
      sortDirection: 'DESC',
    });
  });

  it('should clear filters and refetch', async () => {
    await setup();
    component.searchQuery.set('jazz');
    component.activeCategoryId.set(2);
    component.currentPage.set(3);
    eventsService.getEvents.mockClear();

    component.clearFilters();

    expect(component.searchQuery()).toBe('');
    expect(component.activeCategoryId()).toBeNull();
    expect(component.currentPage()).toBe(0);
    expect(eventsService.getEvents).toHaveBeenCalledWith({
      page: 0,
      size: component.pageSize,
      sortDirection: 'ASC',
    });
  });

  it('should retry using the current page', async () => {
    await setup();
    component.currentPage.set(2);
    eventsService.getEvents.mockClear();

    component.retry();

    expect(eventsService.getEvents).toHaveBeenCalledWith(expect.objectContaining({ page: 2 }));
  });

  it('should compute hasFilters based on search query and category', async () => {
    await setup();
    expect(component.hasFilters()).toBe(false);
    component.searchQuery.set('jazz');
    expect(component.hasFilters()).toBe(true);
  });

  it('should format day of week, day number, and month name', async () => {
    await setup();
    const dateStr = '2026-03-05T00:00:00Z';
    expect(component.dayOfWeek(dateStr)).toBe(
      new Date(dateStr).toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase(),
    );
    expect(component.dayNum(dateStr)).toBe(new Date(dateStr).getDate().toString().padStart(2, '0'));
    expect(component.monthName(dateStr)).toBe(
      new Date(dateStr).toLocaleDateString('en-US', { month: 'short' }).toUpperCase(),
    );
  });
});
