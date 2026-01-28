import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {RouterLink} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {Auth} from '@core/services/auth';
import {Snackbar} from '@core/services/snackbar';
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
    private authService: Auth,
    private snackbarService: Snackbar
  ) {
    this.forgotPasswordForm = fb.group({
      email: ['', [Validators.email, Validators.required]]
    })
  }

  sendPasswordResetInstructions() {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return
    }

    this.isLoading.set(true);

    const payload: ForgotPasswordPayload = {
      email: this.forgotPasswordForm.get('email')!.value
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

  showForm() {
    this.emailSent.set(false);
    this.forgotPasswordForm.reset();
  }
}
