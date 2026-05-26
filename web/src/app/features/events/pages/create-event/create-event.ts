import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { EventsService } from '../../services/events-service';
import { SnackbarService } from '@core/services/snackbar-service';
import { CreateEventRequest } from '@core/models/event.models';

interface Category {
  id: number;
  name: string;
}

type InfoForm = {
  title: FormControl<string>;
  description: FormControl<string>;
  categoryId: FormControl<number | null>;
  isPublic: FormControl<boolean>;
};

type VenueForm = {
  venueName: FormControl<string>;
  venueCity: FormControl<string>;
  venueCountry: FormControl<string>;
  startDate: FormControl<Date | null>;
  endDate: FormControl<Date | null>;
};

type TicketTypeForm = {
  name: FormControl<string>;
  description: FormControl<string>;
  price: FormControl<number>;
  currency: FormControl<string>;
  quantity: FormControl<number>;
  maxPerOrder: FormControl<number | null>;
  saleStartDate: FormControl<Date | null>;
  saleEndDate: FormControl<Date | null>;
  isHidden: FormControl<boolean>;
};

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatDatepickerModule,
  ],
  templateUrl: './create-event.html',
  styleUrl: './create-event.css',
})
export class CreateEvent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  isLoading = signal(false);
  isUploadingBanner = signal(false);
  isDeletingBanner = signal(false);
  bannerFile = signal<File | null>(null);
  bannerPreview = signal<string | null>(null);
  bannerUrl = signal<string | null>(null);
  currentStep = signal(0);
  expandedTickets = signal(new Set<number>());

  readonly steps = [
    { index: 0, name: 'Event Details', desc: 'Title, category & visibility' },
    { index: 1, name: 'Venue & Schedule', desc: 'Location, date & time' },
    { index: 2, name: 'Media & Tickets', desc: 'Banner & ticket types' },
  ];

  readonly categories: Category[] = [
    { id: 1, name: 'Music & Concerts' },
    { id: 2, name: 'Sports & Fitness' },
    { id: 3, name: 'Food & Drink' },
    { id: 4, name: 'Arts & Culture' },
    { id: 5, name: 'Business & Networking' },
    { id: 6, name: 'Education & Workshops' },
    { id: 7, name: 'Community & Social' },
    { id: 8, name: 'Technology' },
    { id: 9, name: 'Fashion & Beauty' },
    { id: 10, name: 'Other' },
  ];

  readonly currencies = ['KES', 'USD', 'EUR', 'GBP', 'TZS', 'UGX'];

  infoForm: FormGroup<InfoForm> = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(20)]],
    categoryId: this.fb.control<number | null>(null, Validators.required),
    isPublic: [true],
  });

  venueForm: FormGroup<VenueForm> = this.fb.nonNullable.group({
    venueName: ['', Validators.required],
    venueCity: ['', Validators.required],
    venueCountry: ['', Validators.required],
    startDate: this.fb.control<Date | null>(null, Validators.required),
    endDate: this.fb.control<Date | null>(null, Validators.required),
  });

  ticketsForm = this.fb.group({
    ticketTypes: this.fb.array<FormGroup<TicketTypeForm>>([]),
  });

  get ticketTypes(): FormArray<FormGroup<TicketTypeForm>> {
    return this.ticketsForm.get('ticketTypes') as FormArray<FormGroup<TicketTypeForm>>;
  }

  readonly minDate = new Date();

  nextStep(): void {
    if (this.currentStep() === 0) {
      this.infoForm.markAllAsTouched();
      if (this.infoForm.invalid) return;
    } else if (this.currentStep() === 1) {
      this.venueForm.markAllAsTouched();
      if (this.venueForm.invalid) return;
    }
    this.currentStep.update(s => Math.min(s + 1, this.steps.length - 1));
  }

  prevStep(): void {
    this.currentStep.update(s => Math.max(s - 1, 0));
  }

  addTicketType(): void {
    this.ticketTypes.push(
      this.fb.nonNullable.group<TicketTypeForm>({
        name: this.fb.nonNullable.control('', Validators.required),
        description: this.fb.nonNullable.control(''),
        price: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
        currency: this.fb.nonNullable.control('KES', Validators.required),
        quantity: this.fb.nonNullable.control(100, [Validators.required, Validators.min(1)]),
        maxPerOrder: this.fb.control<number | null>(null),
        saleStartDate: this.fb.control<Date | null>(null),
        saleEndDate: this.fb.control<Date | null>(null),
        isHidden: this.fb.nonNullable.control(false),
      })
    );
  }

  removeTicketType(index: number): void {
    this.ticketTypes.removeAt(index);
    this.expandedTickets.update(set => {
      const next = new Set<number>();
      set.forEach(i => { if (i !== index) next.add(i > index ? i - 1 : i); });
      return next;
    });
  }

  ticketGroup(index: number): FormGroup<TicketTypeForm> {
    return this.ticketTypes.at(index);
  }

  toggleAdvanced(index: number): void {
    this.expandedTickets.update(set => {
      const next = new Set(set);
      next.has(index) ? next.delete(index) : next.add(index);
      return next;
    });
  }

  isExpanded(index: number): boolean {
    return this.expandedTickets().has(index);
  }

  onBannerSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    this.bannerFile.set(file);
    this.bannerUrl.set(null);

    const reader = new FileReader();
    reader.onload = (e) => this.bannerPreview.set(e.target?.result as string);
    reader.readAsDataURL(file);

    this.isUploadingBanner.set(true);
    this.eventsService.uploadBanner(file).subscribe({
      next: ({ url }) => {
        this.bannerUrl.set(url);
        this.isUploadingBanner.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to upload banner image. Please try again.');
        this.bannerFile.set(null);
        this.bannerPreview.set(null);
        this.isUploadingBanner.set(false);
      },
    });
  }

  removeBanner(): void {
    const url = this.bannerUrl();
    this.bannerFile.set(null);
    this.bannerPreview.set(null);
    this.bannerUrl.set(null);

    if (url) {
      this.isDeletingBanner.set(true);
      this.eventsService.deleteBanner(url).subscribe({
        next: () => this.isDeletingBanner.set(false),
        error: () => this.isDeletingBanner.set(false),
      });
    }
  }

  submit(): void {
    if (this.isUploadingBanner()) {
      this.snackbarService.showError('Please wait for the banner to finish uploading');
      return;
    }

    if (!this.bannerUrl()) {
      this.snackbarService.showError('Please upload a banner image');
      return;
    }

    if (this.ticketTypes.length === 0) {
      this.snackbarService.showError('Add at least one ticket type before publishing');
      return;
    }

    if (this.infoForm.invalid || this.venueForm.invalid) {
      this.snackbarService.showError('Please complete all required fields');
      return;
    }

    this.ticketsForm.markAllAsTouched();
    if (this.ticketsForm.invalid) {
      this.snackbarService.showError('Please complete all ticket type fields');
      return;
    }

    this.isLoading.set(true);

    this.eventsService.createEvent(this.buildPayload()).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Event created successfully!');
        this.router.navigateByUrl('/dashboard/events');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to create event. Please try again.';
        this.snackbarService.showError(msg);
        this.isLoading.set(false);
      },
    });
  }

  private buildPayload(): CreateEventRequest {
    const info = this.infoForm.getRawValue();
    const venue = this.venueForm.getRawValue();

    return {
      title: info.title,
      description: info.description,
      categoryId: info.categoryId as number,
      isPublic: info.isPublic,
      venueName: venue.venueName,
      venueCity: venue.venueCity,
      venueCountry: venue.venueCountry,
      startTime: (venue.startDate as Date).toISOString(),
      endTime: (venue.endDate as Date).toISOString(),
      bannerUrl: this.bannerUrl() as string,
      ticketTypes: this.ticketTypes.controls.map(ctrl => {
        const t = ctrl.getRawValue();
        return {
          name: t.name,
          description: t.description || null,
          price: t.price,
          currency: t.currency,
          quantity: t.quantity,
          maxPerOrder: t.maxPerOrder,
          saleStartDate: t.saleStartDate ? t.saleStartDate.toISOString() : null,
          saleEndDate: t.saleEndDate ? t.saleEndDate.toISOString() : null,
          isHidden: t.isHidden,
        };
      }),
    };
  }
}
