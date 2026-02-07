import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

interface Zone {
  id: number;
  zone_name: string;
  city: string;
  is_active: boolean;
}

interface ParcelPoint {
  id: number;
  name: string;
  zone_id: number;
  agent_id: number;
  address_description: string;
  phone: string;
  is_active: boolean;
  opening_time: string;
  closing_time: string;
}

@Component({
  selector: 'app-new-delivery',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './new-delivery.html',
  styleUrl: './new-delivery.css',
})
export class NewDelivery implements OnInit {
  deliveryForm!: FormGroup;

  // Mock data - Replace with API calls
  zones = signal<Zone[]>([
    { id: 1, zone_name: 'Westlands', city: 'Nairobi', is_active: true },
    { id: 2, zone_name: 'Eastleigh', city: 'Nairobi', is_active: true },
    { id: 3, zone_name: 'CBD', city: 'Nairobi', is_active: true },
    { id: 4, zone_name: 'Kasarani', city: 'Nairobi', is_active: true },
  ]);

  parcelPoints = signal<ParcelPoint[]>([
    { id: 1, name: 'Sarit Center Duka', zone_id: 1, agent_id: 1, address_description: 'Next to Sarit Center', phone: '0712345678', is_active: true, opening_time: '08:00', closing_time: '20:00' },
    { id: 2, name: 'Westgate Mall Point', zone_id: 1, agent_id: 2, address_description: 'Westgate Shopping Mall', phone: '0723456789', is_active: true, opening_time: '09:00', closing_time: '21:00' },
    { id: 3, name: 'Eastleigh 1st Avenue', zone_id: 2, agent_id: 3, address_description: '1st Avenue, Section 1', phone: '0734567890', is_active: true, opening_time: '07:00', closing_time: '22:00' },
    { id: 4, name: 'CBD Koja Point', zone_id: 3, agent_id: 4, address_description: 'River Road', phone: '0745678901', is_active: true, opening_time: '08:00', closing_time: '19:00' },
    { id: 5, name: 'Kasarani Mwiki', zone_id: 4, agent_id: 5, address_description: 'Mwiki Road', phone: '0756789012', is_active: true, opening_time: '08:00', closing_time: '20:00' },
  ]);

  // Signals for tracking selected zones to trigger computed updates
  private fromZoneSignal = signal<number | null>(null);
  private toZoneSignal = signal<number | null>(null);

  // Filtered points based on selected zones
  fromPoints = computed(() => {
    const zoneId = this.fromZoneSignal();
    if (!zoneId) return [];
    return this.parcelPoints().filter(point => point.zone_id === zoneId && point.is_active);
  });

  toPoints = computed(() => {
    const zoneId = this.toZoneSignal();
    if (!zoneId) return [];
    return this.parcelPoints().filter(point => point.zone_id === zoneId && point.is_active);
  });

  // Delivery fee calculation (dummy for now)
  deliveryFee = computed(() => {
    const fromZone = this.fromZoneSignal();
    const toZone = this.toZoneSignal();

    if (!fromZone || !toZone) return 0;

    // Same zone
    if (fromZone === toZone) return 100;

    // Different zones - dummy pricing
    return 150;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit() {
    this.initializeForm();
  }

  initializeForm() {
    this.deliveryForm = this.fb.group({
      // Recipient details
      recipientName: ['', [Validators.required, Validators.minLength(2)]],
      recipientPhone: ['', [Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]],

      // From location
      fromZone: ['', Validators.required],
      fromPoint: ['', Validators.required],

      // To location
      toZone: ['', Validators.required],
      toPoint: ['', Validators.required],

      // Package details
      packageName: ['', [Validators.required, Validators.minLength(2)]],
      packagePrice: ['', [Validators.required, Validators.min(0)]],
      packageDescription: ['', [Validators.maxLength(500)]],

      // Cash on Delivery
      collectCash: [false],
      cashAmount: [{ value: '', disabled: true }]
    });

    // Enable/disable cash amount field based on checkbox
    this.deliveryForm.get('collectCash')?.valueChanges.subscribe(checked => {
      const cashAmountControl = this.deliveryForm.get('cashAmount');
      if (checked) {
        cashAmountControl?.enable();
        cashAmountControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        cashAmountControl?.disable();
        cashAmountControl?.clearValidators();
        cashAmountControl?.setValue('');
      }
      cashAmountControl?.updateValueAndValidity();
    });

    // Update fromZoneSignal when fromZone changes to trigger computed signal
    this.deliveryForm.get('fromZone')?.valueChanges.subscribe((value) => {
      this.fromZoneSignal.set(value);
      this.deliveryForm.get('fromPoint')?.setValue('');
    });

    // Update toZoneSignal when toZone changes to trigger computed signal
    this.deliveryForm.get('toZone')?.valueChanges.subscribe((value) => {
      this.toZoneSignal.set(value);
      this.deliveryForm.get('toPoint')?.setValue('');
    });
  }

  onSubmit() {
    if (this.deliveryForm.valid) {
      const deliveryData = {
        ...this.deliveryForm.value,
        deliveryFee: this.deliveryFee()
      };

      console.log('Delivery Order:', deliveryData);

      // Navigate to payment page with delivery data
      // You'll commit the order AFTER successful payment
      this.router.navigate(['/payment'], {
        state: { deliveryOrder: deliveryData }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.deliveryForm.controls).forEach(key => {
        this.deliveryForm.get(key)?.markAsTouched();
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const control = this.deliveryForm.get(fieldName);

    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('minlength')) {
      return `Minimum ${control.errors?.['minlength'].requiredLength} characters required`;
    }
    if (control?.hasError('pattern')) {
      return 'Invalid phone number format (07XXXXXXXX or 01XXXXXXXX)';
    }
    if (control?.hasError('min')) {
      return 'Value must be greater than 0';
    }
    if (control?.hasError('maxlength')) {
      return 'Maximum 500 characters allowed';
    }

    return '';
  }
}
