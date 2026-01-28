import { Component, input, output, signal, OnInit } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

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
})
export class Sidenav implements OnInit {
  isMobile = input<boolean>(false);
  closeSidenav = output<void>();

  private currentRole: 'ADMIN' | 'MERCHANT' | 'AGENT' = 'MERCHANT';

  menuItem = signal<MenuItem[]>([]);

  constructor(private router: Router) {}

  ngOnInit() {
    this.loadMenuByRole();
  }

  private loadMenuByRole() {
    switch(this.currentRole) {
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

  private getAdminMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'local_shipping', label: 'Orders', route: '/orders' },
      { icon: 'store', label: 'Agents', route: '/agents' },
      { icon: 'two_wheeler', label: 'Riders', route: '/riders' },
      { icon: 'shopping_bag', label: 'Merchants', route: '/merchants' },
      { icon: 'widgets', label: 'Batching', route: '/batching' },
      { icon: 'route', label: 'Routing', route: '/routing' },
      { icon: 'payments', label: 'Payments', route: '/payments' },
      { icon: 'account_balance_wallet', label: 'Payouts', route: '/payouts' },
      { icon: 'analytics', label: 'Reports', route: '/reports' },
      { icon: 'notifications', label: 'Notifications', route: '/notifications' },
      { icon: 'tune', label: 'Settings', route: '/settings' },
    ];
  }

  private getMerchantMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'add_circle', label: 'New Order', route: '/orders/new' },
      { icon: 'inventory_2', label: 'My Orders', route: '/orders' },
      { icon: 'location_on', label: 'Drop-off Points', route: '/drop-off-points' },
      { icon: 'history', label: 'Order History', route: '/history' },
      { icon: 'receipt_long', label: 'Invoices', route: '/invoices' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
      { icon: 'help', label: 'Support', route: '/support' },
    ];
  }

  private getAgentMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'qr_code_scanner', label: 'Scan Parcel', route: '/scan' },
      { icon: 'input', label: 'Incoming', route: '/incoming' },
      { icon: 'output', label: 'Outgoing', route: '/outgoing' },
      { icon: 'inventory', label: 'Current Stock', route: '/stock' },
      { icon: 'paid', label: 'Commissions', route: '/commissions' },
      { icon: 'account_balance', label: 'Payout Request', route: '/payout' },
      { icon: 'notifications', label: 'Notifications', route: '/notifications' },
      { icon: 'settings', label: 'Settings', route: '/settings' },
    ];
  }

  onCloseSidenav() {
    this.closeSidenav.emit();
  }
}
