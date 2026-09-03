import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AnalyticsService } from './analytics-service';
import { OrganizerSummaryMetrics } from './models/analytics.models';
import { WalletService } from '@features/wallet/wallet-service';
import { AuthService } from '@features/auth/auth-service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, MatIconModule, RouterLink],
  templateUrl: './summary.html',
  styleUrl: './summary.css',
})
export class Summary implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private walletService = inject(WalletService);
  private authService = inject(AuthService);

  metrics = signal<OrganizerSummaryMetrics | null>(null);
  metricsLoading = signal(true);
  metricsError = signal(false);

  walletBalance = signal<number | null>(null);
  walletLoading = signal(true);

  ngOnInit(): void {
    this.loadMetrics();
    this.loadWallet();
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

  private loadWallet(): void {
    const key = this.authService.getExternalId();
    if (!key) return;
    this.walletService.getWallet(key).subscribe({
      next: (w) => {
        this.walletBalance.set(w.balance);
        this.walletLoading.set(false);
      },
      error: () => this.walletLoading.set(false),
    });
  }
}
