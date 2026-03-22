import {Component, computed, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatRadioModule} from '@angular/material/radio';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {DeliveryService} from '@features/deliveries/services/delivery.service';
import {DeliveryResponse} from '@features/deliveries/models/delivery.model';
import {SnackbarService} from '@core/services/snackbar-service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {PaymentService} from '@features/payments/services/payment.service';
import {InitiatePaymentRequest, PaymentStatus, PaymentType} from '@features/payments/models/payment.model';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-delivery-payment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './delivery-payment.html',
  styleUrl: './delivery-payment.css',
})
export class DeliveryPayment implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private paymentService = inject(PaymentService);
  private snackbarService = inject(SnackbarService);
  private deliveryService = inject(DeliveryService);

  deliveryId = signal<string>('');
  delivery = signal<DeliveryResponse | null>(null);
  isLoading = signal(true);
  isProcessingPayment = signal(false);
  paymentInitiated = signal(false);
  paymentStatus = signal<'waiting' | 'success' | 'failed' | null>(null);
  paymentMessage = signal<string>('');

  paymentForm!: FormGroup;
  private paymentSubscription?: Subscription;

  paymentMethods = [
    { value: 'MPESA', label: 'M-Pesa', icon: 'phone_android', description: 'Pay via M-Pesa STK Push' },
  ];

  totalAmount = computed(() => {
    const del = this.delivery();
    if (!del) return 0;
    return del.deliveryFee;
  });

  ngOnInit() {
    this.deliveryId.set(this.route.snapshot.paramMap.get('id') || '');
    this.initializeForm();
    this.loadDelivery();
  }

  ngOnDestroy() {
    this.paymentSubscription?.unsubscribe();
  }

  initializeForm() {
    this.paymentForm = this.fb.group({
      paymentMethod: ['MPESA', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]]
    });

    const paymentMethodControl = this.paymentForm.controls['paymentMethod'];
    const phoneControl = this.paymentForm.controls['phoneNumber'];

    paymentMethodControl.valueChanges.subscribe((method: string) => {
      if (method === 'MPESA') {
        phoneControl.setValidators([Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]);
      } else {
        phoneControl.clearValidators();
      }
      phoneControl.updateValueAndValidity();
    });
  }

  loadDelivery() {
    this.isLoading.set(true);
    this.deliveryService.getDeliveryById(this.deliveryId()).subscribe({
      next: (delivery) => {
        this.delivery.set(delivery);
        this.isLoading.set(false);

        if (delivery.paymentStatus === PaymentStatus.SUCCESS) {
          this.snackbarService.showInfo('This delivery has already been paid for');
          this.router.navigate(['/deliveries', delivery.externalId]);
        }
      },
      error: (error) => {
        console.error('Error loading delivery:', error);
        this.snackbarService.showError('Failed to load delivery details');
        this.isLoading.set(false);
        this.router.navigate(['/deliveries']);
      }
    });
  }

  processPayment() {
    if (this.paymentForm.invalid) {
      Object.keys(this.paymentForm.controls).forEach(key => {
        this.paymentForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isProcessingPayment.set(true);
    this.paymentInitiated.set(true);
    this.paymentStatus.set('waiting');

    const paymentPayload: InitiatePaymentRequest = {
      // amount: this.totalAmount(),
      amount: 1, // TODO: Remove hard coded amount
      referenceId: this.deliveryId(),
      paymentType: PaymentType.DELIVERY_FEE,
      paymentMethod: this.paymentForm.get('paymentMethod')?.value,
      phoneNumber: this.paymentForm.get('phoneNumber')?.value || undefined,
      paymentDescription: 'Oro Delivery Fee Payment'
    };

    this.paymentService.initiatePayment(paymentPayload).subscribe({
      next: (response) => {
        this.snackbarService.showSuccess('STK push sent to your phone');
        this.subscribeToPaymentStatus(response.paymentId);
      },
      error: (error) => {
        console.error('Payment error:', error);
        const errorMessage = error?.error?.message || 'Payment failed. Please try again';
        this.snackbarService.showError(errorMessage);
        this.isProcessingPayment.set(false);
        this.paymentInitiated.set(false);
        this.paymentStatus.set(null);
      }
    });
  }

  subscribeToPaymentStatus(paymentId: string) {
    this.paymentSubscription = this.paymentService.subscribeToPaymentEvents(paymentId).subscribe({
      next: (event) => {
        console.log('Payment event received:', event);

        if (event.status === PaymentStatus.SUCCESS) {
          this.paymentStatus.set('success');
          this.paymentMessage.set('Payment completed successfully!');
          this.isProcessingPayment.set(false);
          this.snackbarService.showSuccess('Payment successful!');

          setTimeout(() => {
            this.router.navigate(['/deliveries']);
          }, 1000);
        } else if (event.status === PaymentStatus.FAILED) {
          this.paymentStatus.set('failed');
          this.paymentMessage.set('Payment failed');
          this.isProcessingPayment.set(false);
          this.snackbarService.showError('Payment failed');
        }
      },
      error: (error) => {
        console.error('Payment event error:', error);
        this.paymentStatus.set('failed');
        this.paymentMessage.set('Connection lost. Please check your payment status.');
        this.isProcessingPayment.set(false);
      },
      complete: () => {
        console.log('Payment event stream completed');
      }
    });
  }

  retryPayment() {
    this.paymentInitiated.set(false);
    this.paymentStatus.set(null);
    this.paymentMessage.set('');
    this.paymentSubscription?.unsubscribe();
  }

  getErrorMessage(fieldName: string): string {
    const control = this.paymentForm.get(fieldName);

    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('pattern')) {
      return 'Invalid phone number format (07XXXXXXXX or 01XXXXXXXX)';
    }

    return '';
  }
}
