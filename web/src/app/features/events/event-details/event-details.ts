import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { forkJoin } from 'rxjs';
import { EventsService } from '../services/events-service';
import { EventHero } from '@features/events/event-hero/event-hero';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { PageError } from '@shared/components/page-error/page-error';
import { SnackbarService } from '@shared/services/snackbar-service';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';
import { CheckInCodeDialog } from '@shared/components/check-in-code-dialog/check-in-code-dialog';
import {
  EventResponse,
  TicketTypeResponse,
  TicketTypeSaleStatus,
  TicketTypeAvailabilityStatus,
} from '@features/events/models/event.models';

@Component({
  selector: 'app-event-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatTooltipModule,
    EventHero,
    EntriButton,
    PageError,
  ],
  templateUrl: './event-details.html',
  styleUrl: './event-details.css',
})
export class EventDetails implements OnInit {
  readonly saleStatusLabel: Record<TicketTypeSaleStatus, string> = {
    ON_SALE: 'On Sale',
    UPCOMING: 'Upcoming',
    ENDED: 'Sale Ended',
  };

  readonly availabilityStatusLabel: Record<TicketTypeAvailabilityStatus, string> = {
    AVAILABLE: 'Available',
    SOLD_OUT: 'Sold Out',
  };
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private dialog = inject(MatDialog);
  private snackbarService = inject(SnackbarService);

  event = signal<EventResponse | null>(null);
  ticketTypes = signal<TicketTypeResponse[]>([]);
  isLoading = signal(true);
  hasError = signal(false);
  eventExternalId = signal(this.route.snapshot.paramMap.get('eventExternalId')!);

  ngOnInit(): void {
    this.loadEvent();
  }

  navigateToEditEvent(externalId: string): void {
    this.router.navigate(['/dashboard/events', externalId, 'edit']);
  }

  navigateToAddTicket(externalId: string): void {
    this.router.navigate(['/dashboard/events', externalId, 'ticket-types']);
  }

  navigateToEditTicketType(externalId: string, ticketId: number): void {
    this.router.navigate(['/dashboard/events', externalId, 'ticket-types', ticketId, 'edit']);
  }

  openDeleteTicketTypeDialog(ticketId: number, ticketName: string): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        message: `"${ticketName}" will be permanently deleted. This action cannot be undone.`,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.eventsService.deleteTicketType(ticketId).subscribe({
        next: () => {
          this.snackbarService.showSuccess('Ticket type deleted');
          this.loadEvent();
        },
        error: () => {
          this.snackbarService.showError('Failed to delete ticket type');
        },
      });
    });
  }

  publishEvent(externalId: string): void {
    this.eventsService.publishEvent(externalId).subscribe({
      next: (event) => {
        this.event.set(event);
        this.snackbarService.showSuccess('Event published');
      },
      error: (err: Error) => {
        this.snackbarService.showError(err.message);
      },
    });
  }

  cancelEvent(externalId: string): void {
    this.eventsService.cancelEvent(externalId).subscribe({
      next: (event) => {
        this.event.set(event);
        this.snackbarService.showSuccess('Event cancelled');
      },
      error: (err: Error) => {
        this.snackbarService.showError(err.message);
      },
    });
  }

  generateCheckInCode(externalId: string): void {
    this.eventsService.generateCheckInCode(externalId).subscribe({
      next: (response) => {
        this.dialog.open(CheckInCodeDialog, { data: response });
      },
      error: (err: Error) => {
        this.snackbarService.showError(err.message);
      },
    });
  }

  loadEvent(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    forkJoin([
      this.eventsService.getEvent(this.eventExternalId()),
      this.eventsService.getEventTicketTypes(this.eventExternalId()),
    ]).subscribe({
      next: ([event, ticketTypes]) => {
        this.event.set(event);
        this.ticketTypes.set(ticketTypes);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
