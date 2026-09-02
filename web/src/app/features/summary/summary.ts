import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { AnalyticsService } from './analytics-service';
import { OrganizerSummaryMetrics } from './models/analytics.models';
import { SalesTrendChart } from '@shared/components/sales-trend-chart/sales-trend-chart';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, SalesTrendChart, MatIconModule],
  templateUrl: './summary.html',
  styleUrl: './summary.css',
})
export class Summary implements OnInit {
  private analyticsService = inject(AnalyticsService);

  metrics = signal<OrganizerSummaryMetrics | null>(null);
  metricsLoading = signal(true);
  metricsError = signal(false);

  readonly trendLoader = (period: string) => this.analyticsService.getOrganizerSalesTrend(period);

  ngOnInit(): void {
    this.loadMetrics();
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
}
