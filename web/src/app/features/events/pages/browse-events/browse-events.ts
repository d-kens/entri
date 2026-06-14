import { Component, signal, computed, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged, skip, switchMap, catchError, EMPTY } from 'rxjs';
import { EventsService } from '../../services/events-service';
import { CategoryResponse, EventFilter, EventResponse } from '@core/models/event.models';
import { PageResponse } from '@core/models/common.model';

const CATEGORY_STYLES: Record<string, { gradient: string; emoji: string }> = {
  Music:  { gradient: 'linear-gradient(135deg, #2D1B4E 0%, #7C3AED 100%)', emoji: '🎵' },
  Tech:   { gradient: 'linear-gradient(135deg, #0D3B5E 0%, #0EA5E9 100%)', emoji: '💻' },
  Comedy: { gradient: 'linear-gradient(135deg, #7C2D12 0%, #EA580C 100%)', emoji: '😂' },
  Sports: { gradient: 'linear-gradient(135deg, #064E3B 0%, #10B981 100%)', emoji: '🏃' },
  Food:   { gradient: 'linear-gradient(135deg, #713F12 0%, #F59E0B 100%)', emoji: '🍷' },
  Arts:   { gradient: 'linear-gradient(135deg, #0E4D5C 0%, #06B6D4 100%)', emoji: '🎪' },
};
const DEFAULT_STYLE = { gradient: 'linear-gradient(135deg, #1E293B 0%, #475569 100%)', emoji: '🎟️' };

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

  categories = signal<CategoryResponse[]>([]);
  activeCategoryId = signal<number | null>(null);
  searchQuery = signal('');
  startFromDate = signal('');
  startToDate = signal('');
  currentPage = signal(0);
  events = signal<EventResponse[]>([]);
  pageInfo = signal<Omit<PageResponse<EventResponse>, 'content'> | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  featuredEvent = computed(() => this.events()[0] ?? null);
  gridEvents    = computed(() => this.events().slice(1));

  private readonly trigger$ = new Subject<{ page: number; searchTerm: string; categoryId?: number; startFrom?: string; startTo?: string }>();

  constructor() {
    this.eventsService.getCategories().subscribe({
      next: cats => this.categories.set(cats),
    });

    this.trigger$.pipe(
      switchMap(({ page, searchTerm, categoryId, startFrom, startTo }) => {
        this.loading.set(true);
        const filter: EventFilter = { page, size: this.pageSize };
        if (searchTerm) filter.searchTerm = searchTerm;
        if (categoryId) filter.categoryId = categoryId;
        if (startFrom) filter.startFrom = startFrom;
        if (startTo)   filter.startTo   = startTo;
        return this.eventsService.getEvents(filter).pipe(
          catchError(() => {
            this.error.set('Failed to load events. Please try again.');
            this.loading.set(false);
            return EMPTY;
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(({ content, ...info }) => {
      this.events.set(content);
      this.pageInfo.set(info);
      this.loading.set(false);
      this.error.set(null);
    });

    this.trigger$.next({ page: 0, searchTerm: '' });

    toObservable(this.searchQuery).pipe(
      skip(1),
      debounceTime(400),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(q => {
      this.currentPage.set(0);
      this.trigger$.next({ page: 0, searchTerm: q, categoryId: this.activeCategoryId() ?? undefined });
    });
  }

  private fire(page: number): void {
    this.trigger$.next({
      page,
      searchTerm: this.searchQuery(),
      categoryId: this.activeCategoryId() ?? undefined,
      startFrom: this.startFromDate() ? `${this.startFromDate()}T00:00:00.000Z` : undefined,
      startTo:   this.startToDate()   ? `${this.startToDate()}T23:59:59.999Z`   : undefined,
    });
  }

  retry(): void { this.fire(this.currentPage()); }

  onSearch(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  setCategory(id: number | null): void {
    this.activeCategoryId.set(id);
    this.currentPage.set(0);
    this.fire(0);
  }

  onStartFromChange(event: Event): void {
    this.startFromDate.set((event.target as HTMLInputElement).value);
    this.currentPage.set(0);
    this.fire(0);
  }

  onStartToChange(event: Event): void {
    this.startToDate.set((event.target as HTMLInputElement).value);
    this.currentPage.set(0);
    this.fire(0);
  }

  clearDateFilter(): void {
    this.startFromDate.set('');
    this.startToDate.set('');
    this.currentPage.set(0);
    this.fire(0);
  }

  hasDateFilter(): boolean {
    return !!(this.startFromDate() || this.startToDate());
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.fire(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  getStyle(categoryName: string) {
    return CATEGORY_STYLES[categoryName] ?? DEFAULT_STYLE;
  }

  coverStyle(event: EventResponse): string {
    if (event.bannerUrl) return `url(${event.bannerUrl}) center / cover no-repeat`;
    return this.getStyle(event.categoryName).gradient;
  }

  proximityLabel(dateStr: string): string | null {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const target = new Date(dateStr);
    target.setHours(0, 0, 0, 0);
    const diff = Math.round((target.getTime() - today.getTime()) / 86_400_000);

    if (diff < 0) return null;
    if (diff === 0) return 'Today';
    if (diff === 1) return 'Tomorrow';

    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    if (diff <= 6) return `This ${days[target.getDay()]}`;
    if (diff <= 13) return `Next ${days[target.getDay()]}`;

    const evMonth = target.getMonth();
    const nowMonth = today.getMonth();
    const nowYear = today.getFullYear();
    const isNextMonth = (evMonth === (nowMonth + 1) % 12) &&
      (evMonth === 0 ? target.getFullYear() === nowYear + 1 : target.getFullYear() === nowYear);
    if (isNextMonth) return 'Next Month';

    return null;
  }

  isUrgent(dateStr: string): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const target = new Date(dateStr);
    target.setHours(0, 0, 0, 0);
    return Math.round((target.getTime() - today.getTime()) / 86_400_000) <= 1;
  }
}
