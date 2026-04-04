import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '@core/services/auth-service';
import { SnackbarService } from '@core/services/snackbar-service';

@Component({
  selector: 'app-forgot-password',
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
  templateUrl: './forgot-password.html',
})
export class ForgotPassword {
  form: FormGroup;
  isLoading = signal(false);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private snackbarService: SnackbarService,
  ) {
    this.form = fb.group({
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^0[17]\d{8}$/),
      ]],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.authService.forgotPassword({ phoneNumber: this.form.value.phoneNumber }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/auth/reset-password'], {
          queryParams: { phoneNumber: this.form.value.phoneNumber }
        });
      },
      error: () => {
        this.isLoading.set(false);
        this.snackbarService.showError('Could not send OTP. Please try again.');
      }
    });
  }
}
