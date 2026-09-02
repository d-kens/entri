import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { Observable } from 'rxjs';
import {
  Chart,
  CategoryScale,
  Filler,
  LineController,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from 'chart.js';
import { SalesTrendDataPoint, SalesTrendResponse } from '@features/summary/models/analytics.models';

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
  selector: 'app-sales-trend-chart',
  standalone: true,
  imports: [FormsModule, MatButtonToggleModule],
  templateUrl: './sales-trend-chart.html',
})
export class SalesTrendChart implements OnInit, AfterViewInit, OnDestroy {
  @Input({ required: true }) trendLoader!: (period: string) => Observable<SalesTrendResponse>;
  @Input() title = 'Revenue Trend';

  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  loading = signal(true);
  error = signal(false);
  selectedPeriod = '30d';

  private chart: Chart | null = null;
  private trendData: SalesTrendDataPoint[] = [];

  ngOnInit(): void {
    this.load();
  }

  ngAfterViewInit(): void {
    this.initChart();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  onPeriodChange(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.trendLoader(this.selectedPeriod).subscribe({
      next: (res) => {
        this.trendData = res.data;
        this.loading.set(false);
        this.updateChart();
      },
      error: (err) => {
        console.error('Failed to load trend data', err);
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  private initChart(): void {
    const ctx = this.chartCanvas?.nativeElement.getContext('2d');
    if (!ctx) return;
    this.chart = new Chart(ctx, {
      type: 'line',
      data: { labels: [], datasets: [this.dataset()] },
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

  private dataset() {
    return {
      label: this.title,
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
