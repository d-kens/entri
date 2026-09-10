import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ChangePassword } from './change-password';
import { AuthService } from '@features/auth/auth-service';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('ChangePassword', () => {
  let component: ChangePassword;
  let fixture: ComponentFixture<ChangePassword>;
  let authService: { getExternalId: ReturnType<typeof vi.fn> };
  let usersService: { changePassword: ReturnType<typeof vi.fn> };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  const validPayload = {
    currentPassword: 'OldPassword1!',
    password: 'NewPassword1!',
    confirmPassword: 'NewPassword1!',
  };

  beforeEach(async () => {
    authService = { getExternalId: vi.fn().mockReturnValue('ext-key') };
    usersService = { changePassword: vi.fn() };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ChangePassword],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: UsersService, useValue: usersService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangePassword);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit and should mark fields touched when form is invalid', () => {
    component.submit();

    expect(usersService.changePassword).not.toHaveBeenCalled();
    expect(component.form.get('currentPassword')?.touched).toBe(true);
  });

  it('should not submit when there is no external id', () => {
    authService.getExternalId.mockReturnValue(null);
    component.form.setValue(validPayload);

    component.submit();

    expect(usersService.changePassword).not.toHaveBeenCalled();
  });

  it('should submit mapped payload and navigate to profile on success', () => {
    usersService.changePassword.mockReturnValue(of(undefined));
    component.form.setValue(validPayload);

    component.submit();

    expect(usersService.changePassword).toHaveBeenCalledWith('ext-key', {
      currentPassword: 'OldPassword1!',
      newPassword: 'NewPassword1!',
    });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Password changed successfully');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/profile']);
  });

  it('should show the server error message and stop loading on failure', () => {
    usersService.changePassword.mockReturnValue(
      throwError(() => ({ error: { detail: 'Current password is incorrect' } })),
    );
    component.form.setValue(validPayload);

    component.submit();

    expect(snackbarService.showError).toHaveBeenCalledWith('Current password is incorrect');
    expect(component.isLoading()).toBe(false);
  });

  it('should fall back to a generic error message when the error has no detail or message', () => {
    usersService.changePassword.mockReturnValue(throwError(() => ({})));
    component.form.setValue(validPayload);

    component.submit();

    expect(snackbarService.showError).toHaveBeenCalledWith('Failed to change password');
  });

  it('should toggle each password field visibility independently', () => {
    expect(component.hideCurrentPassword()).toBe(true);
    expect(component.hideNewPassword()).toBe(true);
    expect(component.hideConfirmPassword()).toBe(true);
  });
});
