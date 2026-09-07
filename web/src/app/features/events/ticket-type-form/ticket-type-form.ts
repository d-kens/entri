import { Component, computed, effect, input, output, signal } from '@angular/core';
import { form, min, required, submit, validate, FormField } from '@angular/forms/signals';
import {
  MatError,
  MatFormField,
  MatHint,
  MatInput,
  MatLabel,
  MatPrefix,
  MatSuffix,
} from '@angular/material/input';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerToggle,
} from '@angular/material/datepicker';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

export interface TicketTypeFormData {
  name: string;
  price: number | string;
  quantity: number | string;
  description: string;
  salesStartDate: string | Date;
  salesEndDate: string | Date;
  maxTicketsPerOrder: number | string;
}

@Component({
  selector: 'app-tickek-type-form',
  imports: [
    MatError,
    MatFormField,
    MatInput,
    MatLabel,
    FormField,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatPrefix,
    MatSuffix,
    MatIcon,
    MatButton,
    MatProgressSpinner,
  ],
  templateUrl: './ticket-type-form.html',
  styleUrl: './ticket-type-form.css',
})
export class TicketTypeForm {
  initialData = input<TicketTypeFormData>();
  submitLabel = input('Add Ticket Type');
  isLoading = input(false);
  eventStartTime = input<string>();
  eventEndTime = input<string>();

  added = output<TicketTypeFormData>();

  minDate = computed(() => (this.eventStartTime() ? new Date(this.eventStartTime()!) : null));
  maxDate = computed(() => (this.eventEndTime() ? new Date(this.eventEndTime()!) : null));

  private formData = signal<TicketTypeFormData>({
    name: '',
    price: '',
    quantity: '',
    description: '',
    salesStartDate: '',
    salesEndDate: '',
    maxTicketsPerOrder: '',
  });

  ticketTypeForm = form(this.formData, (fields) => {
    required(fields.name, { message: 'Name is required' });
    required(fields.price, { message: 'Price is required' });
    required(fields.quantity, { message: 'Quantity is required' });
    required(fields.maxTicketsPerOrder, { message: 'Maximum order per ticket is required' });

    min(fields.price, 0, { message: 'Price cannot be negative' });
    min(fields.quantity, 1, { message: 'At least 1 item is required' });
    min(fields.maxTicketsPerOrder, 1, { message: 'Should be at least 1' });

    validate(fields.salesEndDate, (ctx) => {
      const end = ctx.value();
      const start = this.formData().salesStartDate;
      if (!end && !start) return null;
      if (!!start !== !!end) {
        return {
          kind: 'required',
          message: 'Both sale start and end dates must be provided together',
        };
      }
      const endDate = end instanceof Date ? end : new Date(end as string);
      const startDate = start instanceof Date ? start : new Date(start as string);
      return endDate >= startDate
        ? null
        : { kind: 'minDate', message: 'End date must be after start date' };
    });
  });

  constructor() {
    effect(() => {
      const data = this.initialData();
      if (data) this.formData.set(data);
    });
  }

  async onSubmit(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.ticketTypeForm, async () => {
      this.added.emit(this.formData());
    });
  }
}
