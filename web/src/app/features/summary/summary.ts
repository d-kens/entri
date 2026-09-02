import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AnalyticsService } from './analytics-service';
import { OrganizerSummaryMetrics } from './models/analytics.models';
import { SalesTrendChart } from '@shared/components/sales-trend-chart/sales-trend-chart';
import { PayoutAccountsService } from '@features/payouts/payout-accounts-service';
import { AuthService } from '@features/auth/auth-service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, SalesTrendChart, RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './summary.html',
  styleUrl: './summary.css',
})
export class Summary implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private payoutAccountsService = inject(PayoutAccountsService);
  private authService = inject(AuthService);

  metrics = signal<OrganizerSummaryMetrics | null>(null);
  metricsLoading = signal(true);
  metricsError = signal(false);
  hasPayoutAccount = signal(true);

  readonly trendLoader = (period: string) => this.analyticsService.getOrganizerSalesTrend(period);

  ngOnInit(): void {
    this.loadMetrics();
    this.checkPayoutAccount();
  }

  private loadMetrics(): void {
    this.metricsLoading.set(true);
    this.metricsError.set(false);
    this.analyticsService.getOrganizerSummaryMetrics().subscribe({
      next: (data) => {
        this.metrics.set(data);
        this.metricsLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load organizer metrics', err);
        this.metricsError.set(true);
        this.metricsLoading.set(false);
      },
    });
  }

  private checkPayoutAccount(): void {
    const organizerKey = this.authService.getExternalId();
    if (!organizerKey) return;
    this.payoutAccountsService.getAccounts(organizerKey).subscribe({
      next: (accounts) => this.hasPayoutAccount.set(accounts.length > 0),
      error: () => this.hasPayoutAccount.set(true),
    });
  }
}
