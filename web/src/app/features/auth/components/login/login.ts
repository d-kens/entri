import {Component, signal} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {SnackbarService} from '@core/services/snackbar-service';
import {AuthService} from '@core/services/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  host: { class: 'w-full' },
  imports: [
    CommonModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loginForm!: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);

  togglePassword() { this.hidePassword.update(v => !v); }

  returnUrl: string = '/dashboard';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService,
  ) {
    this.loginForm = fb.group({
      password: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
    });

    const fromQuery = this.route.snapshot.queryParamMap.get('returnUrl');
    if (fromQuery) {
      this.returnUrl = fromQuery;
    }
  }

  login() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    this.authService.login({
      email: this.loginForm.get('email')!.value,
      password: this.loginForm.get('password')!.value
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Invalid email or password.';
        this.snackbarService.showError(errorMessage);
        this.isLoading.set(false);
      }
    });
  }
}
