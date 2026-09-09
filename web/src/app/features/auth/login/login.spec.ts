import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Login } from './login';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authService: { login: ReturnType<typeof vi.fn> };
  let snackbarService: { showError: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    authService = { login: vi.fn() };
    snackbarService = { showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: SnackbarService, useValue: snackbarService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap({}) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default returnUrl to the dashboard summary when no query param is present', () => {
    expect(component.returnUrl).toBe('/dashboard/summary');
  });

  it('should not call authService.login and should mark fields touched when form is invalid', () => {
    component.login();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.loginForm.get('email')?.touched).toBe(true);
    expect(component.loginForm.get('password')?.touched).toBe(true);
  });

  it('should call authService.login with form values and navigate on success', () => {
    authService.login.mockReturnValue(of({ accessToken: { token: 'tok' }, user: {} } as any));
    component.loginForm.setValue({ email: 'john@example.com', password: 'Password123!' });

    component.login();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'john@example.com',
      password: 'Password123!',
    });
    expect(component.isLoading()).toBe(false);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard/summary');
  });

  it('should show an error and stop loading when login fails', () => {
    authService.login.mockReturnValue(
      throwError(() => ({ error: { message: 'Invalid credentials' } })),
    );
    component.loginForm.setValue({ email: 'john@example.com', password: 'wrong' });

    component.login();

    expect(snackbarService.showError).toHaveBeenCalledWith('Invalid credentials');
    expect(component.isLoading()).toBe(false);
  });

  it('should fall back to a generic error message when the error has no message', () => {
    authService.login.mockReturnValue(throwError(() => ({})));
    component.loginForm.setValue({ email: 'john@example.com', password: 'wrong' });

    component.login();

    expect(snackbarService.showError).toHaveBeenCalledWith('Invalid email or password.');
  });

  it('should toggle password visibility', () => {
    expect(component.hidePassword()).toBe(true);
    component.togglePassword();
    expect(component.hidePassword()).toBe(false);
  });

  it('should use the returnUrl query param when present', async () => {
    TestBed.resetTestingModule();
    const authServiceStub = { login: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        { provide: SnackbarService, useValue: { showError: vi.fn() } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap({ returnUrl: '/dashboard/events' }) },
          },
        },
      ],
    }).compileComponents();

    const customFixture = TestBed.createComponent(Login);
    const customComponent = customFixture.componentInstance;

    expect(customComponent.returnUrl).toBe('/dashboard/events');
  });
});
