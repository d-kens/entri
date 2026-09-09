import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { signal } from '@angular/core';

import { Profile } from './profile';
import { AuthService } from '@features/auth/auth-service';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { UserResponse } from '@features/users/models/user.models';

describe('Profile', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let authService: { getExternalId: ReturnType<typeof vi.fn> };
  let usersService: {
    currentUser: ReturnType<typeof vi.fn>;
    getUserByExternalKey: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
  };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  const user: UserResponse = {
    role: 'ORGANIZER',
    email: 'john@example.com',
    lastName: 'Doe',
    firstName: 'John',
    phoneNumber: '0712345678',
    externalKey: 'ext-key',
    enabled: true,
  } as UserResponse;

  async function setup() {
    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: UsersService, useValue: usersService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
  }

  beforeEach(() => {
    authService = { getExternalId: vi.fn().mockReturnValue('ext-key') };
    usersService = {
      currentUser: vi.fn().mockReturnValue(null),
      getUserByExternalKey: vi.fn().mockReturnValue(of(user)),
      updateUser: vi.fn(),
    };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };
  });

  it('should create', async () => {
    await setup();
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should patch the form from the cached current user without an HTTP call', async () => {
    usersService.currentUser.mockReturnValue(user);
    await setup();

    fixture.detectChanges();

    expect(usersService.getUserByExternalKey).not.toHaveBeenCalled();
    expect(component.profileForm.value.email).toBe('john@example.com');
  });

  it('should fetch the user by external key and patch the form when not cached', async () => {
    await setup();

    fixture.detectChanges();

    expect(usersService.getUserByExternalKey).toHaveBeenCalledWith('ext-key');
    expect(component.profileForm.value.firstName).toBe('John');
  });

  it('should not fetch when there is no external id and nothing cached', async () => {
    authService.getExternalId.mockReturnValue(null);
    await setup();

    fixture.detectChanges();

    expect(usersService.getUserByExternalKey).not.toHaveBeenCalled();
  });

  it('should not submit and should mark fields touched when form is invalid', async () => {
    await setup();
    fixture.detectChanges();
    component.profileForm.reset();

    component.save();

    expect(usersService.updateUser).not.toHaveBeenCalled();
    expect(component.profileForm.get('firstName')?.touched).toBe(true);
  });

  it('should submit mapped payload and show success on save', async () => {
    usersService.updateUser.mockReturnValue(of(user));
    await setup();
    fixture.detectChanges();

    component.save();

    expect(usersService.updateUser).toHaveBeenCalledWith('ext-key', {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
    });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Profile updated successfully');
    expect(component.isLoading()).toBe(false);
  });

  it('should show the server error message and stop loading on failure', async () => {
    usersService.updateUser.mockReturnValue(
      throwError(() => ({ error: { detail: 'Email already in use' } })),
    );
    await setup();
    fixture.detectChanges();

    component.save();

    expect(snackbarService.showError).toHaveBeenCalledWith('Email already in use');
    expect(component.isLoading()).toBe(false);
  });
});
