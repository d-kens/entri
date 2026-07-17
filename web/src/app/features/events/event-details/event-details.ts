import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EventsService } from '../services/events-service';
import { EventDetailResponse } from '@features/events/models/event.models';
import { EventHero } from '../components/event-hero/event-hero';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { PageError } from '@shared/components/page-error/page-error';

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
  private route = inject(ActivatedRoute);
  private eventsService = inject(EventsService);

  event = signal<EventDetailResponse | null>(null);
  isLoading = signal(true);
  hasError = signal(false);

  private eventId = this.route.snapshot.paramMap.get('id')!;

  ngOnInit(): void {
    this.loadEvent();
  }

  loadEvent(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    this.eventsService.getEvent(this.eventId).subscribe({
      next: (event) => {
        this.event.set(event);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
