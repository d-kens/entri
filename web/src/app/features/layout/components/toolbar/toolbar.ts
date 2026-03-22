import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserProfile } from '@features/layout/components/user-profile/user-profile';
import { UserResponse } from '@core/models/user.models';
import { AuthService } from '@core/services/auth-service';
import {WalletService} from '@features/wallet/service/wallet.service';

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
export class Toolbar implements OnInit {
  private authService = inject(AuthService);
  private walletService = inject(WalletService);

  isMobile = input<boolean>(false);
  walletBalance = signal<number>(0);
  user = signal<UserResponse | undefined>(undefined);
  isLoadingWallet = signal<boolean>(false);

  toggleSidenav = output<void>();

  ngOnInit() {
    this.getCurrentUser();
  }

  onToggleSidenav() {
    this.toggleSidenav.emit();
  }

  getCurrentUser() {
    this.authService.getCurrentUser().subscribe({
      next: (user: UserResponse) => {
        this.user.set(user);

        // Load wallet balance only for non-admin users
        if (user.role !== 'ADMIN') {
          this.loadWalletBalance();
        }
      },
      error: (error) => {
        console.error('Failed to fetch user:', error);
      }
    });
  }

  loadWalletBalance() {
    this.isLoadingWallet.set(true);
    this.walletService.getMyWallet().subscribe({
      next: (wallet) => {
        this.walletBalance.set(wallet.balance);
        this.isLoadingWallet.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch wallet balance:', error);
        this.walletBalance.set(0);
        this.isLoadingWallet.set(false);
      }
    });
  }
}
