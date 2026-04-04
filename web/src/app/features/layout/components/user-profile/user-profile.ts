import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
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
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile {
  private router = inject(Router);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  user = input<UserResponse | undefined>();

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/auth/login'),
      error: () => {
        this.snackbarService.showError('An error occurred trying to log you out. Try again later');
      }
    });
  }
}
