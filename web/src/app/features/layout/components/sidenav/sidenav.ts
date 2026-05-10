import { Component, input, output, signal, OnInit } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '@core/services/auth-service';

interface MenuItem {
  icon: string;
  label: string;
  route: string;
}

@Component({
  selector: 'app-sidenav',
  imports: [
    MatListModule,
    MatIconModule,
    MatButtonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './sidenav.html',
  styleUrl: './sidenav.css',
  standalone: true
})
export class Sidenav implements OnInit {
  isMobile = input<boolean>(false);
  closeSidenav = output<void>();

  menuItem = signal<MenuItem[]>([]);

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit() {
    this.loadMenuByRole();
  }

  private loadMenuByRole() {
    switch(this.authService.getRole()) {
      case 'PLATFORM_ADMIN':
        this.menuItem.set(this.getPlatformAdminMenu());
        break;
      case 'PLATFORM_USER':
        this.menuItem.set(this.getPlatformUserMenu());
        break;
      default:
        this.menuItem.set([]);
    }
  }

  private getPlatformUserMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'groups', label: 'My Chamas', route: '/chamas' },
      { icon: 'payments', label: 'Contributions', route: '/contributions' },
      { icon: 'account_balance_wallet', label: 'Payouts', route: '/payouts' },
      { icon: 'receipt_long', label: 'Transactions', route: '/transactions' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
    ];
  }

  private getPlatformAdminMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'groups', label: 'Chamas', route: '/chamas' },
      { icon: 'people', label: 'Members', route: '/members' },
      { icon: 'payments', label: 'Contributions', route: '/contributions' },
      { icon: 'account_balance_wallet', label: 'Payouts', route: '/payouts' },
      { icon: 'receipt_long', label: 'Transactions', route: '/transactions' },
      { icon: 'analytics', label: 'Reports', route: '/reports' },
      { icon: 'tune', label: 'Settings', route: '/settings' },
    ];
  }

  onCloseSidenav() {
    this.closeSidenav.emit();
  }
}
