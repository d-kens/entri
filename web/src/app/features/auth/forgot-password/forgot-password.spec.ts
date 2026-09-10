import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ForgotPassword } from './forgot-password';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('ForgotPassword', () => {
  let component: ForgotPassword;
  let fixture: ComponentFixture<ForgotPassword>;
  let authService: { forgotPassword: ReturnType<typeof vi.fn> };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(async () => {
    authService = { forgotPassword: vi.fn() };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ForgotPassword],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPassword);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit and should mark fields touched when email is invalid', () => {
    component.forgotPassword();

    expect(authService.forgotPassword).not.toHaveBeenCalled();
    expect(component.forgotPasswordForm.get('email')?.touched).toBe(true);
  });

  it('should call authService.forgotPassword with the email and navigate to login on success', () => {
    authService.forgotPassword.mockReturnValue(of(undefined));
    component.forgotPasswordForm.setValue({ email: 'john@example.com' });

    component.forgotPassword();

    expect(authService.forgotPassword).toHaveBeenCalledWith('john@example.com');
    expect(snackbarService.showSuccess).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(component.isLoading()).toBe(false);
  });

  it('should show a generic error and stop loading when the request fails', () => {
    authService.forgotPassword.mockReturnValue(throwError(() => new Error('network error')));
    component.forgotPasswordForm.setValue({ email: 'john@example.com' });

    component.forgotPassword();

    expect(snackbarService.showError).toHaveBeenCalledWith(
      'Something went wrong. Please try again later.',
    );
    expect(component.isLoading()).toBe(false);
  });
});
