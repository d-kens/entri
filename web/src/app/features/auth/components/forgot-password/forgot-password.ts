import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {RouterLink} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '@core/services/auth-service';
import {SnackbarService} from '@core/services/snackbar-service';
import {ForgotPasswordPayload} from '@core/models/auth.models';

@Component({
  selector: 'app-forgot-password',
  imports: [
    RouterLink,
    CommonModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  forgotPasswordForm: FormGroup;
  isLoading = signal(false);
  emailSent = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackbarService: SnackbarService
  ) {
    this.forgotPasswordForm = fb.group({
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^0[17]\d{8}$/), // Matches 07XXXXXXXX or 01XXXXXXXX
        Validators.minLength(10),
        Validators.maxLength(10)
      ]],
    })
  }

  sendPasswordResetInstructions() {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return
    }

    this.isLoading.set(true);

    const payload: ForgotPasswordPayload = {
      phoneNumber: this.forgotPasswordForm.get('phoneNumber')!.value
    }

    this.authService.sendPasswordResetInstructions(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.emailSent.set(true);
        this.snackbarService.showSuccess('Password Reset Instruction sent to the provided email.');
      },
      error: (err) => {
        console.log('This is the error: ', err);
        const errorMessage = err?.error?.message || 'Reset password failed. Try again later';
        this.snackbarService.showError(errorMessage);
        this.isLoading.set(false);
      }
    })
  }

  getPhoneErrorMessage(): string {
    const control = this.forgotPasswordForm.get('phoneNumber');

    if (control?.hasError('required')) {
      return 'Phone number is required';
    }
    if (control?.hasError('pattern') || control?.hasError('minLength') || control?.hasError('maxLength')) {
      return 'Enter a valid Kenyan phone number (07XX XXX XXX or 01XX XXX XXX)';
    }

    return '';
  }

  showForm() {
    this.emailSent.set(false);
    this.forgotPasswordForm.reset();
  }
}
