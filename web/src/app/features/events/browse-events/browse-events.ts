import { Component, signal, computed, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  skip,
  switchMap,
  catchError,
  EMPTY,
} from 'rxjs';
import { EventsService } from '../services/events-service';
import { CategoryResponse, EventFilter, EventResponse } from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';

export type DateRange = 'ANYTIME' | 'TODAY' | 'WEEKEND' | 'MONTH';

const CATEGORY_ICONS: Record<string, string> = {
  music: 'music_note',
  sport: 'sports_soccer',
  art: 'theater_comedy',
  theatre: 'theater_comedy',
  theater: 'theater_comedy',
  food: 'restaurant',
  drink: 'restaurant',
  business: 'business_center',
  networking: 'business_center',
  tech: 'memory',
  health: 'spa',
  wellness: 'spa',
  education: 'school',
  community: 'volunteer_activism',
  cause: 'volunteer_activism',
};
const DEFAULT_CATEGORY_ICON = 'celebration';

@Component({
  selector: 'app-events-listing',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './browse-events.html',
  styleUrl: './browse-events.css',
})
export class BrowseEvents {
  private eventsService = inject(EventsService);
  private destroyRef = inject(DestroyRef);

  readonly pageSize = 12;
  readonly dateRanges: DateRange[] = ['ANYTIME', 'TODAY', 'WEEKEND', 'MONTH'];
  readonly dateRangeLabels: Record<DateRange, string> = {
    ANYTIME: 'Anytime',
    TODAY: 'Today',
    WEEKEND: 'This weekend',
    MONTH: 'This month',
  };

  // The soonest upcoming event, fetched once and independent of active filters -
  // it anchors the hero backdrop so that filtering the grid below never makes
  // the page's first impression flicker.
  heroEvent = signal<EventResponse | null>(null);

  categories = signal<CategoryResponse[]>([]);
  activeCategoryId = signal<number | null>(null);
  dateRange = signal<DateRange>('ANYTIME');
  searchQuery = signal('');
  currentPage = signal(0);
  events = signal<EventResponse[]>([]);
  pageInfo = signal<Omit<PageResponse<EventResponse>, 'content'> | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  totalEvents = computed(() => this.pageInfo()?.totalElements ?? 0);
  hasFilters = computed(
    () =>
      !!this.searchQuery() || this.activeCategoryId() !== null || this.dateRange() !== 'ANYTIME',
  );

  resultsTitle = computed(() => {
    if (this.activeCategoryId() !== null) {
      const cat = this.categories().find((c) => c.id === this.activeCategoryId());
      return cat?.name ?? 'Events';
    }
    if (this.dateRange() !== 'ANYTIME') return this.dateRangeLabels[this.dateRange()];
    return 'All Events';
  });

  private readonly trigger$ = new Subject<{
    page: number;
    searchTerm: string;
    categoryId?: number;
    dateRange: DateRange;
  }>();

  constructor() {
    this.eventsService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
    });

    this.eventsService.getEvents({ page: 0, size: 1 }).subscribe({
      next: ({ content }) => this.heroEvent.set(content[0] ?? null),
    });

    this.trigger$
      .pipe(
        switchMap(({ page, searchTerm, categoryId, dateRange }) => {
          this.loading.set(true);
          const filter: EventFilter = { page, size: this.pageSize };
          if (searchTerm) filter.searchTerm = searchTerm;
          if (categoryId) filter.categoryId = categoryId;
          if (dateRange !== 'ANYTIME') Object.assign(filter, this.dateRangeBounds(dateRange));
          return this.eventsService.getEvents(filter).pipe(
            catchError(() => {
              this.error.set('Failed to load events. Please try again.');
              this.loading.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ content, ...info }) => {
        this.events.set(content);
        this.pageInfo.set(info);
        this.loading.set(false);
        this.error.set(null);
      });

    this.trigger$.next({ page: 0, searchTerm: '', dateRange: 'ANYTIME' });

    toObservable(this.searchQuery)
      .pipe(skip(1), debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((q) => {
        this.currentPage.set(0);
        this.fire(0, q);
      });
  }

  private dateRangeBounds(range: DateRange): { startFrom: string; startTo: string } {
    const now = new Date();

    if (range === 'TODAY') {
      const start = new Date(now);
      start.setHours(0, 0, 0, 0);
      const end = new Date(start);
      end.setDate(end.getDate() + 1);
      return { startFrom: start.toISOString(), startTo: end.toISOString() };
    }

    if (range === 'WEEKEND') {
      const daysUntilSaturday = (6 - now.getDay() + 7) % 7;
      const start = new Date(now);
      start.setHours(0, 0, 0, 0);
      start.setDate(start.getDate() + daysUntilSaturday);
      const end = new Date(start);
      end.setDate(end.getDate() + 2);
      return { startFrom: start.toISOString(), startTo: end.toISOString() };
    }

    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    return { startFrom: start.toISOString(), startTo: end.toISOString() };
  }

  private fire(page: number, searchTerm = this.searchQuery()): void {
    this.trigger$.next({
      page,
      searchTerm,
      categoryId: this.activeCategoryId() ?? undefined,
      dateRange: this.dateRange(),
    });
  }

  retry(): void {
    this.fire(this.currentPage());
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.activeCategoryId.set(null);
    this.dateRange.set('ANYTIME');
    this.currentPage.set(0);
    this.fire(0);
  }

  onSearch(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  setCategory(id: number | null): void {
    this.activeCategoryId.set(id);
    this.currentPage.set(0);
    this.fire(0);
  }

  setDateRange(range: DateRange): void {
    this.dateRange.set(range);
    this.currentPage.set(0);
    this.fire(0);
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.fire(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  categoryIcon(name: string): string {
    const key = name.toLowerCase();
    const match = Object.entries(CATEGORY_ICONS).find(([keyword]) => key.includes(keyword));
    return match ? match[1] : DEFAULT_CATEGORY_ICON;
  }

  dayOfWeek(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase();
  }

  dayNum(dateStr: string): string {
    return new Date(dateStr).getDate().toString().padStart(2, '0');
  }

  monthName(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' }).toUpperCase();
  }
}
