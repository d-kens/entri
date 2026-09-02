import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AnalyticsService } from '@features/summary/analytics-service';
import { PlatformSummaryMetrics } from '@features/summary/models/analytics.models';
import { SalesTrendChart } from '@shared/components/sales-trend-chart/sales-trend-chart';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [DecimalPipe, SalesTrendChart],
  templateUrl: './reports.html',
})
export class Reports implements OnInit {
  private analyticsService = inject(AnalyticsService);

  metrics = signal<PlatformSummaryMetrics | null>(null);
  metricsLoading = signal(true);
  metricsError = signal(false);

  readonly trendLoader = (period: string) => this.analyticsService.getPlatformSalesTrend(period);

  ngOnInit(): void {
    this.loadMetrics();
  }

  private loadMetrics(): void {
    this.metricsLoading.set(true);
    this.metricsError.set(false);
    this.analyticsService.getPlatformSummaryMetrics().subscribe({
      next: (data) => {
        this.metrics.set(data);
        this.metricsLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load platform metrics', err);
        this.metricsError.set(true);
        this.metricsLoading.set(false);
      },
    });
  }
}
