import {Component, signal} from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {SnackbarService} from '../../../../core/services/snackbar-service';
import {AuthService} from '../../../../core/services/auth-service';
import {AuthRequest} from '../../../../core/models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
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
  loginForm: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);

  returnUrl: string = '/dashboard';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService,
  ) {
    this.loginForm = fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    })

    const fromQuery = this.route.snapshot.queryParamMap.get('returnUrl');
    if (fromQuery) {
      this.returnUrl = fromQuery;
    }
  }

  togglePasswordVisibility(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword())
    event.stopPropagation();
  }

  login() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const authRequest: AuthRequest = {
      email: this.loginForm.get('email')!.value,
      password: this.loginForm.get('password')!.value
    };

    this.authService.login(authRequest).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackbarService.showSuccess('Login successful!');
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        console.log('This is the error: ', err);
        const errorMessage = err?.error?.message || 'Login failed. Please check your credentials.';
        this.snackbarService.showError(errorMessage)
        this.isLoading.set(false);
      }
    });
  }
}
