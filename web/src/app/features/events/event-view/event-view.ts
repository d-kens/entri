import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { EventsService } from '../services/events-service';
import { PageError } from '@shared/components/page-error/page-error';
import {
  EventResponse,
  TicketTypeResponse,
  EventTicketReservationItemRequest,
} from '@features/events/models/event.models';
import { SnackbarService } from '@shared/services/snackbar-service';

@Component({
  selector: 'app-event-view',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, MatIconModule, PageError],
  templateUrl: './event-view.html',
  styleUrl: './event-view.css',
})
export class EventView implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private snackbar = inject(SnackbarService);

  readonly externalId = this.route.snapshot.paramMap.get('externalId')!;

  event = signal<EventResponse | null>(null);
  ticketTypes = signal<TicketTypeResponse[]>([]);
  loading = signal(true);
  error = signal(false);

  selections = signal<Map<number, number>>(new Map());

  totalTickets = computed(() => {
    let n = 0;
    this.selections().forEach((qty) => (n += qty));
    return n;
  });

  totalPrice = computed(() => {
    let total = 0;
    this.selections().forEach((qty, id) => {
      const ticket = this.ticketTypes().find((t) => t.id === id);
      if (ticket) total += ticket.price * qty;
    });
    return total;
  });

  currency = computed(() => this.event()?.currency ?? '');

  ngOnInit(): void {
    this.loadEvent();
  }

  qty(ticketId: number): number {
    return this.selections().get(ticketId) ?? 0;
  }

  increment(ticket: TicketTypeResponse): void {
    if (this.qty(ticket.id) >= ticket.maxTicketsPerOrder) return;
    const next = new Map(this.selections());
    next.set(ticket.id, this.qty(ticket.id) + 1);
    this.selections.set(next);
  }

  decrement(ticketId: number): void {
    if (this.qty(ticketId) === 0) return;
    const next = new Map(this.selections());
    const updated = this.qty(ticketId) - 1;
    if (updated === 0) next.delete(ticketId);
    else next.set(ticketId, updated);
    this.selections.set(next);
  }

  loadEvent(): void {
    this.loading.set(true);
    this.error.set(false);

    forkJoin([
      this.eventsService.getEvent(this.externalId),
      this.eventsService.getEventTicketTypes(this.externalId),
    ]).subscribe({
      next: ([ev, ticketTypes]) => {
        this.event.set(ev);
        this.ticketTypes.set(ticketTypes);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  buyTickets(): void {
    if (this.totalTickets() === 0 || this.loading()) return;

    const itemRequests: EventTicketReservationItemRequest[] = Array.from(
      this.selections(),
      ([ticketTypeId, quantity]) => ({ ticketTypeId, quantity }),
    );

    this.loading.set(true);

    this.eventsService.reserveEventTickets(this.externalId, { itemRequests }).subscribe({
      next: (reservation) => {
        this.loading.set(false);
        this.router.navigate(['/events', this.externalId, 'checkout', reservation.reservationId]);
      },
      error: (err) => {
        this.loading.set(false);
        const message = err?.error?.detail ?? 'Could not reserve tickets. Please try again.';
        this.snackbar.showError(message, {
          label: 'Retry',
          callback: () => this.buyTickets(),
        });
      },
    });
  }
}
