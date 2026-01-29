import { Component, input, output } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {UserProfile} from '@features/dashboard/components/user-profile/user-profile';

type UserRole = 'ADMIN' | 'MERCHANT' | 'AGENT' | 'CUSTOMER';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    UserProfile
  ],
  templateUrl: './toolbar.html',
  styleUrl: './toolbar.css',
})
export class Toolbar {
  isMobile = input<boolean>(false);
  userName = input<string>('');
  walletBalance = input<number>(0);
  userRole = input<UserRole>('MERCHANT');

  toggleSidenav = output<void>();

  onToggleSidenav() {
    this.toggleSidenav.emit();
  }
}
