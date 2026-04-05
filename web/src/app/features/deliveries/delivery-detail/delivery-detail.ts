import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DeliveryService } from '@features/deliveries/services/delivery.service';
import { DeliveryResponse, DeliveryStatus } from '@features/deliveries/models/delivery.model';
import { AuthService } from '@core/services/auth-service';
import { SnackbarService } from '@core/services/snackbar-service';

@Component({
  selector: 'app-delivery-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './delivery-detail.html',
  styleUrl: './delivery-detail.css',
})
export class DeliveryDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private deliveryService = inject(DeliveryService);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  delivery = signal<DeliveryResponse | null>(null);
  isLoading = signal(true);
  isUpdating = signal(false);

  private readonly AGENT_ALLOWED = new Set([DeliveryStatus.AT_PICKUP_AGENT, DeliveryStatus.DELIVERED]);

  private readonly TRANSITIONS: Partial<Record<DeliveryStatus, DeliveryStatus[]>> = {
    [DeliveryStatus.PENDING]:               [DeliveryStatus.AT_PICKUP_AGENT, DeliveryStatus.CANCELLED],
    [DeliveryStatus.AT_PICKUP_AGENT]:       [DeliveryStatus.AT_HUB, DeliveryStatus.CANCELLED],
    [DeliveryStatus.AT_HUB]:               [DeliveryStatus.OUT_FOR_DELIVERY],
    [DeliveryStatus.OUT_FOR_DELIVERY]:     [DeliveryStatus.AT_DESTINATION_AGENT],
    [DeliveryStatus.AT_DESTINATION_AGENT]: [DeliveryStatus.DELIVERED],
  };

  ngOnInit() {
    const externalId = this.route.snapshot.paramMap.get('externalId') ?? '';
    this.deliveryService.getDeliveryById(externalId).subscribe({
      next: (d) => { this.delivery.set(d); this.isLoading.set(false); },
      error: () => { this.snackbarService.showError('Delivery not found'); this.router.navigate(['/deliveries']); }
    });
  }

  goBack() {
    this.router.navigate(['/deliveries']);
  }

  canUpdateStatus(): boolean {
    return this.authService.hasPermission('delivery:update');
  }

  getAvailableStatuses(): DeliveryStatus[] {
    const d = this.delivery();
    if (!d) return [];
    const next = this.TRANSITIONS[d.deliveryStatus] ?? [];
    const isAdmin = this.authService.getRole() === 'ADMIN';
    return isAdmin ? next : next.filter(s => this.AGENT_ALLOWED.has(s));
  }

  updateStatus(status: DeliveryStatus) {
    const d = this.delivery();
    if (!d || this.isUpdating()) return;
    this.isUpdating.set(true);
    this.deliveryService.updateDeliveryStatus(d.externalId, { status }).subscribe({
      next: (updated) => {
        this.delivery.set(updated);
        this.isUpdating.set(false);
        this.snackbarService.showSuccess('Status updated');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to update status';
        this.snackbarService.showError(msg);
        this.isUpdating.set(false);
      }
    });
  }

  getStatusActionLabel(status: DeliveryStatus): string {
    const labels: Partial<Record<DeliveryStatus, string>> = {
      [DeliveryStatus.AT_PICKUP_AGENT]:       'Confirm Drop-off',
      [DeliveryStatus.AT_HUB]:               'Mark at Hub',
      [DeliveryStatus.OUT_FOR_DELIVERY]:     'Dispatch',
      [DeliveryStatus.AT_DESTINATION_AGENT]: 'Mark at Destination',
      [DeliveryStatus.DELIVERED]:            'Mark Delivered',
      [DeliveryStatus.CANCELLED]:            'Cancel Delivery',
    };
    return labels[status] ?? status;
  }

  getStatusActionDescription(status: DeliveryStatus): string {
    const descriptions: Partial<Record<DeliveryStatus, string>> = {
      [DeliveryStatus.AT_PICKUP_AGENT]:       'The sender has dropped off the package at your agent point',
      [DeliveryStatus.AT_HUB]:               'The package has arrived at the sorting hub',
      [DeliveryStatus.OUT_FOR_DELIVERY]:     'The package has been dispatched for last-mile delivery',
      [DeliveryStatus.AT_DESTINATION_AGENT]: 'The package has arrived at the destination agent point',
      [DeliveryStatus.DELIVERED]:            'The package has been collected by the recipient',
      [DeliveryStatus.CANCELLED]:            'This delivery will be cancelled and cannot be undone',
    };
    return descriptions[status] ?? '';
  }

  isCancelStatus(status: DeliveryStatus): boolean {
    return status === DeliveryStatus.CANCELLED;
  }

  getDeliveryStatusLabel(status: DeliveryStatus): string {
    const labels: Record<DeliveryStatus, string> = {
      [DeliveryStatus.PENDING]: 'Pending',
      [DeliveryStatus.AT_PICKUP_AGENT]: 'At Pickup Agent',
      [DeliveryStatus.AT_HUB]: 'At Hub',
      [DeliveryStatus.OUT_FOR_DELIVERY]: 'Out for Delivery',
      [DeliveryStatus.AT_DESTINATION_AGENT]: 'At Destination Agent',
      [DeliveryStatus.DELIVERED]: 'Delivered',
      [DeliveryStatus.CANCELLED]: 'Cancelled',
    };
    return labels[status] ?? status;
  }

  getDeliveryStatusClass(status: DeliveryStatus): string {
    const map: Record<DeliveryStatus, string> = {
      [DeliveryStatus.PENDING]: 'status-pending',
      [DeliveryStatus.AT_PICKUP_AGENT]: 'status-in-transit',
      [DeliveryStatus.AT_HUB]: 'status-in-transit',
      [DeliveryStatus.OUT_FOR_DELIVERY]: 'status-out-for-delivery',
      [DeliveryStatus.AT_DESTINATION_AGENT]: 'at-destination-agent',
      [DeliveryStatus.DELIVERED]: 'status-delivered',
      [DeliveryStatus.CANCELLED]: 'status-cancelled',
    };
    return map[status] ?? '';
  }
}
