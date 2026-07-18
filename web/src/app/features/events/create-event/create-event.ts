import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EventsService } from '../services/events-service';
import { FileService } from '@shared/services/file-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CategoryResponse } from '@features/events/models/event.models';
import { EventDetailsFormData, EventDetailsForm } from '../event-details-form/event-details-form';
import { combineDateTime } from '@features/events/utils/date-time.utils';

@Component({
  selector: 'app-create-event',
  imports: [RouterLink, MatButtonModule, MatIconModule, EventDetailsForm],
  templateUrl: './create-event.html',
  styleUrl: './create-event.css',
})
export class CreateEvent implements OnInit {
  private router = inject(Router);
  private eventService = inject(EventsService);
  private fileService = inject(FileService);
  private snackbarService = inject(SnackbarService);

  loading = signal(false);
  categories = signal<CategoryResponse[]>([]);

  ngOnInit(): void {
    this.eventService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
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
        this.eventService.createEvent({
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
      this.router.navigate(['/dashboard/events', event.externalId]);
    } catch (err: any) {
      const msg = err?.error?.message || 'Failed to create event.';
      this.snackbarService.showError(msg);
    } finally {
      this.loading.set(false);
    }
  }
}
