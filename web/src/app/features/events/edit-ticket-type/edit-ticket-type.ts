import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EventResponse, TicketTypeRequest } from '@features/events/models/event.models';
import { EventsService } from '@features/events/services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventHero } from '@features/events/event-hero/event-hero';
import {
  TicketTypeForm,
  TicketTypeFormData,
} from '@features/events/ticket-type-form/ticket-type-form';
import { Shimmer } from '@shared/components/shimmer/shimmer';
import { PageError } from '@shared/components/page-error/page-error';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-edit-ticket-type',
  imports: [EventHero, TicketTypeForm, Shimmer, PageError, EntriButton, MatIconModule],
  templateUrl: './edit-ticket-type.html',
  styleUrl: './edit-ticket-type.css',
})
export class EditTicketType implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);
  private dialog = inject(MatDialog);

  loading = signal(true);
  hasError = signal(false);
  event = signal<EventResponse | null>(null);
  ticketTypeFormData = signal<TicketTypeFormData | undefined>(undefined);

  eventExternalId = signal(this.route.snapshot.paramMap.get('eventExternalId')!);
  ticketTypeId = signal(this.route.snapshot.paramMap.get('ticketTypeId')!);

  ngOnInit() {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
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
          quantity: String(ticketType.availableQuantity),
          maxTicketsPerOrder: ticketType.maxTicketsPerOrder,
          salesStartDate: ticketType.saleStartDate ?? '',
          salesEndDate: ticketType.saleEndDate ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.loading.set(false);
      },
    });
  }

  openDeleteDialog(): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      panelClass: 'entri-confirm-dialog',
      data: {
        message: 'This ticket type will be permanently deleted. This action cannot be undone.',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.eventService.deleteTicketType(Number(this.ticketTypeId())).subscribe({
        next: () => {
          this.snackbarService.showSuccess('Ticket type deleted');
          this.router.navigate(['/dashboard/events', this.eventExternalId()]);
        },
        error: () => {
          this.snackbarService.showError('Failed to delete ticket type');
        },
      });
    });
  }

  onSaved(data: TicketTypeFormData): void {
    if (this.loading()) return;

    this.loading.set(true);

    const ticketType: TicketTypeRequest = {
      name: data.name,
      price: Number(data.price),
      description: data.description,
      quantity: Number(data.quantity),
      maxTicketsPerOrder: Number(data.maxTicketsPerOrder),
      saleStartDate: data.salesStartDate,
      saleEndDate: data.salesEndDate,
    };

    this.eventService.updateEventTicketType(this.ticketTypeId(), ticketType).subscribe({
      next: () => {
        this.loading.set(false);
        this.snackbarService.showSuccess('Ticket type updated successfully');
        this.router.navigate(['/dashboard/events', this.eventExternalId()]);
      },
      error: () => {
        this.snackbarService.showError('Failed to update ticket type');
        this.loading.set(false);
      },
    });
  }
}
