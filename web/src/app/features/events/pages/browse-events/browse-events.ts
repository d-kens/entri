import { Component, signal, computed, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged, skip, switchMap, catchError, EMPTY } from 'rxjs';
import { EventsService } from '../../services/events-service';
import { CategoryResponse, EventFilter, EventResponse } from '@core/models/event.models';
import { PageResponse } from '@core/models/common.model';

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
  currentPage = signal(0);
  events = signal<EventResponse[]>([]);
  pageInfo = signal<Omit<PageResponse<EventResponse>, 'content'> | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  totalEvents = computed(() => this.pageInfo()?.totalElements ?? 0);

  private readonly trigger$ = new Subject<{ page: number; searchTerm: string; categoryId?: number }>();

  constructor() {
    this.eventsService.getCategories().subscribe({
      next: cats => this.categories.set(cats),
    });

    this.trigger$.pipe(
      switchMap(({ page, searchTerm, categoryId }) => {
        this.loading.set(true);
        const filter: EventFilter = { page, size: this.pageSize };
        if (searchTerm) filter.searchTerm = searchTerm;
        if (categoryId) filter.categoryId = categoryId;
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

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.fire(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
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
