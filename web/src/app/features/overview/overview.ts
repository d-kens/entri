import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { UsersService } from '@core/services/users-service';

interface StatCard {
  icon: string;
  label: string;
  value: string | number;
  sub: string;
  accent?: boolean;
}

export interface RecentPayment {
  tenant: string;
  unit: string;
  property: string;
  amount: number;
  period: string;
  status: 'paid' | 'pending' | 'overdue';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatTableModule],
  templateUrl: './overview.html',
  styleUrl: './overview.css',
})
export class Overview {
  private usersService = inject(UsersService);

  user = this.usersService.currentUser;

  greeting = computed(() => {
    const hour = new Date().getHours();
    const name = this.user()?.firstName ?? '';
    const base = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
    return name ? `${base}, ${name}` : base;
  });

  today = new Date().toLocaleDateString('en-KE', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  });

  stats: StatCard[] = [
    { icon: 'apartment',     label: 'Properties', value: 3,            sub: '2 active'   },
    { icon: 'meeting_room',  label: 'Units',       value: 12,           sub: '8 occupied' },
    { icon: 'payments',      label: 'Collected',   value: 'KES 96,000', sub: 'This month' },
    { icon: 'warning_amber', label: 'Overdue',     value: 1,            sub: '2 pending', accent: true },
  ];

  displayedColumns: string[] = ['tenant', 'property', 'unit', 'period', 'amount', 'status'];

  recentPayments: RecentPayment[] = [
    { tenant: 'John Mwangi',   unit: '1A', property: 'Kilimani Heights',     amount: 12000, period: 'May 2025', status: 'paid'    },
    { tenant: 'Sarah Kamau',   unit: '1B', property: 'Kilimani Heights',     amount: 12000, period: 'May 2025', status: 'paid'    },
    { tenant: 'David Ochieng', unit: '2A', property: 'Kilimani Heights',     amount: 15000, period: 'May 2025', status: 'pending' },
    { tenant: 'Mary Wanjiku',  unit: '2B', property: 'Kilimani Heights',     amount: 12000, period: 'May 2025', status: 'paid'    },
    { tenant: 'Peter Njoroge', unit: '3A', property: 'Westlands Apartments', amount: 18000, period: 'May 2025', status: 'overdue' },
  ];

  formatAmount(amount: number): string {
    return `KES ${amount.toLocaleString()}`;
  }
}
