import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventDetailResponse } from '@features/events/models/event.models';
import { EventsService } from '@features/events/services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EventHero } from '../components/event-hero/event-hero';
import { form, min, required } from '@angular/forms/signals';

export interface TicketTypeFormData {
  name: string;
  price: number;
  quantity: number;
  description: string;
  salesStartDate: string;
  salesEndDate: string;
  maxPerOrder: number;
}

@Component({
  selector: 'app-add-edit-ticket-type',
  imports: [MatProgressSpinnerModule, EventHero],
  templateUrl: './add-edit-ticket-type.html',
  styleUrl: './add-edit-ticket-type.css',
})
export class AddEditTicketType implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  ticketType = signal<TicketTypeFormData>({
    name: '',
    price: 1,
    quantity: 0,
    description: '',
    salesStartDate: '',
    salesEndDate: '',
    maxPerOrder: 0,
  });

  ticketTypeForm = form(this.ticketType, (fields) => {
    required(fields.name);
    required(fields.price);
    required(fields.quantity);

    min(fields.price, 1, { message: 'Price must be a minimum of KES 1' });
    min(fields.quantity, 1, { message: 'Quantity must be a minimum of 1' });
  });

  isLoading = signal(true);
  eventId = signal(this.route.snapshot.paramMap.get('eventId')!);
  event = signal<EventDetailResponse | null>(null);

  ngOnInit() {
    this.eventService.getEvent(this.eventId()).subscribe({
      next: (event) => {
        this.event.set(event);
        console.log(event);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load event');
        this.isLoading.set(false);
        this.router.navigate(['/dashboard/events']);
      },
    });
  }

  submit() {
    this.isLoading.set(true);
  }
}
