import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';

import { Toolbar } from './toolbar';
import { AuthService } from '@features/auth/auth-service';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { UserResponse } from '@features/users/models/user.models';

describe('Toolbar', () => {
  let component: Toolbar;
  let fixture: ComponentFixture<Toolbar>;
  let currentUser: ReturnType<typeof signal<UserResponse | null>>;
  let authService: { getExternalId: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let usersService: {
    currentUser: ReturnType<typeof signal<UserResponse | null>>;
    getUserByExternalKey: ReturnType<typeof vi.fn>;
  };
  let snackbarService: { showError: ReturnType<typeof vi.fn> };
  let router: {
    url: string;
    events: ReturnType<typeof of>;
    navigateByUrl: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    currentUser = signal<UserResponse | null>(null);
    authService = { getExternalId: vi.fn(() => null), logout: vi.fn() };
    usersService = { currentUser, getUserByExternalKey: vi.fn(() => of({} as UserResponse)) };
    snackbarService = { showError: vi.fn() };
    router = { url: '/dashboard/events', events: of(), navigateByUrl: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Toolbar],
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: authService },
        { provide: UsersService, useValue: usersService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Toolbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should derive the initial page title from the current router url', () => {
    expect(component.pageTitle()).toBe('Events');
  });

  it('should compute empty initials when there is no current user', () => {
    expect(component.initials()).toBe('');
  });

  it('should compute initials from the current user first and last name', () => {
    currentUser.set({ firstName: 'Jane', lastName: 'Doe' } as UserResponse);
    fixture.detectChanges();

    expect(component.initials()).toBe('JD');
  });

  it('should not fetch the current user on init when there is no external id', () => {
    expect(usersService.getUserByExternalKey).not.toHaveBeenCalled();
  });

  it('should fetch the current user on init when an external id is present', async () => {
    authService.getExternalId.mockReturnValue('ext-123');
    usersService.getUserByExternalKey.mockReturnValue(of({} as UserResponse));

    const localFixture = TestBed.createComponent(Toolbar);
    localFixture.detectChanges();

    expect(usersService.getUserByExternalKey).toHaveBeenCalledWith('ext-123');
  });

  it('should emit toggleSidenav when onToggleSidenav is called', () => {
    const emitSpy = vi.fn();
    component.toggleSidenav.subscribe(emitSpy);

    component.onToggleSidenav();

    expect(emitSpy).toHaveBeenCalledOnce();
  });

  it('should navigate to login after a successful logout', () => {
    authService.logout.mockReturnValue(of(undefined));

    component.logout();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/auth/login');
  });

  it('should show an error snackbar when logout fails', () => {
    authService.logout.mockReturnValue(throwError(() => new Error('network error')));

    component.logout();

    expect(snackbarService.showError).toHaveBeenCalledWith(
      'An error occurred trying to log you out. Try again later',
    );
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });
});
