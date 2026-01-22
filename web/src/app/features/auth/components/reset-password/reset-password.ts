import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '@core/services/auth-service';
import {SnackbarService} from '@core/services/snackbar-service';
import {passwordsMatchValidator} from '@core/utils/validators';

@Component({
  selector: 'app-reset-password',
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
  styleUrl: './reset-password.css',
})
export class ResetPassword {
  resetPasswordForm: FormGroup;
  isLoading = signal(false);
  visibility = signal({
    password: true,
    confirmPassword: true,
  });

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService,
    private snackbarService: SnackbarService
  ) {
    this.resetPasswordForm = fb.group({
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordsMatchValidator })
  }

  toggleVisibility(field: 'password' | 'confirmPassword', event: MouseEvent) {
    event.stopPropagation();
    this.visibility.update(v => ({
      ...v,
      [field]: !v[field],
    }));
  }

  resetPassword() {

  }

}

/**
 [User clicks link]
 ↓
 [Frontend extracts token → POST to /validate-reset-token]
 ↓
 [Backend validates token]
 ├─ Valid → Show Reset Password form
 └─ Invalid → Show error message ("Link expired/invalid")
 ↓
 [User submits new password → POST /reset-password]
 */
