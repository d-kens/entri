import {Component, signal} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {CommonModule} from '@angular/common';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {AbstractControl, FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {SnackbarService} from '@core/services/snackbar-service';
import {RegisterMerchantRequest} from '@core/models/user.models';
import {UserService} from '@core/services/user-service';
import {ErrorStateMatcher} from '@angular/material/core';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
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
    private userService: UserService,
    private snackbarService: SnackbarService
  ) {
    this.registerForm = fb.group({
      name: ['', Validators.required],
      phoneNumber: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(12)
      ]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, { validators: passwordsMatch });
  }

  createAccount() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: RegisterMerchantRequest = {
      name: this.registerForm.get('name')!.value,
      phoneNumber: this.registerForm.get('phoneNumber')!.value,
      password: this.registerForm.get('password')!.value
    }

    this.userService.registerMerchant(payload).subscribe({
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
