import {
  Component,
  inject,
  Injector,
  input,
  OnInit,
  output,
  runInInjectionContext,
  WritableSignal,
} from '@angular/core';
import { form, min, required, submit, FormField } from '@angular/forms/signals';
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
  salesStartDate: string;
  salesEndDate: string;
  maxPerOrder: number | string;
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
export class TicketTypeForm implements OnInit {
  private injector = inject(Injector);

  data = input.required<WritableSignal<TicketTypeFormData>>();
  submitLabel = input('Add Ticket Type');
  isLoading = input(false);

  added = output<TicketTypeFormData>();

  ticketTypeForm!: ReturnType<typeof form<TicketTypeFormData>>;

  ngOnInit(): void {
    this.ticketTypeForm = runInInjectionContext(this.injector, () =>
      form(this.data(), (fields) => {
        required(fields.name, { message: 'Name is required' });
        required(fields.price, { message: 'Price is required' });
        required(fields.quantity, { message: 'Quantity is required' });

        min(fields.price, 1, { message: 'Price cannot be negative' });
        min(fields.quantity, 1, { message: 'At least 1 item is required' });
      }),
    );
  }

  async onSubmit(): Promise<void> {
    await submit(this.ticketTypeForm, async () => {
      this.added.emit(this.data()());
    });
  }
}
