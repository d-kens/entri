import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DeliveryService } from '@features/deliveries/services/delivery.service';
import { DeliveryResponse, DeliveryStatus } from '@features/deliveries/models/delivery.model';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import {PaymentStatus} from '@features/payments/models/payment.model';

@Component({
  selector: 'app-deliveries-list',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatTooltipModule
  ],
  templateUrl: './deliveries-list.html',
  styleUrl: './deliveries-list.css',
  standalone: true
})
export class DeliveriesList implements OnInit {
  private router = inject(Router);
  private deliveryService = inject(DeliveryService);

  Math = Math;

  deliveries = signal<DeliveryResponse[]>([]);
  isLoading = signal(true);

  pageIndex = signal(0);
  pageSize = signal(20);
  totalElements = signal(0);

  searchControl = new FormControl('');
  statusFilter = signal<string>('');

  statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'DROPPED_AT_PICKUP_AGENT', label: 'At Pickup Agent' },
    { value: 'AT_HUB', label: 'At Hub' },
    { value: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
    { value: 'DELIVERED', label: 'Delivered' },
    { value: 'CANCELLED', label: 'Cancelled' }
  ];

  ngOnInit() {
    this.loadDeliveries();
    this.setupSearchListener();
  }

  setupSearchListener() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.pageIndex.set(0);
        this.loadDeliveries();
      });
  }

  loadDeliveries() {
    this.isLoading.set(true);

    this.deliveryService.getDeliveries({
      page: this.pageIndex(),
      size: this.pageSize(),
      status: this.statusFilter(),
      search: this.searchControl.value || undefined
    }).subscribe({
      next: (response) => {
        this.deliveries.set(response.content);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading deliveries:', error);
        this.isLoading.set(false);
      }
    });
  }

  onStatusFilterChange(status: string) {
    this.statusFilter.set(status);
    this.pageIndex.set(0);
    this.loadDeliveries();
  }

  onPageChange(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadDeliveries();
  }

  viewDelivery(delivery: DeliveryResponse) {
    this.router.navigate(['/deliveries', delivery.externalId]);
  }

  editDelivery(event: Event, delivery: DeliveryResponse) {
    event.stopPropagation();
    this.router.navigate(['/deliveries', delivery.externalId, 'edit']);
  }

  createNewDelivery() {
    this.router.navigate(['/deliveries/new']);
  }

  getDeliveryStatusClass(status: DeliveryStatus): string {
    const statusMap: Record<DeliveryStatus, string> = {
      [DeliveryStatus.PENDING]: 'status-pending',
      [DeliveryStatus.DROPPED_AT_PICKUP_AGENT]: 'status-in-transit',
      [DeliveryStatus.AT_HUB]: 'status-in-transit',
      [DeliveryStatus.OUT_FOR_DELIVERY]: 'status-out-for-delivery',
      [DeliveryStatus.DELIVERED]: 'status-delivered',
      [DeliveryStatus.CANCELLED]: 'status-cancelled'
    };
    return statusMap[status] || '';
  }

  getDeliveryStatusLabel(status: DeliveryStatus): string {
    const statusMap: Record<DeliveryStatus, string> = {
      [DeliveryStatus.PENDING]: 'Pending',
      [DeliveryStatus.DROPPED_AT_PICKUP_AGENT]: 'At Pickup',
      [DeliveryStatus.AT_HUB]: 'At Hub',
      [DeliveryStatus.OUT_FOR_DELIVERY]: 'Out for Delivery',
      [DeliveryStatus.DELIVERED]: 'Delivered',
      [DeliveryStatus.CANCELLED]: 'Cancelled'
    };
    return statusMap[status] || status;
  }

  getPaymentStatusClass(status: PaymentStatus): string {
    const statusMap: Record<PaymentStatus, string> = {
      [PaymentStatus.PENDING]: 'payment-pending',
      [PaymentStatus.PAID]: 'payment-paid',
      [PaymentStatus.FAILED]: 'payment-failed',
      [PaymentStatus.REFUNDED]: 'payment-refunded'
    };
    return statusMap[status] || '';
  }
}
