import {Component, inject, signal} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {CommonModule} from '@angular/common';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {SnackbarService} from '@core/services/snackbar-service';
import {RegisterUserRequest} from '@core/models/auth.models';
import {AuthService} from '@core/services/auth-service';
import {passwordsMatch, PasswordMismatchStateMatcher} from '../../validators/password.validators';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    RouterLink,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);

  registerForm = this.fb.group({
    firstName:     ['', [Validators.required, Validators.minLength(2)]],
    lastName:      ['', [Validators.required, Validators.minLength(2)]],
    email:         ['', [Validators.required, Validators.email]],
    phoneNumber:   ['', [
      Validators.required,
      Validators.minLength(10),
      Validators.maxLength(13),
      Validators.pattern(/^(\+?254|0)[17]\d{8}$/),
    ]],
    password:        ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
  }, {validators: passwordsMatch});

  isLoading           = signal(false);
  hidePassword        = signal(true);
  hideConfirmPassword = signal(true);
  passwordMatcher     = new PasswordMismatchStateMatcher();

  togglePassword()        { this.hidePassword.update(v => !v); }
  toggleConfirmPassword() { this.hideConfirmPassword.update(v => !v); }

  createAccount() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: RegisterUserRequest = {
      firstName:   this.registerForm.get('firstName')?.value   ?? '',
      lastName:    this.registerForm.get('lastName')?.value    ?? '',
      email:       this.registerForm.get('email')?.value       ?? '',
      phoneNumber: this.registerForm.get('phoneNumber')?.value ?? '',
      password:    this.registerForm.get('password')?.value    ?? '',
      role: 'PLATFORM_USER',
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Account created successfully!');
        this.router.navigateByUrl('/auth/login');
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Account creation failed. Please try again.';
        this.snackbarService.showError(errorMessage);
        this.isLoading.set(false);
      },
    });
  }
}
