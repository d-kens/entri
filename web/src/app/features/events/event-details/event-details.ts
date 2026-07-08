import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EventsService } from '../services/events-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EventDetailResponse } from '@features/events/models/event.models';
import { EventHero } from '../components/event-hero/event-hero';

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
  ],
  templateUrl: './event-details.html',
  styleUrl: './event-details.css',
})
export class EventDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  event = signal<EventDetailResponse | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const eventId = signal(this.route.snapshot.paramMap.get('id')!);
    this.eventsService.getEvent(eventId()).subscribe({
      next: (event) => {
        this.event.set(event);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load event');
        this.isLoading.set(false);
        this.router.navigate(['/dashboard/events']);
      },
    });
  }
}
