import {Component, signal} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {CommonModule} from '@angular/common';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  FormGroupDirective,
  NgForm,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {SnackbarService} from '@core/services/snackbar-service';

import {ErrorStateMatcher} from '@angular/material/core';
import {RegisterUserRequest} from '@core/models/auth.models';
import {AuthService} from '@core/services/auth-service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : {passwordsMismatch: true};
}

class PasswordMismatchStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control?.touched && form?.hasError('passwordsMismatch'));
  }
}

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
  registerForm!: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);
  passwordMatcher = new PasswordMismatchStateMatcher();

  togglePassword() { this.hidePassword.update(v => !v); }
  toggleConfirmPassword() { this.hideConfirmPassword.update(v => !v); }

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService,
    private snackbarService: SnackbarService
  ) {
    this.registerForm = fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(13),
        Validators.pattern(/^(\+?254|0)[17]\d{8}$/),
      ]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, {validators: passwordsMatch});
  }

  createAccount() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: RegisterUserRequest = {
      firstName: this.registerForm.get('firstName')!.value,
      lastName: this.registerForm.get('lastName')!.value,
      email: this.registerForm.get('email')!.value,
      phoneNumber: this.registerForm.get('phoneNumber')!.value,
      password: this.registerForm.get('password')!.value,
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
      }
    });
  }
}
