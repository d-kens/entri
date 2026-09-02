import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { first, interval, Subscription, switchMap } from 'rxjs';
import { EventsService } from '@features/events/services/events-service';
import { TicketResponse } from '@features/events/models/event.models';

@Component({
  selector: 'app-reservation-tickets',
  standalone: true,
  imports: [DatePipe, MatIconModule],
  templateUrl: './reservation-tickets.html',
  styleUrl: './reservation-tickets.css',
})
export class ReservationTickets implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private eventsService = inject(EventsService);

  readonly reservationId = this.route.snapshot.paramMap.get('reservationId')!;

  tickets = signal<TicketResponse[]>([]);
  loading = signal(true);
  error = signal(false);
  processingTimeout = signal(false);

  private pollSub?: Subscription;

  ngOnInit(): void {
    this.eventsService.getTicketsByReservation(this.reservationId).subscribe({
      next: (tickets) => {
        if (tickets.length > 0) {
          this.tickets.set(tickets);
          this.loading.set(false);
        } else {
          this.startPolling();
        }
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  qrUrl(ticketCode: string): string {
    return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${ticketCode}&qzone=1`;
  }

  holderName(ticket: TicketResponse): string {
    return [ticket.holderFirstName, ticket.holderLastName].filter(Boolean).join(' ');
  }

  private startPolling(): void {
    this.pollSub = interval(3000)
      .pipe(
        switchMap(() => this.eventsService.getTicketsByReservation(this.reservationId)),
        first((t) => t.length > 0, [] as TicketResponse[]),
      )
      .subscribe({
        next: (tickets) => {
          this.tickets.set(tickets);
          this.loading.set(false);
          if (tickets.length === 0) {
            this.processingTimeout.set(true);
          }
        },
      });
  }
}
