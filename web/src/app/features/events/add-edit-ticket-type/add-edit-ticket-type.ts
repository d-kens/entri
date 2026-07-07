import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventDetailResponse } from '@features/auth/models/event.models';
import { EventsService } from '@features/events/services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { FormBuilder } from '@angular/forms';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-add-edit-ticket-type',
  imports: [
    TitleCasePipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './add-edit-ticket-type.html',
  styleUrl: './add-edit-ticket-type.css',
})
export class AddEditTicketType implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

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
