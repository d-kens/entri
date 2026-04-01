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
  otpSent = signal(false);

  returnUrl: string = '/';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private snackbarService: SnackbarService,
  ) {
    this.loginForm = fb.group({
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^0[17]\d{8}$/),
        Validators.minLength(10),
        Validators.maxLength(10)
      ]],
      code: ['']
    });

    const fromQuery = this.route.snapshot.queryParamMap.get('returnUrl');
    if (fromQuery) {
      this.returnUrl = fromQuery;
    }
  }

  login() {
    if (this.otpSent()) {
      if (this.loginForm.invalid) {
        this.loginForm.markAllAsTouched();
        return;
      }
    } else {
      const phoneControl = this.loginForm.get('phoneNumber');
      phoneControl?.markAsTouched();
      if (phoneControl?.invalid) return;
    }

    this.isLoading.set(true);

    const phoneNumber = this.loginForm.get('phoneNumber')!.value;
    const code = this.otpSent() ? this.loginForm.get('code')!.value : undefined;

    this.authService.login({ phoneNumber, code }).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response?.accessToken) {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.loginForm.get('phoneNumber')?.disable();
          this.loginForm.get('code')?.setValidators([Validators.required, Validators.pattern(/^\d{6}$/)]);
          this.loginForm.get('code')?.updateValueAndValidity();
          this.otpSent.set(true);
        }
      },
      error: (err) => {
        const errorMessage = err?.error?.message || (this.otpSent() ? 'Invalid or expired code.' : 'Failed to send OTP.');
        this.snackbarService.showError(errorMessage);
        this.isLoading.set(false);
      }
    });
  }

  changePhone() {
    this.loginForm.get('phoneNumber')?.enable();
    const codeControl = this.loginForm.get('code');
    codeControl?.clearValidators();
    codeControl?.reset('');
    codeControl?.updateValueAndValidity();
    this.otpSent.set(false);
  }

  get maskedPhone(): string {
    const p = this.loginForm.get('phoneNumber')!.value as string;
    if (!p) return '';
    return p.slice(0, 4) + '****' + p.slice(-2);
  }
}
