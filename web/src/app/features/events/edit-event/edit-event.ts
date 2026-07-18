import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventsService } from '@features/events/services/events-service';
import { FileService } from '@shared/services/file-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CategoryResponse } from '@features/events/models/event.models';
import {
  EventDetailsForm,
  EventDetailsFormData,
} from '@features/events/event-details-form/event-details-form';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { Shimmer } from '@shared/components/shimmer/shimmer';
import { PageError } from '@shared/components/page-error/page-error';
import { firstValueFrom } from 'rxjs';
import { combineDateTime, formatTime } from '@features/events/utils/date-time.utils';

@Component({
  selector: 'app-edit-event',
  imports: [RouterLink, EventDetailsForm, MatButton, MatIcon, Shimmer, PageError],
  templateUrl: './edit-event.html',
  styleUrl: './edit-event.css',
})
export class EditEvent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fileService = inject(FileService);
  private eventService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  loading = signal(true);
  hasError = signal(false);
  categories = signal<CategoryResponse[]>([]);
  eventExternalId = signal(this.route.snapshot.paramMap.get('eventExternalId')!);
  eventDetailsFormData = signal<EventDetailsFormData | undefined>(undefined);

  ngOnInit() {
    this.eventService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
    });

    this.loadEventData();
  }

  loadEventData(): void {
    this.loading.set(true);
    this.hasError.set(false);
    this.eventService.getEvent(this.eventExternalId()).subscribe({
      next: (event) => {
        console.log(event);
        const startDate = new Date(event.startTime);
        const endDate = new Date(event.endTime);
        this.eventDetailsFormData.set({
          title: event.title,
          description: event.description,
          categoryId: event.categoryId,
          bannerUrl: event.bannerUrl,
          bannerFile: null,
          venueName: event.venueName,
          venueCity: event.venueCity,
          venueCountry: event.venueCountry,
          startDate: startDate,
          startTime: formatTime(startDate),
          endDate: endDate,
          endTime: formatTime(endDate),
        });
        this.loading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.loading.set(false);
      },
    });
  }

  async onSubmitted(data: EventDetailsFormData): Promise<void> {
    this.loading.set(true);

    try {
      let bannerUrl = data.bannerUrl;
      if (data.bannerFile) {
        bannerUrl = await firstValueFrom(this.fileService.upload(data.bannerFile));
      }

      const event = await firstValueFrom(
        this.eventService.updateEvent(this.eventExternalId(), {
          title: data.title,
          description: data.description,
          categoryId: data.categoryId as number,
          venueName: data.venueName,
          venueCity: data.venueCity,
          venueCountry: data.venueCountry,
          startTime: combineDateTime(data.startDate, data.startTime),
          endTime: combineDateTime(data.endDate, data.endTime),
          bannerUrl,
        }),
      );

      this.snackbarService.showSuccess('Event created successfully!');
      this.router.navigate(['/dashboard/events', this.eventExternalId()]);
    } catch (err: any) {
      const msg = err?.error?.message || 'Failed to create event.';
      this.snackbarService.showError(msg);
    } finally {
      this.loading.set(false);
    }
  }
}
