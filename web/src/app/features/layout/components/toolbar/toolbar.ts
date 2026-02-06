import {Component, inject, input, output, signal, OnInit} from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {UserProfile} from '@features/layout/components/user-profile/user-profile';
import {UserResponse} from '@core/models/user.models';
import {AuthService} from '@core/services/auth-service';

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

  isMobile = input<boolean>(false);
  walletBalance = signal(12450.50);
  user = signal<UserResponse | undefined>(undefined);

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
      },
      error: (error) => {
        console.error('Failed to fetch user:', error);
      }
    });
  }
}
