import { Component, effect, input, output, signal } from '@angular/core';
import { form, FormField, min, required, submit, validate } from '@angular/forms/signals';
import {
  MatError,
  MatFormField,
  MatHint,
  MatInput,
  MatLabel,
  MatSuffix,
} from '@angular/material/input';
import { ReactiveFormsModule } from '@angular/forms';
import { CategoryResponse } from '@features/events/models/event.models';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { MatIcon } from '@angular/material/icon';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerToggle,
} from '@angular/material/datepicker';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

export interface EventDetailsFormData {
  title: string;
  description: string;
  categoryId: number | string;

  bannerUrl: string;
  bannerFile: File | null;

  venueName: string;
  venueCity: string;
  venueCountry: string;
  startDate: Date | string;
  startTime: string;
  endDate: Date | string;
  endTime: string;
}

function notInPast(message: string) {
  return (ctx: { value: () => Date | string }) => {
    const value = ctx.value();
    if (!value) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const date = value instanceof Date ? value : new Date(value);
    return date >= today ? null : { kind: 'minDate', message };
  };
}

@Component({
  selector: 'app-event-basic-details-form',
  imports: [
    MatError,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    MatHint,
    MatOption,
    MatSelect,
    FormField,
    MatIcon,
    MatSuffix,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatIconButton,
    MatButton,
    MatProgressSpinner,
  ],
  templateUrl: './event-details-form.html',
  styleUrl: './event-details-form.css',
})
export class EventDetailsForm {
  loading = input(false);
  formSubmitButtonLabel = input('Create Event');
  eventCategories = input<CategoryResponse[]>([]);
  data = input<EventDetailsFormData>();
  submitted = output<EventDetailsFormData>();

  bannerPreview = signal<string | null>(null);

  private eventDetailsFormData = signal<EventDetailsFormData>({
    title: '',
    description: '',
    categoryId: '',
    bannerUrl: '',
    bannerFile: null,
    venueName: '',
    venueCity: '',
    venueCountry: '',
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
  });

  eventDetailsForm = form(this.eventDetailsFormData, (fields) => {
    required(fields.title, { message: 'Title is required' });
    min(fields.title, 1, { message: 'Title should be at least 3 characters' });

    required(fields.description, { message: 'Description is required' });
    min(fields.description, 1, { message: 'Description should be at least 20 characters' });

    required(fields.categoryId, { message: 'Please select category for the event' });

    required(fields.venueName, { message: 'Event venues is required' });
    required(fields.venueCity, { message: 'Venue city is required' });
    required(fields.venueCountry, { message: 'Venue Country is required' });
    validate(fields.bannerUrl, (ctx) => {
      if (ctx.value() || this.eventDetailsFormData().bannerFile) return null;
      return { kind: 'required', message: 'Please upload a banner image' };
    });

    required(fields.startDate, { message: 'Start Date is required' });
    validate(fields.startDate, (ctx) =>
      this.data() ? null : notInPast('Start date must be today or later')(ctx),
    );
    required(fields.startTime, { message: 'Start Time is required' });
    required(fields.endDate, { message: 'End Date is required' });
    validate(fields.endDate, (ctx) =>
      this.data() ? null : notInPast('End date must be today or later')(ctx),
    );
    required(fields.endTime, { message: 'End Time is required' });
  });

  constructor() {
    effect(() => {
      const data = this.data();
      if (data) this.populateForm(data);
    });
  }

  onBannerSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const prev = this.bannerPreview();
    if (prev?.startsWith('blob:')) URL.revokeObjectURL(prev);
    this.bannerPreview.set(URL.createObjectURL(file));
    this.eventDetailsFormData.update((data) => ({ ...data, bannerFile: file }));
  }

  removeBanner(): void {
    const prev = this.bannerPreview();
    if (prev?.startsWith('blob:')) URL.revokeObjectURL(prev);
    this.bannerPreview.set(null);
    this.eventDetailsFormData.update((data) => ({ ...data, bannerFile: null, bannerUrl: '' }));
  }

  async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.eventDetailsForm, async () => {
      this.submitted.emit(this.eventDetailsFormData());
    });
  }

  private populateForm(data: EventDetailsFormData | undefined): void {
    if (data) {
      this.eventDetailsFormData.set(data);

      if (data.bannerUrl) {
        this.bannerPreview.set(data.bannerUrl);
      }
    }
  }
}
