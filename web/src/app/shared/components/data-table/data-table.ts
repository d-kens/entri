import { Component, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Shimmer } from '@shared/components/shimmer/shimmer';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, Shimmer],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable {
  loading = input(false);
  error = input(false);
  empty = input(false);
  emptyIcon = input('table_chart');
  emptyTitle = input('No data');
  emptyMessage = input('');
  retry = output<void>();

  readonly skeletonRows: [string, string, string][] = [
    ['40%', '25%', '20%'],
    ['55%', '30%', '15%'],
    ['35%', '20%', '25%'],
    ['50%', '28%', '18%'],
    ['45%', '22%', '22%'],
  ];
}
