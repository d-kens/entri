import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '@core/services/auth-service';
import { SnackbarService } from '@core/services/snackbar-service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  host: { class: 'w-full' },
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './reset-password.html',
})
export class ResetPassword {
  form: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);
  phoneNumber: string;

  togglePassword() { this.hidePassword.update(v => !v); }

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService,
  ) {
    this.phoneNumber = this.route.snapshot.queryParamMap.get('phoneNumber') ?? '';

    this.form = fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.authService.resetPassword({
      phoneNumber: this.phoneNumber,
      otp: this.form.value.otp,
      newPassword: this.form.value.newPassword,
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Password reset successfully. Please sign in.');
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        this.isLoading.set(false);
        const message = err?.error?.message || 'Invalid or expired OTP.';
        this.snackbarService.showError(message);
      }
    });
  }
}
