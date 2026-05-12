import {Component, OnInit, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {ResetPasswordRequest} from '@core/models/auth.models';
import {AuthService} from '@core/services/auth-service';
import {SnackbarService} from '@core/services/snackbar-service';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatError, MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {passwordsMatch, PasswordMismatchStateMatcher} from '../../validators/password.validators';


@Component({
  selector: 'app-reset-password',
  imports: [
    FormsModule,
    MatButton,
    MatError,
    MatFormField,
    MatIcon,
    MatIconButton,
    MatInput,
    MatLabel,
    MatProgressSpinner,
    MatSuffix,
    ReactiveFormsModule
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  token!: string;
  resetPasswordForm!: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);
  passwordMatcher = new PasswordMismatchStateMatcher();

  togglePassword() { this.hidePassword.update(v => !v); }
  toggleConfirmPassword() { this.hideConfirmPassword.update(v => !v); }

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService
  ) {
    this.resetPasswordForm = fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, {validators: passwordsMatch});
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];

      if (!token) {
        this.router.navigate(['/auth/login']);
        return;
      }

      this.token = token;
    })
  }

  createAccount() {
    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: ResetPasswordRequest = {
      token: this.token,
      password: this.resetPasswordForm.get('password')!.value,
    };

    this.authService.resetPassword(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Password reset successfully!');
        this.router.navigateByUrl('/auth/login');
      },
      error: (err) => {
        this.isLoading.set(false);

        if (err.status === 401) {
          this.router.navigateByUrl('/auth/forgot-password');
          this.snackbarService.showError('This reset link has expired or already been used. Request a new one.');
          return;
        }

        const errorMessage = err?.error?.message || 'Password reset failed. Please try again.';
        this.snackbarService.showError(errorMessage);
      }
    });
  }
}
