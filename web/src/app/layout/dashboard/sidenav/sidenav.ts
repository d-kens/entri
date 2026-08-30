import { Component, effect, inject, input, output, signal } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UsersService } from '@features/users/users-service';

interface MenuItem {
  icon: string;
  label: string;
  route: string;
}

@Component({
  selector: 'app-sidenav',
  imports: [MatListModule, MatIconModule, MatButtonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
  standalone: true,
})
export class Sidenav {
  private usersService = inject(UsersService);

  isMobile = input<boolean>(false);
  closeSidenav = output<void>();
  menuItem = signal<MenuItem[]>([]);

  constructor() {
    effect(() => {
      const role = this.usersService.currentUser()?.role ?? null;
      switch (role) {
        case 'ADMIN':
          this.menuItem.set(this.getPlatformAdminMenu());
          break;
        case 'ORGANIZER':
          this.menuItem.set(this.getPlatformUserMenu());
          break;
        default:
          this.menuItem.set([]);
      }
    });
  }

  private getPlatformUserMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard/summary' },
      { icon: 'confirmation_number', label: 'Events', route: '/dashboard/events' },
      { icon: 'local_activity', label: 'Tickets', route: '/dashboard/tickets' },
      { icon: 'groups', label: 'Attendees', route: '/dashboard/attendees' },
      { icon: 'account_balance_wallet', label: 'Wallet', route: '/dashboard/wallet' },
      { icon: 'account_circle', label: 'Profile', route: '/dashboard/profile' },
    ];
  }

  private getPlatformAdminMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard/summary' },
      { icon: 'people', label: 'Users', route: '/dashboard/users' },
      { icon: 'confirmation_number', label: 'Events', route: '/dashboard/events' },
      { icon: 'category', label: 'Categories', route: '/dashboard/categories' },
      { icon: 'payments', label: 'Payments', route: '/dashboard/payments' },
      { icon: 'bar_chart', label: 'Reports', route: '/dashboard/reports' },
      { icon: 'account_circle', label: 'Profile', route: '/dashboard/profile' },
    ];
  }

  onCloseSidenav() {
    this.closeSidenav.emit();
  }
}
