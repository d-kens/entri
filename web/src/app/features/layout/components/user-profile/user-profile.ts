import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserResponse } from '@core/models/user.models';
import { AuthService } from '@core/services/auth-service';
import { SnackbarService } from '@core/services/snackbar-service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile {
  private router = inject(Router);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  user = input<UserResponse | undefined>();
  walletBalance = input<number>(0);
  isLoadingBalance = input<boolean>(false);

  navigateToPayout() {
    this.router.navigate(['/payout']);
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/auth/login'),
      error: err => {
        this.snackbarService.showError('An error occurred trying to log you out. Try again later');
      }
    });
  }
}
