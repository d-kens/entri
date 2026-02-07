import {Component, OnInit, OnDestroy, signal} from '@angular/core';
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
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-reset-password',
  standalone: true,
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
  styleUrls: ['./reset-password.css'],
})
export class ResetPassword implements OnInit, OnDestroy {
  resetPasswordToken!: string;
  resetPasswordForm!: FormGroup;
  isLoading = signal(false);
  showSuccessMessage = signal(false);
  visibility = signal({
    password: true,
    confirmPassword: true,
  });

  private subscriptions = new Subscription();

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
    }, { validators: passwordsMatchValidator });
  }

  ngOnInit(): void {
    const sub = this.route.queryParamMap.subscribe(params => {
      const token = params.get('token');
      if (token) {
        this.resetPasswordToken = token;
      } else {
        this.snackbarService.showError('Invalid or missing reset token.');
        this.router.navigateByUrl('/auth/login');
      }
    });

    this.subscriptions.add(sub);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
        this.showSuccessMessage.set(true);

        setTimeout(() => {
          this.router.navigateByUrl('/auth/login');
        }, 3000);
      },
      error: (err) => {
        this.isLoading.set(false);

        const errorMessage =
          err.error?.error || 'Password reset failed. Please try again later.';

        this.snackbarService.showError(errorMessage);

        setTimeout(() => {
          this.router.navigateByUrl('/auth/login');
        }, 2500);
      }
    });
  }
}
