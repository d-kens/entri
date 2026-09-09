import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Register } from './register';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authService: { register: ReturnType<typeof vi.fn> };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  const validPayload = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    phoneNumber: '0712345678',
    password: 'Password123!',
    confirmPassword: 'Password123!',
  };

  beforeEach(async () => {
    authService = { register: vi.fn() };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit and should mark all fields touched when form is invalid', () => {
    component.createAccount();

    expect(authService.register).not.toHaveBeenCalled();
    expect(component.registerForm.get('firstName')?.touched).toBe(true);
  });

  it('should flag mismatched passwords as invalid', () => {
    component.registerForm.setValue({ ...validPayload, confirmPassword: 'Different1!' });

    expect(component.registerForm.hasError('passwordsMismatch')).toBe(true);
  });

  it('should register with mapped payload including the ORGANIZER role and navigate on success', () => {
    authService.register.mockReturnValue(of({} as any));
    component.registerForm.setValue(validPayload);

    component.createAccount();

    expect(authService.register).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
      password: 'Password123!',
      role: 'ORGANIZER',
    });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Account created successfully!');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/auth/login');
    expect(component.isLoading()).toBe(false);
  });

  it('should show an error and stop loading when registration fails', () => {
    authService.register.mockReturnValue(
      throwError(() => ({ error: { message: 'Email already exists' } })),
    );
    component.registerForm.setValue(validPayload);

    component.createAccount();

    expect(snackbarService.showError).toHaveBeenCalledWith('Email already exists');
    expect(component.isLoading()).toBe(false);
  });

  it('should fall back to a generic error message when the error has no message', () => {
    authService.register.mockReturnValue(throwError(() => ({})));
    component.registerForm.setValue(validPayload);

    component.createAccount();

    expect(snackbarService.showError).toHaveBeenCalledWith(
      'Account creation failed. Please try again.',
    );
  });

  it('should toggle password and confirm password visibility independently', () => {
    expect(component.hidePassword()).toBe(true);
    expect(component.hideConfirmPassword()).toBe(true);

    component.togglePassword();
    expect(component.hidePassword()).toBe(false);
    expect(component.hideConfirmPassword()).toBe(true);

    component.toggleConfirmPassword();
    expect(component.hideConfirmPassword()).toBe(false);
  });
});
