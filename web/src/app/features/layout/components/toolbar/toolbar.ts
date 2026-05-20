import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map, startWith } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '@core/services/auth-service';
import { UsersService } from '@features/users/services/users-service';
import { SnackbarService } from '@core/services/snackbar-service';

const ROUTE_TITLES: Record<string, string> = {
  '/dashboard':   'Dashboard',
  '/properties':  'Properties',
  '/tenants':     'Tenants',
  '/landlords':   'Landlords',
  '/payments':    'Payments',
  '/profile':     'Profile',
};

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

  user = this.usersService.currentUser;

  pageTitle = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: NavigationEnd) => {
        const base = '/' + e.urlAfterRedirects.split('/')[1];
        return ROUTE_TITLES[base] ?? '';
      }),
      startWith(ROUTE_TITLES['/' + this.router.url.split('/')[1]] ?? ''),
    ),
    { initialValue: '' },
  );

  initials = computed(() => {
    const u = this.user();
    if (!u) return '';
    return `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase();
  });

  ngOnInit() {
    const externalKey = this.authService.getExternalId();
    if (externalKey) {
      this.usersService.getUserByExternalKey(externalKey).subscribe({
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
