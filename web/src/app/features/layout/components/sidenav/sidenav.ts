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
      case 'ADMIN':
        this.menuItem.set(this.getAdminMenu());
        break;
      case 'MERCHANT':
        this.menuItem.set(this.getMerchantMenu());
        break;
      case 'AGENT':
        this.menuItem.set(this.getAgentMenu());
        break;
      default:
        this.menuItem.set([]);
    }
  }

  private getMerchantMenu(): MenuItem[] {
    return [
      { icon: 'inventory_2', label: 'Deliveries', route: '/deliveries' },
      { icon: 'add_circle', label: 'New Delivery', route: '/deliveries/new' },
      { icon: 'timeline', label: 'Track Delivery', route: '/deliveries/track' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
      { icon: 'help', label: 'Support', route: '/support' },
    ];
  }

  private getAgentMenu(): MenuItem[] {
    return [
      { icon: 'inventory_2', label: 'Deliveries', route: '/deliveries' },
      { icon: 'paid', label: 'Commissions', route: '/commissions' },
      { icon: 'account_balance', label: 'Payout Request', route: '/payout' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
      { icon: 'help', label: 'Support', route: '/support' },
    ];
  }

  private getAdminMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'inventory_2', label: 'Deliveries', route: '/deliveries' },
      { icon: 'store', label: 'Agents', route: '/agents' },
      { icon: 'shopping_bag', label: 'Customers', route: '/customers' },
      { icon: 'payments', label: 'Payments', route: '/payments' },
      { icon: 'analytics', label: 'Reports', route: '/reports' },
      { icon: 'tune', label: 'Settings', route: '/settings' },
    ];
  }

  onCloseSidenav() {
    this.closeSidenav.emit();
  }
}
