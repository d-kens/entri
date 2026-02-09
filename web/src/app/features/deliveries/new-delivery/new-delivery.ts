import { Component, OnInit, signal, computed, inject } from '@angular/core';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ZonesService, Zone, ParcelPoint } from '@core/services/zones-service';

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
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './new-delivery.html',
  styleUrl: './new-delivery.css',
})
export class NewDelivery implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private zonesService = inject(ZonesService);

  deliveryForm!: FormGroup;

  zones = signal<Zone[]>([]);
  parcelPoints = signal<ParcelPoint[]>([]);

  loadingZones = signal(true);
  loadingFromPoints = signal(false);
  loadingToPoints = signal(false);

  fromPoints = computed(() => {
    const zoneId = this.fromZoneSignal();
    if (!zoneId) return [];
    return this.parcelPoints().filter(point => point.zoneId === zoneId && point.isActive);
  });

  toPoints = computed(() => {
    const zoneId = this.toZoneSignal();
    if (!zoneId) return [];
    return this.parcelPoints().filter(point => point.zoneId === zoneId && point.isActive);
  });

  deliveryFee = computed(() => {
    const fromZone = this.fromZoneSignal();
    const toZone = this.toZoneSignal();

    if (!fromZone || !toZone) return 0;

    // Same zone
    if (fromZone === toZone) return 100;

    // Different zones - dummy pricing
    return 150;
  });

  // Signals for tracking selected zones to trigger computed updates
  private fromZoneSignal = signal<number | null>(null);
  private toZoneSignal = signal<number | null>(null);

  ngOnInit() {
    this.loadZones();
    this.initializeForm();
  }

  loadZones() {
    this.loadingZones.set(true);
    this.zonesService.getAllZones(0, 100).subscribe({
      next: (response) => {
        this.zones.set(response.content);
        this.loadingZones.set(false);
      },
      error: (error) => {
        console.error('Error loading zones:', error);
        this.loadingZones.set(false);
      }
    });
  }

  loadParcelPointsForZone(zoneId: number, isFromZone: boolean) {
    if (isFromZone) {
      this.loadingFromPoints.set(true);
    } else {
      this.loadingToPoints.set(true);
    }

    this.zonesService.getParcelPointsByZone(zoneId, 0, 100).subscribe({
      next: (response) => {
        this.parcelPoints.set(response.content);

        if (isFromZone) {
          this.loadingFromPoints.set(false);
        } else {
          this.loadingToPoints.set(false);
        }
      },
      error: (error) => {
        console.error('Error loading parcel points:', error);
        if (isFromZone) {
          this.loadingFromPoints.set(false);
        } else {
          this.loadingToPoints.set(false);
        }
      }
    });
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
      this.loadParcelPointsForZone(value, true);
    });

    // Update toZoneSignal when toZone changes to trigger computed signal
    this.deliveryForm.get('toZone')?.valueChanges.subscribe((value) => {
      this.toZoneSignal.set(value);
      this.deliveryForm.get('toPoint')?.setValue('');
      this.loadParcelPointsForZone(value, false);
    });
  }

  onSubmit() {
    if (this.deliveryForm.valid) {
      const deliveryData = {
        ...this.deliveryForm.value,
        deliveryFee: this.deliveryFee()
      };

      console.log('Delivery Order:', deliveryData);

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
