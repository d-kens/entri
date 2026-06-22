import { Component, inject, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SnackbarService } from '@core/services/snackbar-service';
import { AuthService } from '@core/services/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  host: { class: 'w-full' },
  imports: [
    CommonModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  isLoading = signal(false);
  hidePassword = signal(true);
  returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard/overview';

  togglePassword() {
    this.hidePassword.update((v) => !v);
  }

  login() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    this.authService
      .login({
        email: this.loginForm.get('email')?.value ?? '',
        password: this.loginForm.get('password')?.value ?? '',
      })
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigateByUrl(this.returnUrl);
        },
        error: (err) => {
          const errorMessage = err?.error?.message || 'Invalid email or password.';
          this.snackbarService.showError(errorMessage);
          this.isLoading.set(false);
        },
      });
  }
}
