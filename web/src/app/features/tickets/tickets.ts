import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { EventsService } from '@features/events/services/events-service';
import { TicketResponse } from '@features/events/models/event.models';
import { PageResponse } from '@shared/models/common.model';
import { DataTable } from '@shared/components/data-table/data-table';

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [DatePipe, MatIconModule, MatTableModule, MatPaginatorModule, DataTable],
  templateUrl: './tickets.html',
  styleUrl: './tickets.css',
})
export class Tickets implements OnInit {
  private eventsService = inject(EventsService);

  loading = signal(true);
  error = signal(false);
  page = signal<PageResponse<TicketResponse>>({
    content: [],
    number: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  readonly columns = ['holder', 'event', 'ticketType', 'status', 'checkedInAt'];

  ngOnInit(): void {
    this.load(0, 10);
  }

  onPage(event: PageEvent): void {
    this.load(event.pageIndex, event.pageSize);
  }

  retry(): void {
    this.load(this.page().number, this.page().size);
  }

  private load(pageNumber: number, pageSize: number): void {
    this.loading.set(true);
    this.error.set(false);
    this.eventsService.getManagedTickets(pageNumber, pageSize).subscribe({
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
