import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { UserResponse } from '@core/models/user.models';
import { AuthService } from '@core/services/auth-service';
import { UsersService } from '@features/users/services/users-service';
import { SnackbarService } from '@core/services/snackbar-service';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './toolbar.html',
  styleUrl: './toolbar.css',
})
export class Toolbar implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private usersService = inject(UsersService);
  private snackbarService = inject(SnackbarService);

  isMobile = input<boolean>(false);
  toggleSidenav = output<void>();

  user = signal<UserResponse | undefined>(undefined);

  initials = computed(() => {
    const u = this.user();
    if (!u) return '';
    return `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase();
  });

  ngOnInit() {
    const externalKey = this.authService.getExternalId();
    if (externalKey) {
      this.usersService.getUserByExternalKey(externalKey).subscribe({
        next: (user: UserResponse) => this.user.set(user),
        error: (err) => console.error('Failed to fetch user:', err),
      });
    }
  }

  onToggleSidenav() {
    this.toggleSidenav.emit();
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/auth/login'),
      error: () => this.snackbarService.showError('An error occurred trying to log you out. Try again later'),
    });
  }
}
