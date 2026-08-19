import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { EventsService } from '@features/events/services/events-service';
import { EventResponse } from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';
import { SnackbarService } from '@shared/services/snackbar-service';

@Component({
  selector: 'app-events-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    TitleCasePipe,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatMenuModule,
  ],
  templateUrl: './events-list.html',
  styleUrl: './events-list.css',
})
export class EventsList implements OnInit {
  private eventsService = inject(EventsService);
  private router = inject(Router);
  private snackbarService = inject(SnackbarService);

  loading = signal(true);
  error = signal(false);
  page = signal<PageResponse<EventResponse>>({
    content: [],
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  readonly columns = [
    'title',
    'venue',
    'startTime',
    'endTime',
    'currency',
    'dateCreated',
    'status',
    'actions',
  ];

  ngOnInit(): void {
    this.load(0, 10);
  }

  onPage(event: PageEvent): void {
    this.load(event.pageIndex, event.pageSize);
  }

  publishEvent(externalId: string): void {
    this.eventsService.publishEvent(externalId).subscribe({
      next: () => {
        this.snackbarService.showSuccess('Event published');
        this.load(this.page().pageNumber, this.page().pageSize);
      },
      error: (err: Error) => {
        this.snackbarService.showError(err.message);
      },
    });
  }

  cancelEvent(externalId: string): void {
    this.eventsService.cancelEvent(externalId).subscribe({
      next: () => {
        this.snackbarService.showSuccess('Event cancelled');
        this.load(this.page().pageNumber, this.page().pageSize);
      },
      error: (err: Error) => {
        this.snackbarService.showError(err.message);
      },
    });
  }

  goToEvent(id: string): void {
    this.router.navigate(['/dashboard/events', id]);
  }

  retry(): void {
    this.load(this.page().pageNumber, this.page().pageSize);
  }

  private load(pageNumber: number, pageSize: number): void {
    this.loading.set(true);
    this.error.set(false);
    this.eventsService.getManagedEvents({ page: pageNumber, size: pageSize }).subscribe({
      next: (res) => {
        this.page.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
