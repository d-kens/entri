import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '@core/services/auth-service';
import { UserService } from '@core/services/user-service';
import { SnackbarService } from '@core/services/snackbar-service';
import { UserResponse } from '@core/models/user.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private snackbarService = inject(SnackbarService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  user = signal<UserResponse | null>(null);
  isLoadingUser = signal(false);
  isEditing = signal(false);
  isSavingProfile = signal(false);
  isChangingPassword = signal(false);
  showPasswordSection = signal(false);
  showCurrentPassword = signal(false);
  showNewPassword = signal(false);
  showConfirmPassword = signal(false);

  initials = computed(() => {
    const name = this.user()?.name ?? '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  });

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  ngOnInit() {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      phoneNumber: ['', Validators.required],
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });

    this.loadUser();
  }

  private loadUser() {
    this.isLoadingUser.set(true);
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.user.set(user);
        this.profileForm.patchValue({ name: user.name, phoneNumber: user.phoneNumber });
        this.isLoadingUser.set(false);
      },
      error: () => {
        this.snackbarService.showError('Failed to load profile');
        this.isLoadingUser.set(false);
      },
    });
  }

  startEditing() {
    this.isEditing.set(true);
  }

  cancelEditing() {
    const user = this.user();
    if (user) {
      this.profileForm.patchValue({ name: user.name, phoneNumber: user.phoneNumber });
    }
    this.isEditing.set(false);
  }

  saveProfile() {
    if (this.profileForm.invalid) return;
    const externalId = this.user()!.externalId;
    this.isSavingProfile.set(true);
    this.userService.updateProfile(externalId, { name: this.profileForm.value.name }).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.isEditing.set(false);
        this.isSavingProfile.set(false);
        this.snackbarService.showSuccess('Profile updated');
      },
      error: () => {
        this.snackbarService.showError('Failed to update profile');
        this.isSavingProfile.set(false);
      },
    });
  }

  toggleCurrentPassword() { this.showCurrentPassword.update(v => !v); }
  toggleNewPassword() { this.showNewPassword.update(v => !v); }
  toggleConfirmPassword() { this.showConfirmPassword.update(v => !v); }

  togglePasswordSection() {
    this.showPasswordSection.update(v => !v);
    if (!this.showPasswordSection()) {
      this.passwordForm.reset();
    }
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;
    if (newPassword !== confirmPassword) {
      this.snackbarService.showError('New passwords do not match');
      return;
    }
    this.isChangingPassword.set(true);
    this.userService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.isChangingPassword.set(false);
        this.passwordForm.reset();
        this.showPasswordSection.set(false);
        this.snackbarService.showSuccess('Password changed successfully');
      },
      error: () => {
        this.snackbarService.showError('Failed to change password. Check your current password.');
        this.isChangingPassword.set(false);
      },
    });
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/auth/login'),
      error: () => this.snackbarService.showError('Failed to log out. Try again.'),
    });
  }
}
