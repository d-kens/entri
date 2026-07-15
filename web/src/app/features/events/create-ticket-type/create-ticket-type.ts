import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventDetailResponse } from '@features/events/models/event.models';
import { EventsService } from '@features/events/services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EventHero } from '../components/event-hero/event-hero';
import {
  TicketTypeForm,
  TicketTypeFormData,
} from '@features/events/components/ticket-type-form/ticket-type-form';

@Component({
  selector: 'app-create-ticket-type',
  imports: [MatProgressSpinnerModule, EventHero, TicketTypeForm],
  templateUrl: './create-ticket-type.html',
  styleUrl: './create-ticket-type.css',
})
export class CreateTicketType implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  ticketType = signal<TicketTypeFormData>({
    name: '',
    price: '',
    quantity: '',
    description: '',
    salesStartDate: '',
    salesEndDate: '',
    maxPerOrder: '',
  });

  isLoadingEvent = signal(true);
  isSaving = signal(false);
  eventId = signal(this.route.snapshot.paramMap.get('eventId')!);
  event = signal<EventDetailResponse | null>(null);

  ngOnInit() {
    this.eventService.getEvent(this.eventId()).subscribe({
      next: (event) => {
        this.event.set(event);
        this.isLoadingEvent.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load event');
        this.isLoadingEvent.set(false);
        this.router.navigate(['/dashboard/events']);
      },
    });
  }

  onSaved(data: TicketTypeFormData): void {
    this.isSaving.set(true);
    console.log(data);
    this.isSaving.set(false);
  }
}
