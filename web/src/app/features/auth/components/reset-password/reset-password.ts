import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '@core/services/auth-service';
import {SnackbarService} from '@core/services/snackbar-service';
import {passwordsMatchValidator} from '@core/utils/validators';
import {ResetPasswordPayload} from '@core/models/auth.models';

@Component({
  selector: 'app-reset-password',
  imports: [
    CommonModule,
    RouterLink,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  resetPasswordToken!: string
  resetPasswordForm: FormGroup;
  isLoading = signal(false);
  visibility = signal({
    password: true,
    confirmPassword: true,
  });

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService
  ) {
    this.resetPasswordForm = fb.group({
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordsMatchValidator })
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const token = params.get('token');
      if (token)
        this.resetPasswordToken = token
    })
  }

  toggleVisibility(field: 'password' | 'confirmPassword', event: MouseEvent) {
    event.stopPropagation();
    this.visibility.update(v => ({
      ...v,
      [field]: !v[field],
    }));
  }

  resetPassword() {
    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    if (!this.resetPasswordToken) {
      this.snackbarService.showError('Invalid or missing reset token.');
      return;
    }

    this.isLoading.set(true);

    const payload: ResetPasswordPayload = {
      token: this.resetPasswordToken,
      newPassword: this.resetPasswordForm.get('password')!.value
    };

    this.authService.resetPassword(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Password Reset Successful');
        this.router.navigateByUrl('/auth/login');
      },
      error: (err) => {
        this.isLoading.set(false);

        if (err.status === 404) {
          this.snackbarService.showError('Invalid or expired reset token.');
          this.router.navigateByUrl('/auth/login');
          return;
        }

        const errorMessage =
          err.error?.error || 'Password reset failed. Please try again later.';

        this.snackbarService.showError(errorMessage);
      }
    });
  }

}
