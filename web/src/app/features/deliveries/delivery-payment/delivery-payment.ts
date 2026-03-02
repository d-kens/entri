import { Component, OnInit, OnDestroy, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DeliveryService, PaymentEvent } from '@features/deliveries/services/delivery.service';
import { DeliveryResponse } from '@features/deliveries/models/delivery.model';
import { SnackbarService } from '@core/services/snackbar-service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

type PaymentMethod = 'MPESA' | 'CARD' | 'WALLET';

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
export class DeliveryPayment implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private deliveryService = inject(DeliveryService);
  private snackbarService = inject(SnackbarService);

  deliveryId = signal<string>('');
  delivery = signal<DeliveryResponse | null>(null);
  isLoading = signal(true);
  isProcessingPayment = signal(false);

  paymentForm!: FormGroup;

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

  initializeForm() {
    this.paymentForm = this.fb.group({
      paymentMethod: ['MPESA', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]]
    });

    this.paymentForm.get('paymentMethod')?.valueChanges.subscribe(method => {
      const phoneControl = this.paymentForm.get('phoneNumber');
      if (method === 'MPESA') {
        phoneControl?.setValidators([Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]);
      } else {
        phoneControl?.clearValidators();
      }
      phoneControl?.updateValueAndValidity();
    });
  }

  loadDelivery() {
    this.isLoading.set(true);
    this.deliveryService.getDeliveryById(this.deliveryId()).subscribe({
      next: (delivery) => {
        this.delivery.set(delivery);
        this.isLoading.set(false);

        if (delivery.paymentStatus === 'PAID') {
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

    this.isProcessingPayment.set(true);

    const paymentPayload = {
      reference: this.deliveryId(),
      paymentMethod: this.paymentForm.value.paymentMethod,
      phoneNumber: this.paymentForm.value.phoneNumber || undefined,
      amount: this.totalAmount()
    };

    this.deliveryService.initiatePayment(paymentPayload).subscribe({
      next: (response) => {
        if (this.paymentForm.value.paymentMethod === 'MPESA') {
          this.snackbarService.showSuccess('STK push sent! Please enter your M-Pesa PIN');
          this.subscribeToPaymentStatus();
        } else {
          this.isProcessingPayment.set(false);
          this.snackbarService.showSuccess('Payment successful!');
          this.router.navigate(['/deliveries', this.deliveryId()]);
        }
      },
      error: (error) => {
        console.error('Payment error:', error);
        const errorMessage = error?.error?.message || 'Payment failed. Please try again';
        this.snackbarService.showError(errorMessage);
        this.isProcessingPayment.set(false);
      }
    });
  }

  subscribeToPaymentStatus() {} // TODO:

  cancelPayment() {
    this.router.navigate(['/deliveries']);
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
