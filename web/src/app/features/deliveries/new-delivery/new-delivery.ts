import {Component, computed, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatStepperModule} from '@angular/material/stepper';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {ParcelPoint, Zone} from '@features/deliveries/models/delivery.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-new-delivery',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './new-delivery.html',
  styleUrl: './new-delivery.css',
})
export class NewDelivery implements OnInit{
  deliveryForm!: FormGroup

  zones = signal<Zone[]>([]);
  fromPoints = signal<ParcelPoint[]>([]);
  toPoints = signal<ParcelPoint[]>([]);

  deliveryFee = computed(() => {
    const from = this.deliveryForm?.get('from.parcelPoint')?.value;
    const to = this.deliveryForm?.get('to.parcelPoint')?.value;

    if (from && to) {
      // TODO: Calculate based on distance/zones
      return 150;
    }
    return 0;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit() {
    this.initializeForm();
    this.loadZones();
  }

  initializeForm() {
    this.deliveryForm = this.fb.group({
      recipient: this.fb.group({
        name: ['', [Validators.required, Validators.minLength(3)]],
        phoneNumber: ['', [Validators.required, Validators.pattern(/^(?:254|\+254|0)([17]\d{8})$/)]]
      }),
      from: this.fb.group({
        zone: [null, Validators.required],
        parcelPoint: [null, Validators.required]
      }),
      to: this.fb.group({
        zone: [null, Validators.required],
        parcelPoint: [null, Validators.required]
      }),
      parcelDetails: this.fb.group({
        name: ['', [Validators.required, Validators.minLength(3)]],
        price: [0, [Validators.required, Validators.min(0)]],
        description: ['', [Validators.maxLength(500)]]
      }),
      cashOnDelivery: this.fb.group({
        enabled: [false],
        amount: [{ value: 0, disabled: true }, [Validators.min(0)]]
      })
    });

    // Watch COD checkbox changes
    this.deliveryForm.get('cashOnDelivery.enabled')?.valueChanges.subscribe(enabled => {
      const amountControl = this.deliveryForm.get('cashOnDelivery.amount');
      if (enabled) {
        amountControl?.enable();
        amountControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        amountControl?.disable();
        amountControl?.setValue(0);
        amountControl?.clearValidators();
      }
      amountControl?.updateValueAndValidity();
    });

    // Watch zone changes to load parcel points
    this.deliveryForm.get('from.zone')?.valueChanges.subscribe(zone => {
      if (zone) {
        this.loadParcelPoints(zone.id, 'from');
      }
    });

    this.deliveryForm.get('to.zone')?.valueChanges.subscribe(zone => {
      if (zone) {
        this.loadParcelPoints(zone.id, 'to');
      }
    });
  }

  loadZones() {
    // TODO: Replace with actual API call
    this.zones.set([
      { id: 1, zoneName: 'Westlands', city: 'Nairobi', isActive: true },
      { id: 2, zoneName: 'CBD', city: 'Nairobi', isActive: true },
      { id: 3, zoneName: 'Eastleigh', city: 'Nairobi', isActive: true },
      { id: 4, zoneName: 'Karen', city: 'Nairobi', isActive: true },
    ]);
  }

  loadParcelPoints(zoneId: number, direction: 'from' | 'to') {
    // TODO: Replace with actual API call
    const mockPoints: ParcelPoint[] = [
      {
        id: 1,
        name: 'Sarit Center Duka',
        zoneId: zoneId,
        agentId: 1,
        latitude: -1.2601,
        longitude: 36.7839,
        addressDescription: 'Sarit Center, Ground Floor',
        isActive: true,
        phone: '0712345678',
        openingTime: '08:00',
        closingTime: '20:00'
      },
      {
        id: 2,
        name: 'Westgate Mall Shop',
        zoneId: zoneId,
        agentId: 2,
        latitude: -1.2651,
        longitude: 36.8039,
        addressDescription: 'Westgate Mall, Shop 12',
        isActive: true,
        phone: '0723456789',
        openingTime: '09:00',
        closingTime: '21:00'
      }
    ];

    if (direction === 'from') {
      this.fromPoints.set(mockPoints);
    } else {
      this.toPoints.set(mockPoints);
    }
  }

  proceedToPayment() {
    if (this.deliveryForm.valid) {
      const orderData = {
        ...this.deliveryForm.value,
        deliveryFee: this.deliveryFee()
      };

      // Store order data in session/service
      sessionStorage.setItem('pendingOrder', JSON.stringify(orderData));

      // Navigate to payment
      this.router.navigate(['/payment'], {
        state: { orderData, deliveryFee: this.deliveryFee() }
      });
    }
  }
}
