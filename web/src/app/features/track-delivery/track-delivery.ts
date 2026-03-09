import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

interface TrackingInfo {
  trackingNumber: string;
  packageName: string;
  status: string;
  recipientName: string;
  recipientPhone: string;
  fromZone: string;
  toZone: string;
  timeline: {
    status: string;
    timestamp: string;
    location: string;
    completed: boolean;
  }[];
}

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

  trackingForm: FormGroup;
  isSearching = signal(false);
  trackingInfo = signal<TrackingInfo | null>(null);
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

    // Mock API call
    setTimeout(() => {
      const trackingNumber = this.trackingForm.get('trackingNumber')?.value;

      if (trackingNumber.toLowerCase().includes('notfound')) {
        this.notFound.set(true);
        this.isSearching.set(false);
        return;
      }

      const mockData: TrackingInfo = {
        trackingNumber: trackingNumber,
        packageName: 'Electronics Package',
        status: 'OUT_FOR_DELIVERY',
        recipientName: 'John Kamau',
        recipientPhone: '0712 345 678',
        fromZone: 'Westlands',
        toZone: 'Kilimani',
        timeline: [
          {
            status: 'Package Received',
            timestamp: 'Jan 15, 10:30 AM',
            location: 'Westlands Pickup Point',
            completed: true
          },
          {
            status: 'In Transit to Hub',
            timestamp: 'Jan 15, 11:45 AM',
            location: 'Main Sorting Hub',
            completed: true
          },
          {
            status: 'Out for Delivery',
            timestamp: 'Jan 15, 4:30 PM',
            location: 'Kilimani Delivery Hub',
            completed: true
          },
          {
            status: 'Delivered',
            timestamp: 'Pending',
            location: 'Kilimani',
            completed: false
          }
        ]
      };

      this.trackingInfo.set(mockData);
      this.isSearching.set(false);
    }, 1200);
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
