import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '@features/auth/auth-service';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { EntriButton } from '@shared/components/button/entri-button.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    EntriButton,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private usersService = inject(UsersService);
  private snackbarService = inject(SnackbarService);

  isLoading = signal(false);

  profileForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: [
      '',
      [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(13),
        Validators.pattern(/^(\+?254|0)[17]\d{8}$/),
      ],
    ],
  });

  ngOnInit() {
    const user = this.usersService.currentUser();
    if (user) {
      this.profileForm.patchValue(user);
    }
  }

  save() {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const externalKey = this.authService.getExternalId();
    if (!externalKey) return;

    this.isLoading.set(true);

    this.usersService
      .updateUser(externalKey, {
        firstName: this.profileForm.value.firstName!,
        lastName: this.profileForm.value.lastName!,
        email: this.profileForm.value.email!,
        phoneNumber: this.profileForm.value.phoneNumber!,
      })
      .subscribe({
        next: () => {
          this.snackbarService.showSuccess('Profile updated successfully');
          this.isLoading.set(false);
        },
        error: (err) => {
          const msg = err?.error?.detail || err?.error?.message || 'Failed to update profile';
          this.snackbarService.showError(msg);
          this.isLoading.set(false);
        },
      });
  }
}
