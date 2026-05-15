import { Component, effect, inject, input, output, signal } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { UsersService } from '@features/users/services/users-service';

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
export class Sidenav {
  private usersService = inject(UsersService);

  isMobile = input<boolean>(false);
  closeSidenav = output<void>();
  menuItem = signal<MenuItem[]>([]);

  constructor() {
    effect(() => {
      const role = this.usersService.currentUser()?.role ?? null;
      switch (role) {
        case 'PLATFORM_ADMIN':
          this.menuItem.set(this.getPlatformAdminMenu());
          break;
        case 'PLATFORM_USER':
          this.menuItem.set(this.getPlatformUserMenu());
          break;
        default:
          this.menuItem.set([]);
      }
    });
  }

  private getPlatformUserMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'apartment', label: 'Properties', route: '/properties' },
      { icon: 'people', label: 'Tenants', route: '/tenants' },
      { icon: 'payments', label: 'Payments', route: '/payments' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
    ];
  }

  private getPlatformAdminMenu(): MenuItem[] {
    return [
      { icon: 'dashboard', label: 'Dashboard', route: '/dashboard' },
      { icon: 'apartment', label: 'Properties', route: '/properties' },
      { icon: 'people', label: 'Landlords', route: '/landlords' },
      { icon: 'payments', label: 'Payments', route: '/payments' },
      { icon: 'account_circle', label: 'Profile', route: '/profile' },
    ];
  }

  onCloseSidenav() {
    this.closeSidenav.emit();
  }
}
