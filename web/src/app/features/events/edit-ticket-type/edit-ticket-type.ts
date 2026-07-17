import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
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
  selector: 'app-edit-ticket-type',
  imports: [EventHero, TicketTypeForm, Shimmer, PageError],
  templateUrl: './edit-ticket-type.html',
  styleUrl: './edit-ticket-type.css',
})
export class EditTicketType implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  isLoading = signal(true);
  hasError = signal(false);
  isSaving = signal(false);
  event = signal<EventDetailResponse | null>(null);
  ticketTypeFormData = signal<TicketTypeFormData | undefined>(undefined);

  eventExternalId = signal(this.route.snapshot.paramMap.get('eventId')!);
  ticketTypeId = signal(this.route.snapshot.paramMap.get('ticketTypeId')!);

  ngOnInit() {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    forkJoin([
      this.eventService.getEvent(this.eventExternalId()),
      this.eventService.getTicketTypeById(Number(this.ticketTypeId())),
    ]).subscribe({
      next: ([event, ticketType]) => {
        this.event.set(event);
        this.ticketTypeFormData.set({
          name: ticketType.name,
          description: ticketType.description ?? '',
          price: String(ticketType.price),
          quantity: String(ticketType.quantity),
          maxTicketsPerOrder: ticketType.maxTicketsPerOrder,
          salesStartDate: ticketType.saleStartDate ?? '',
          salesEndDate: ticketType.saleEndDate ?? '',
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
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

    this.eventService.updateEventTicketType(this.ticketTypeId(), ticketType).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackbarService.showSuccess('Ticket type updated successfully');
        this.router.navigate(['/dashboard/events', this.eventExternalId()]);
      },
      error: () => {
        this.snackbarService.showError('Failed to update ticket type');
        this.isSaving.set(false);
      },
    });
  }
}
