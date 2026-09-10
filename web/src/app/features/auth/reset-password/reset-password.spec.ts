import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ResetPassword } from './reset-password';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('ResetPassword', () => {
  let component: ResetPassword;
  let fixture: ComponentFixture<ResetPassword>;
  let authService: { resetPassword: ReturnType<typeof vi.fn> };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  async function setup(queryParams: Record<string, string>) {
    authService = { resetPassword: vi.fn() };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ResetPassword],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: SnackbarService, useValue: snackbarService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(queryParams) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPassword);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  }

  it('should create when a token query param is present', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(component.token).toBe('abc123');
  });

  it('should redirect to login when no token query param is present', async () => {
    await setup({});
    fixture.detectChanges();

    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should not submit and should mark fields touched when form is invalid', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();

    component.resetPassword();

    expect(authService.resetPassword).not.toHaveBeenCalled();
    expect(component.resetPasswordForm.get('password')?.touched).toBe(true);
  });

  it('should flag mismatched passwords as invalid', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();

    component.resetPasswordForm.setValue({
      password: 'Password123!',
      confirmPassword: 'Different1!',
    });

    expect(component.resetPasswordForm.hasError('passwordsMismatch')).toBe(true);
  });

  it('should submit with the token and password, then navigate to login on success', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();
    authService.resetPassword.mockReturnValue(of(undefined));
    component.resetPasswordForm.setValue({
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });

    component.resetPassword();

    expect(authService.resetPassword).toHaveBeenCalledWith({
      token: 'abc123',
      password: 'Password123!',
    });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Password reset successfully!');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/auth/login');
    expect(component.isLoading()).toBe(false);
  });

  it('should redirect to forgot-password when the reset link is expired (401)', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();
    authService.resetPassword.mockReturnValue(throwError(() => ({ status: 401 })));
    component.resetPasswordForm.setValue({
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });

    component.resetPassword();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/auth/forgot-password');
    expect(snackbarService.showError).toHaveBeenCalledWith(
      'This reset link has expired or already been used. Request a new one.',
    );
    expect(component.isLoading()).toBe(false);
  });

  it('should show the server error message for other failures', async () => {
    await setup({ token: 'abc123' });
    fixture.detectChanges();
    authService.resetPassword.mockReturnValue(
      throwError(() => ({ status: 400, error: { message: 'Weak password' } })),
    );
    component.resetPasswordForm.setValue({
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });

    component.resetPassword();

    expect(snackbarService.showError).toHaveBeenCalledWith('Weak password');
  });
});
