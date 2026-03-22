import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {DeliveryService} from '@features/deliveries/services/delivery.service';
import {catchError, of} from 'rxjs';
import {DeliveryStatus, TrackDeliveryResponse} from '@features/deliveries/models/delivery.model';
import {PaymentType} from '@features/payments/models/payment.model';
import {Payment} from '@features/payments/component/payment/payment';

@Component({
  selector: 'app-track-delivery',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    Payment
  ],
  templateUrl: './track-delivery.html',
  styleUrl: './track-delivery.css'
})
export class TrackDelivery {
  private fb = inject(FormBuilder);
  private deliveryService = inject(DeliveryService);

  PaymentType = PaymentType;

  trackingForm: FormGroup;
  isSearching = signal(false);
  trackingInfo = signal<TrackDeliveryResponse | null>(null);
  notFound = signal(false);
  showPaymentForm = signal(false);
  collectionPaid = signal(false);

  constructor() {
    this.trackingForm = this.fb.group({
      trackingNumber: ['', [
        Validators.required,
        Validators.pattern(/^(PAR|ORO)-\d{12}-[A-Z0-9]{4}$/)
      ]]
    });
  }

  trackPackage() {
    if (this.trackingForm.invalid) {
      this.trackingForm.get('trackingNumber')?.markAsTouched();
      return;
    }

    this.isSearching.set(true);
    this.trackingInfo.set(null);
    this.notFound.set(false);
    this.showPaymentForm.set(false);
    this.collectionPaid.set(false);

    const trackingNumber = this.trackingForm.get('trackingNumber')?.value
      .trim()
      .toUpperCase()
      .replace(/\s+/g, '');

    this.deliveryService.trackDelivery(trackingNumber).pipe(
      catchError(() => {
        this.notFound.set(true);
        this.isSearching.set(false);
        return of(null);
      })
    ).subscribe({
      next: (data) => {
        if (data) this.trackingInfo.set(data);
        this.isSearching.set(false);
      }
    });
  }

  canPay = computed(() => {
    const info = this.trackingInfo();
    if (!info) return false;
    return info.deliveryStatus === DeliveryStatus.DELIVERED && !info.isCashCollected;
  });

  onPaymentCompleted() {
    this.collectionPaid.set(true);
    this.showPaymentForm.set(false);
  }

  onPaymentFailed() {
    // PaymentFlow handles its own retry UI — nothing to do here
  }

  reset() {
    this.trackingForm.reset();
    this.trackingInfo.set(null);
    this.notFound.set(false);
    this.showPaymentForm.set(false);
    this.collectionPaid.set(false);
  }

  getStatusLabel(status: string): string {
    const statusMap: Record<string, string> = {
      'PENDING': 'Pending',
      'DROPPED_AT_PICKUP_AGENT': 'At Pickup Point',
      'AT_HUB': 'At Hub',
      'OUT_FOR_DELIVERY': 'Out for Delivery',
      'DELIVERED': 'Delivered'
    };
    return statusMap[status] || status;
  }

  getErrorMessage(): string {
    const control = this.trackingForm.get('trackingNumber');
    if (control?.hasError('required')) return 'Tracking number is required';
    if (control?.hasError('pattern')) return 'Invalid tracking number format (e.g., PAR-260304200856-DZ47)';
    return '';
  }
}
