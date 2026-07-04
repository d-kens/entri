import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
import { CategoryResponse, EventRequest } from '@core/models/event.models';

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
  startTime: FormControl<string>;
  endDate: FormControl<Date | null>;
  endTime: FormControl<string>;
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
export class CreateEvent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private eventsService = inject(EventsService);
  private snackbarService = inject(SnackbarService);

  editId = signal<string | null>(null);
  isEditMode = signal(false);
  isLoadingEvent = signal(false);
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

  categories = signal<CategoryResponse[]>([]);
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
    startTime: ['', Validators.required],
    endDate: this.fb.control<Date | null>(null, Validators.required),
    endTime: ['', Validators.required],
  });

  ticketsForm = this.fb.group({
    ticketTypes: this.fb.array<FormGroup<TicketTypeForm>>([]),
  });

  get ticketTypes(): FormArray<FormGroup<TicketTypeForm>> {
    return this.ticketsForm.get('ticketTypes') as FormArray<FormGroup<TicketTypeForm>>;
  }

  get minDate(): Date | null {
    return this.isEditMode() ? null : new Date();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId.set(id);
      this.isEditMode.set(true);
      this.isLoadingEvent.set(true);
      this.loadForEdit(id);
    }
    this.eventsService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
    });
  }

  private loadForEdit(id: string): void {
    this.eventsService.getEvent(id).subscribe({
      next: (ev) => {
        this.infoForm.patchValue({
          title: ev.title,
          description: ev.description,
          isPublic: ev.isPublic,
        });

        // patch categoryId once categories may already be loaded
        const cat = this.categories().find((c) => c.name === ev.categoryName);
        if (cat) this.infoForm.patchValue({ categoryId: cat.id });
        else {
          // categories not yet loaded — re-patch when they arrive
          const sub = this.eventsService.getCategories().subscribe((cats) => {
            const matched = cats.find((c) => c.name === ev.categoryName);
            if (matched) this.infoForm.patchValue({ categoryId: matched.id });
            sub.unsubscribe();
          });
        }

        const start = new Date(ev.startTime);
        const end = new Date(ev.endTime);
        this.venueForm.patchValue({
          venueName: ev.venueName,
          venueCity: ev.venueCity,
          venueCountry: ev.venueCountry,
          startDate: start,
          startTime: this.toTimeString(start),
          endDate: end,
          endTime: this.toTimeString(end),
        });

        if (ev.bannerUrl) {
          this.bannerUrl.set(ev.bannerUrl);
          this.bannerPreview.set(ev.bannerUrl);
        }

        ev.ticketTypes.forEach((t) => {
          this.ticketTypes.push(
            this.fb.nonNullable.group<TicketTypeForm>({
              name: this.fb.nonNullable.control(t.name, Validators.required),
              description: this.fb.nonNullable.control(t.description ?? ''),
              price: this.fb.nonNullable.control(t.price, [Validators.required, Validators.min(0)]),
              currency: this.fb.nonNullable.control(t.currency, Validators.required),
              quantity: this.fb.nonNullable.control(t.quantity, [
                Validators.required,
                Validators.min(1),
              ]),
              maxPerOrder: this.fb.control<number | null>(t.maxPerOrder ?? null),
              saleStartDate: this.fb.control<Date | null>(
                t.saleStartDate ? new Date(t.saleStartDate) : null,
              ),
              saleEndDate: this.fb.control<Date | null>(
                t.saleEndDate ? new Date(t.saleEndDate) : null,
              ),
              isHidden: this.fb.nonNullable.control(t.isHidden),
            }),
          );
        });

        this.isLoadingEvent.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load event');
        this.router.navigate(['/dashboard/events']);
      },
    });
  }

  nextStep(): void {
    if (this.currentStep() === 0) {
      this.infoForm.markAllAsTouched();
      if (this.infoForm.invalid) return;
    } else if (this.currentStep() === 1) {
      this.venueForm.markAllAsTouched();
      if (this.venueForm.invalid) return;
    }
    this.currentStep.update((s) => Math.min(s + 1, this.steps.length - 1));
  }

  prevStep(): void {
    this.currentStep.update((s) => Math.max(s - 1, 0));
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
      }),
    );
  }

  removeTicketType(index: number): void {
    this.ticketTypes.removeAt(index);
    this.expandedTickets.update((set) => {
      const next = new Set<number>();
      set.forEach((i) => {
        if (i !== index) next.add(i > index ? i - 1 : i);
      });
      return next;
    });
  }

  ticketGroup(index: number): FormGroup<TicketTypeForm> {
    return this.ticketTypes.at(index);
  }

  toggleAdvanced(index: number): void {
    this.expandedTickets.update((set) => {
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
      error: (err: HttpErrorResponse) => {
        const message =
          err.status === 413
            ? 'File is too large. Please upload a smaller image.'
            : 'Failed to upload banner image. Please try again.';
        this.snackbarService.showError(message);
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
      this.snackbarService.showError('Add at least one ticket type before saving');
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
    const payload = this.buildPayload();
    const id = this.editId();

    const request$ = id
      ? this.eventsService.updateEvent(id, payload)
      : this.eventsService.createEvent(payload);

    request$.subscribe({
      next: (event) => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess(
          id ? 'Event updated successfully!' : 'Event created successfully!',
        );
        this.router.navigate(['/dashboard/events', event.externalId]);
      },
      error: (err) => {
        const msg =
          err?.error?.message || (id ? 'Failed to update event.' : 'Failed to create event.');
        this.snackbarService.showError(msg);
        this.isLoading.set(false);
      },
    });
  }

  private toTimeString(date: Date): string {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  }

  private combineDateTime(date: Date, time: string): string {
    const [hours, minutes] = time.split(':').map(Number);
    const dt = new Date(date);
    dt.setHours(hours, minutes, 0, 0);
    return dt.toISOString();
  }

  private buildPayload(): EventRequest {
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
      startTime: this.combineDateTime(venue.startDate as Date, venue.startTime),
      endTime: this.combineDateTime(venue.endDate as Date, venue.endTime),
      bannerUrl: this.bannerUrl() as string,
      ticketTypes: this.ticketTypes.controls.map((ctrl) => {
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
