import {
  Component,
  inject,
  OnInit,
  signal,
  AfterViewInit,
  ElementRef,
  ViewChild,
  OnDestroy,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { FormsModule } from '@angular/forms';
import { AnalyticsService } from './analytics-service';
import { OrganizerSummaryMetrics, SalesTrendDataPoint } from './models/analytics.models';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Filler,
  Tooltip,
} from 'chart.js';

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Filler,
  Tooltip,
);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, MatIconModule, MatButtonToggleModule, FormsModule],
  templateUrl: './summary.html',
  styleUrl: './summary.css',
})
export class Summary implements OnInit, AfterViewInit, OnDestroy {
  private analyticsService = inject(AnalyticsService);

  @ViewChild('trendChart') chartCanvas!: ElementRef<HTMLCanvasElement>;

  metrics = signal<OrganizerSummaryMetrics | null>(null);
  metricsLoading = signal(true);
  metricsError = signal(false);

  trendLoading = signal(true);
  trendError = signal(false);
  selectedPeriod = '30d';

  private chart: Chart | null = null;
  private trendData: SalesTrendDataPoint[] = [];

  ngOnInit(): void {
    this.loadMetrics();
    this.loadTrend();
  }

  ngAfterViewInit(): void {
    this.initChart();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  onPeriodChange(): void {
    this.loadTrend();
  }

  private loadMetrics(): void {
    this.metricsLoading.set(true);
    this.metricsError.set(false);
    this.analyticsService.getOrganizerSummaryMetrics().subscribe({
      next: (data) => {
        this.metrics.set(data);
        this.metricsLoading.set(false);
      },
      error: () => {
        this.metricsError.set(true);
        this.metricsLoading.set(false);
      },
    });
  }

  private loadTrend(): void {
    this.trendLoading.set(true);
    this.trendError.set(false);
    this.analyticsService.getOrganizerSalesTrend(this.selectedPeriod).subscribe({
      next: (res) => {
        this.trendData = res.data;
        this.trendLoading.set(false);
        this.updateChart();
      },
      error: () => {
        this.trendError.set(true);
        this.trendLoading.set(false);
      },
    });
  }

  private initChart(): void {
    const ctx = this.chartCanvas?.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chart = new Chart(ctx, {
      type: 'line',
      data: { labels: [], datasets: [this.revenueDataset()] },
      options: this.chartOptions(),
    });

    if (this.trendData.length) this.updateChart();
  }

  private updateChart(): void {
    if (!this.chart) return;
    this.chart.data.labels = this.trendData.map((d) => d.date);
    this.chart.data.datasets[0].data = this.trendData.map((d) => d.revenue);
    this.chart.update();
  }

  private revenueDataset() {
    return {
      label: 'Revenue',
      data: [] as number[],
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99,102,241,0.1)',
      fill: true,
      tension: 0.4,
      pointRadius: 3,
    };
  }

  private chartOptions() {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { tooltip: { mode: 'index' as const, intersect: false } },
      scales: {
        x: { grid: { display: false } },
        y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
      },
    };
  }
}
