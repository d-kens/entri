import {Component, signal} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {CommonModule} from '@angular/common';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {SnackbarService} from '../../../../core/services/snackbar-service';
import {CreateUserRequest} from '../../../../core/models/user.models';
import {UserService} from '../../../../core/services/user-service';

@Component({
  selector: 'app-register',
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
  registerForm: FormGroup;
  isLoading = signal(false);
  visibility = signal({
    password: true,
    confirmPassword: true,
  });

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private userService: UserService,
    private snackbarService: SnackbarService
  ) {
    this.registerForm = fb.group({
      userName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });
  }

  private passwordsMatchValidator = (group: AbstractControl): ValidationErrors | null => {
    const passwordControl = group.get('password');
    const confirmControl = group.get('confirmPassword');

    if (!passwordControl || !confirmControl) return null;

    const password = passwordControl.value;
    const confirmPassword = confirmControl.value;

    // Helper to remove only the passwordMismatch error without clobbering others
    const clearMismatchError = () => {
      const currentErrors = confirmControl.errors || {};
      if ('passwordMismatch' in currentErrors) {
        const { passwordMismatch, ...rest } = currentErrors as Record<string, any>;
        const newErrors = Object.keys(rest).length ? rest : null;
        confirmControl.setErrors(newErrors);
      }
    };

    // If either field is empty, don't validate mismatch yet, and ensure mismatch error is cleared
    if (!password || !confirmPassword) {
      clearMismatchError();
      return null;
    }

    if (password !== confirmPassword) {
      const currentErrors = confirmControl.errors || {};
      if (!currentErrors['passwordMismatch']) {
        confirmControl.setErrors({ ...currentErrors, passwordMismatch: true });
      }
      return { passwordMismatch: true };
    }

    // Passwords match — clear mismatch error if present
    clearMismatchError();
    return null;
  };

  toggleVisibility(field: 'password' | 'confirmPassword', event: MouseEvent) {
    event.stopPropagation();
    this.visibility.update(v => ({
      ...v,
      [field]: !v[field],
    }));
  }

  createAccount() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: CreateUserRequest = {
      role: 'MERCHANT',
      email: this.registerForm.get('email')!.value,
      userName: this.registerForm.get('userName')!.value,
      password: this.registerForm.get('password')!.value,
      phoneNumber: this.registerForm.get('phoneNumber')!.value
    }

    this.userService.createUser(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Account Created Succesfully!!');
        this.router.navigateByUrl('/auth/login')
      },
      error: (err) => {
        console.log('This is the error: ', err);
        const errorMessage = err?.error?.message || 'Login failed. Please check your credentials.';
        this.isLoading.set(false);
      }
    })
  }
}
