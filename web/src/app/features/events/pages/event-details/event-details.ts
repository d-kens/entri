import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EventsService } from '../../services/events-service';
import { SnackbarService } from '@core/services/snackbar-service';
import {EventDetailResponse, EventResponse} from '@core/models/event.models';

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
  isPublishing = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.eventsService.getEvent(id).subscribe({
      next: (event) => {
        this.event.set(event);
        this.isLoading.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load event');
        this.isLoading.set(false);
        this.router.navigate(['/events']);
      },
    });
  }

  publish(): void {
    console.log("Event published.")
  }
}
