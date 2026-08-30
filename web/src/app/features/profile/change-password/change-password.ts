import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@features/auth/auth-service';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { passwordsMatchValidator } from '@features/auth/utils/validators';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    RouterLink,
    EntriButton,
  ],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePassword {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private usersService = inject(UsersService);
  private snackbarService = inject(SnackbarService);

  isLoading = signal(false);
  hideCurrentPassword = signal(true);
  hideNewPassword = signal(true);
  hideConfirmPassword = signal(true);

  form = this.fb.group(
    {
      currentPassword: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatchValidator },
  );

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const externalKey = this.authService.getExternalId();
    if (!externalKey) return;

    this.isLoading.set(true);

    this.usersService
      .changePassword(externalKey, {
        currentPassword: this.form.value.currentPassword!,
        newPassword: this.form.value.password!,
      })
      .subscribe({
        next: () => {
          this.snackbarService.showSuccess('Password changed successfully');
          this.router.navigate(['/dashboard/profile']);
        },
        error: (err) => {
          const msg = err?.error?.detail || err?.error?.message || 'Failed to change password';
          this.snackbarService.showError(msg);
          this.isLoading.set(false);
        },
      });
  }
}
