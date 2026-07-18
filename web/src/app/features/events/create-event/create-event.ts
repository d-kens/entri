import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EventsService } from '../services/events-service';
import { FileService } from '@shared/services/file-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CategoryResponse } from '@features/events/models/event.models';
import {
  EventBasicDetailsFormData,
  EventDetailsForm,
} from '../event-details-form/event-details-form';

@Component({
  selector: 'app-create-event',
  imports: [RouterLink, MatButtonModule, MatIconModule, EventDetailsForm],
  templateUrl: './create-event.html',
  styleUrl: './create-event.css',
})
export class CreateEvent implements OnInit {
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private fileService = inject(FileService);
  private snackbarService = inject(SnackbarService);

  isLoading = signal(false);
  categories = signal<CategoryResponse[]>([]);

  ngOnInit(): void {
    this.eventsService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
    });
  }

  async onSubmitted(data: EventBasicDetailsFormData): Promise<void> {
    this.isLoading.set(true);
    try {
      let bannerUrl = data.bannerUrl;
      if (data.bannerFile) {
        bannerUrl = await firstValueFrom(this.fileService.upload(data.bannerFile));
      }

      const event = await firstValueFrom(
        this.eventsService.createEvent({
          title: data.title,
          description: data.description,
          categoryId: data.categoryId as number,
          venueName: data.venueName,
          venueCity: data.venueCity,
          venueCountry: data.venueCountry,
          startTime: this.combineDateTime(data.startDate, data.startTime),
          endTime: this.combineDateTime(data.endDate, data.endTime),
          bannerUrl,
        }),
      );

      this.snackbarService.showSuccess('Event created successfully!');
      this.router.navigate(['/dashboard/events', event.externalId]);
    } catch (err: any) {
      const msg = err?.error?.message || 'Failed to create event.';
      this.snackbarService.showError(msg);
    } finally {
      this.isLoading.set(false);
    }
  }

  private combineDateTime(date: Date | string, time: string): string {
    const [hours, minutes] = time.split(':').map(Number);
    const dt = new Date(date);
    dt.setHours(hours, minutes, 0, 0);
    return dt.toISOString();
  }
}
