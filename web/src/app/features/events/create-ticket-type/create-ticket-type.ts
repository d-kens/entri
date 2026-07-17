import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventDetailResponse, TicketTypeRequest } from '@features/events/models/event.models';
import { EventsService } from '@features/events/services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventHero } from '@features/events/event-hero/event-hero';
import {
  TicketTypeForm,
  TicketTypeFormData,
} from '@features/events/ticket-type-form/ticket-type-form';
import { Shimmer } from '@shared/components/shimmer/shimmer';
import { PageError } from '@shared/components/page-error/page-error';

@Component({
  selector: 'app-create-ticket-type',
  imports: [EventHero, TicketTypeForm, Shimmer, PageError],
  templateUrl: './create-ticket-type.html',
  styleUrl: './create-ticket-type.css',
})
export class CreateTicketType implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  isLoadingEvent = signal(true);
  hasError = signal(false);
  isSaving = signal(false);
  eventExternalId = signal(this.route.snapshot.paramMap.get('eventId')!);
  event = signal<EventDetailResponse | null>(null);

  ngOnInit() {
    this.loadEvent();
  }

  loadEvent(): void {
    this.isLoadingEvent.set(true);
    this.hasError.set(false);

    this.eventService.getEvent(this.eventExternalId()).subscribe({
      next: (event) => {
        this.event.set(event);
        this.isLoadingEvent.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoadingEvent.set(false);
      },
    });
  }

  onSaved(data: TicketTypeFormData): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);

    const ticketType: TicketTypeRequest = {
      name: data.name,
      price: Number(data.price),
      description: data.description,
      currency: 'KES',
      quantity: Number(data.quantity),
      maxTicketsPerOrder: Number(data.maxTicketsPerOrder),
      saleStartDate: data.salesStartDate,
      saleEndDate: data.salesEndDate,
    };

    this.eventService.addEventTicketType(this.eventExternalId(), ticketType).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackbarService.showSuccess('Ticket type added successfully');
        this.router.navigate(['/dashboard/events', this.eventExternalId()]);
      },
      error: () => {
        this.snackbarService.showError('Failed to add ticket type');
        this.isSaving.set(false);
      },
    });
  }
}
