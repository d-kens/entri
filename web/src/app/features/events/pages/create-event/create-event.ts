import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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

interface Category {
  id: number;
  name: string;
}

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
  bannerFile = signal<File | null>(null);
  bannerPreview = signal<string | null>(null);
  currentStep = signal(0);

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

  infoForm = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(20)]],
    categoryId: [null as number | null, Validators.required],
    isPublic: [true],
  });

  venueForm = this.fb.group({
    venueName: ['', Validators.required],
    venueCity: ['', Validators.required],
    venueCountry: ['', Validators.required],
    startDate: [null as Date | null, Validators.required],
    endDate: [null as Date | null, Validators.required],
  });

  ticketsForm = this.fb.group({
    ticketTypes: this.fb.array([]),
  });

  get ticketTypes(): FormArray {
    return this.ticketsForm.get('ticketTypes') as FormArray;
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
    this.ticketTypes.push(this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      currency: ['KES', Validators.required],
      quantity: [100, [Validators.required, Validators.min(1)]],
      maxPerOrder: [null],
      saleStartDate: [null as Date | null],
      saleEndDate: [null as Date | null],
      isHidden: [false],
    }));
  }

  removeTicketType(index: number): void {
    this.ticketTypes.removeAt(index);
  }

  ticketGroup(index: number): FormGroup {
    return this.ticketTypes.at(index) as FormGroup;
  }

  onBannerSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.bannerFile.set(file);
    const reader = new FileReader();
    reader.onload = (e) => this.bannerPreview.set(e.target?.result as string);
    reader.readAsDataURL(file);
  }

  removeBanner(): void {
    this.bannerFile.set(null);
    this.bannerPreview.set(null);
  }

  submit(): void {
    if (!this.bannerFile()) {
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

    this.eventsService.createEvent(this.buildFormData()).subscribe({
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

  private buildFormData(): FormData {
    const fd = new FormData();
    const info = this.infoForm.value;
    const venue = this.venueForm.value;

    fd.append('title', info.title!);
    fd.append('description', info.description!);
    fd.append('categoryId', info.categoryId!.toString());
    fd.append('isPublic', (info.isPublic ?? true).toString());

    fd.append('venueName', venue.venueName!);
    fd.append('venueCity', venue.venueCity!);
    fd.append('venueCountry', venue.venueCountry!);
    fd.append('startTime', (venue.startDate as Date).toISOString());
    fd.append('endTime', (venue.endDate as Date).toISOString());

    fd.append('bannerImage', this.bannerFile()!);

    this.ticketTypes.controls.forEach((ctrl, i) => {
      const t = ctrl.value;
      fd.append(`ticketTypes[${i}].name`, t.name);
      if (t.description) fd.append(`ticketTypes[${i}].description`, t.description);
      fd.append(`ticketTypes[${i}].price`, t.price.toString());
      fd.append(`ticketTypes[${i}].currency`, t.currency);
      fd.append(`ticketTypes[${i}].quantity`, t.quantity.toString());
      if (t.maxPerOrder) fd.append(`ticketTypes[${i}].maxPerOrder`, t.maxPerOrder.toString());
      if (t.saleStartDate) fd.append(`ticketTypes[${i}].saleStartDate`, (t.saleStartDate as Date).toISOString());
      if (t.saleEndDate) fd.append(`ticketTypes[${i}].saleEndDate`, (t.saleEndDate as Date).toISOString());
      fd.append(`ticketTypes[${i}].isHidden`, (t.isHidden ?? false).toString());
    });

    return fd;
  }
}
