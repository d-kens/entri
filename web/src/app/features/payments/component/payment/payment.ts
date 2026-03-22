import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription } from 'rxjs';
import { PaymentService } from '@features/payments/services/payment.service';
import { InitiatePaymentRequest, PaymentStatus, PaymentType } from '@features/payments/models/payment.model';

@Component({
  selector: 'app-payment',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
  standalone: true
})
export class Payment {
  @Input({ required: true }) amount!: number;
  @Input({ required: true }) referenceId!: string;
  @Input({ required: true }) paymentType!: PaymentType;
  @Input() paymentDescription: string = 'Payment';

  @Output() completed = new EventEmitter<void>();
  @Output() failed = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private paymentService = inject(PaymentService);

  paymentForm: FormGroup;
  step = signal<'form' | 'waiting' | 'success' | 'failed'>('form');
  paymentMessage = signal<string>('');

  private paymentSubscription?: Subscription;

  constructor() {
    this.paymentForm = this.fb.group({
      phoneNumber: ['', [Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]]
    });
  }

  ngOnDestroy() {
    this.paymentSubscription?.unsubscribe();
  }

  submit() {
    if (this.paymentForm.invalid) {
      this.paymentForm.get('phoneNumber')?.markAsTouched();
      return;
    }

    this.step.set('waiting');

    const payload: InitiatePaymentRequest = {
      amount: this.amount,
      referenceId: this.referenceId,
      paymentType: this.paymentType,
      paymentMethod: 'MPESA',
      phoneNumber: this.paymentForm.get('phoneNumber')?.value,
      paymentDescription: this.paymentDescription
    };

    this.paymentService.initiatePayment(payload).subscribe({
      next: (response) => {
        this.listenForResult(response.paymentId);
      },
      error: (error) => {
        this.paymentMessage.set(error?.error?.message || 'Payment failed. Please try again.');
        this.step.set('failed');
      }
    });
  }

  private listenForResult(paymentId: string) {
    this.paymentSubscription = this.paymentService
      .subscribeToPaymentEvents(paymentId)
      .subscribe({
        next: (event) => {
          if (event.status === PaymentStatus.SUCCESS) {
            this.step.set('success');
            this.paymentMessage.set('Payment completed successfully!');
            setTimeout(() => this.completed.emit(), 1500);
          } else if (event.status === PaymentStatus.FAILED) {
            this.step.set('failed');
            this.paymentMessage.set('Payment failed. Please try again.');
            this.failed.emit();
          }
        },
        error: () => {
          this.step.set('failed');
          this.paymentMessage.set('Connection lost. Please check your payment status.');
          this.failed.emit();
        }
      });
  }

  retry() {
    this.paymentSubscription?.unsubscribe();
    this.step.set('form');
    this.paymentMessage.set('');
  }

  getErrorMessage(): string {
    const control = this.paymentForm.get('phoneNumber');
    if (control?.hasError('required')) return 'Phone number is required';
    if (control?.hasError('pattern')) return 'Invalid format (07XXXXXXXX or 01XXXXXXXX)';
    return '';
  }
}
