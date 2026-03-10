import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {DeliveryService} from '@features/deliveries/services/delivery.service';
import {catchError, of} from 'rxjs';
import {TrackDeliveryResponse} from '@features/deliveries/models/delivery.model';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './track-delivery.html',
  styleUrl: './track-delivery.css'
})
export class TrackDelivery {
  private fb = inject(FormBuilder);
  private deliveryService = inject(DeliveryService)

  trackingForm: FormGroup;
  isSearching = signal(false);
  trackingInfo = signal< TrackDeliveryResponse | null>(null);
  notFound = signal(false);

  constructor() {
    this.trackingForm = this.fb.group({
      trackingNumber: ['', [Validators.required, Validators.minLength(10)]]
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

    const trackingNumber = this.trackingForm.get('trackingNumber')?.value.trim().toUpperCase();

    this.deliveryService.trackDelivery(trackingNumber).pipe(
      catchError((error) => {
        this.notFound.set(true);
        this.isSearching.set(false);
        return of(null);
      })
    ).subscribe({
      next: (data) => {
        if (data) {
          this.trackingInfo.set(data);
        }
        this.isSearching.set(false);
      }
    });
  }

  reset() {
    this.trackingForm.reset();
    this.trackingInfo.set(null);
    this.notFound.set(false);
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
}
