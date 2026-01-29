import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';

type UserRole = 'ADMIN' | 'MERCHANT' | 'AGENT' | 'CUSTOMER';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
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

  constructor(private router: Router) {}

  onToggleSidenav() {
    this.toggleSidenav.emit();
  }

  navigateToPayout() {
    this.router.navigate(['/payout']);
  }

  logout() {
    console.log('Logging out...');
    this.router.navigate(['/login']);
  }
}
