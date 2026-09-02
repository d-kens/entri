import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { EventsService } from '@features/events/services/events-service';
import { TicketResponse } from '@features/events/models/event.models';

@Component({
  selector: 'app-ticket-view',
  standalone: true,
  imports: [DatePipe, MatIconModule, RouterLink],
  templateUrl: './ticket-view.html',
  styleUrl: './ticket-view.css',
})
export class TicketView implements OnInit {
  private route = inject(ActivatedRoute);
  private eventsService = inject(EventsService);
  readonly externalId = this.route.snapshot.paramMap.get('externalId')!;

  ticket = signal<TicketResponse | null>(null);
  loading = signal(true);
  error = signal(false);

  backLink = signal<string[] | null>(null);

  ngOnInit(): void {
    this.eventsService.getTicket(this.externalId).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.backLink.set(['/tickets', ticket.reservationExternalId]);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  qrUrl(ticketCode: string): string {
    return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${ticketCode}&qzone=1`;
  }

  formatCode(code: string): string {
    return `${code.slice(0, 4)}-${code.slice(4, 8)}-${code.slice(8)}`;
  }

  holderName(ticket: TicketResponse): string {
    return [ticket.holderFirstName, ticket.holderLastName].filter(Boolean).join(' ');
  }
}
