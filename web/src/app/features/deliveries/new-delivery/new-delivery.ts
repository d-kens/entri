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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ZonesService, Zone, Agent } from '@core/services/zones-service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

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
    MatProgressSpinnerModule,
    MatAutocompleteModule
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
  agents = signal<Agent[]>([]);

  // Filtered zones for autocomplete
  filteredFromZones = signal<Zone[]>([]);
  filteredToZones = signal<Zone[]>([]);

  // Search subjects for client-side filtering
  fromZoneSearch$ = new Subject<string>();
  toZoneSearch$ = new Subject<string>();

  loadingZones = signal(true);
  loadingFromAgents = signal(false);
  loadingToAgents = signal(false);

  fromPoints = computed(() => {
    const zoneId = this.fromZoneSignal();
    if (!zoneId) return [];
    return this.agents().filter(agent => agent.zoneId === zoneId && agent.isActive);
  });

  toPoints = computed(() => {
    const zoneId = this.toZoneSignal();
    if (!zoneId) return [];
    return this.agents().filter(agent => agent.zoneId === zoneId && agent.isActive);
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
    this.setupZoneSearch();
  }

  loadZones() {
    this.loadingZones.set(true);
    this.zonesService.getAllZones().subscribe({
      next: (zones) => {
        this.zones.set(zones);
        this.filteredFromZones.set(zones);
        this.filteredToZones.set(zones);
        this.loadingZones.set(false);
      },
      error: (error) => {
        console.error('Error loading zones:', error);
        this.loadingZones.set(false);
      }
    });
  }

  setupZoneSearch() {
    this.fromZoneSearch$.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchTerm => {
      this.filterFromZones(searchTerm);
    });

    this.toZoneSearch$.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchTerm => {
      this.filterToZones(searchTerm);
    });
  }

  private filterFromZones(searchTerm: string) {
    if (!searchTerm || searchTerm.trim() === '') {
      this.filteredFromZones.set(this.zones());
      return;
    }

    const search = searchTerm.toLowerCase().trim();
    const filtered = this.zones().filter(zone =>
      zone.zoneName.toLowerCase().includes(search) ||
      zone.city.toLowerCase().includes(search)
    );

    this.filteredFromZones.set(filtered);
  }

  private filterToZones(searchTerm: string) {
    if (!searchTerm || searchTerm.trim() === '') {
      this.filteredToZones.set(this.zones());
      return;
    }

    const search = searchTerm.toLowerCase().trim();
    const filtered = this.zones().filter(zone =>
      zone.zoneName.toLowerCase().includes(search) ||
      zone.city.toLowerCase().includes(search)
    );

    this.filteredToZones.set(filtered);
  }

  onFromZoneSearch(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.fromZoneSearch$.next(value);
  }

  onToZoneSearch(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.toZoneSearch$.next(value);
  }

  displayZone = (zoneId: number): string => {
    if (!zoneId) return '';
    const zone = this.zones().find(z => z.id === zoneId);
    return zone ? `${zone.zoneName} - ${zone.city}` : '';
  };

  loadParcelPointsForZone(zoneId: number, isFromZone: boolean) {
    if (!zoneId) return;

    if (isFromZone) {
      this.loadingFromAgents.set(true);
    } else {
      this.loadingToAgents.set(true);
    }

    this.zonesService.getAgentsByZone(zoneId).subscribe({
      next: (agents) => {
        this.agents.set(agents);

        if (isFromZone) {
          this.loadingFromAgents.set(false);
        } else {
          this.loadingToAgents.set(false);
        }
      },
      error: (error) => {
        console.error('Error loading parcel points:', error);
        if (isFromZone) {
          this.loadingFromAgents.set(false);
        } else {
          this.loadingToAgents.set(false);
        }
      }
    });
  }

  initializeForm() {
    this.deliveryForm = this.fb.group({
      recipientName: ['', [Validators.required, Validators.minLength(2)]],
      recipientPhone: ['', [Validators.required, Validators.pattern(/^(07|01)\d{8}$/)]],

      fromZone: ['', Validators.required],
      fromPoint: ['', Validators.required],

      toZone: ['', Validators.required],
      toPoint: ['', Validators.required],

      packageName: ['', [Validators.required, Validators.minLength(2)]],
      packagePrice: ['', [Validators.required, Validators.min(0)]],
      packageDescription: ['', [Validators.maxLength(500)]],

      collectCash: [false],
      cashAmount: [{ value: '', disabled: true }]
    });

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

    this.deliveryForm.get('fromZone')?.valueChanges.subscribe((value) => {
      if (typeof value === 'number') {
        this.fromZoneSignal.set(value);
        this.deliveryForm.get('fromPoint')?.setValue('');
        this.loadParcelPointsForZone(value, true);
      }
    });

    this.deliveryForm.get('toZone')?.valueChanges.subscribe((value) => {
      if (typeof value === 'number') {
        this.toZoneSignal.set(value);
        this.deliveryForm.get('toPoint')?.setValue('');
        this.loadParcelPointsForZone(value, false);
      }
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
      return 'Invalid phone number format';
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
