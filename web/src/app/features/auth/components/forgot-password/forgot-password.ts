import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {RouterLink} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '../../../../core/services/auth-service';
import {SnackbarService} from '../../../../core/services/snackbar-service';

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
  emailSent = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackbarService: SnackbarService
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

    const email = this.forgotPasswordForm.get('email')!.value;

    this.authService.sendPasswordResetInstructions(email).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.emailSent.set(true);
        this.snackbarService.showError('Password Reset Instruction sent to the provided email.');
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
