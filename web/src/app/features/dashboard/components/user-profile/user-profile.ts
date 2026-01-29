import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';

type UserRole = 'ADMIN' | 'MERCHANT' | 'AGENT' | 'CUSTOMER';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile {
  userName = input<string>('');
  walletBalance = input<number>(0);
  userRole = input<UserRole>('MERCHANT');

  constructor(private router: Router) {}

  navigateToPayout() {
    this.router.navigate(['/payout']);
  }

  logout() {
    console.log('Logging out...');
    this.router.navigate(['/login']);
  }

  getRoleDisplayName(): string {
    const roleMap: Record<UserRole, string> = {
      'ADMIN': 'Administrator',
      'MERCHANT': 'Merchant Account',
      'AGENT': 'Agent Account',
      'CUSTOMER': 'Customer Account'
    };
    return roleMap[this.userRole()];
  }
}
