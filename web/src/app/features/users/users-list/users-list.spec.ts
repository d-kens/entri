import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { of, throwError } from 'rxjs';

import { UsersList } from './users-list';
import { UsersService } from '@features/users/users-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { UserResponse } from '@features/users/models/user.models';
import { PageResponse } from '@shared/models/common.model';

function buildUser(overrides: Partial<UserResponse> = {}): UserResponse {
  return {
    role: 'ORGANIZER',
    email: 'jane@example.com',
    externalKey: 'u1',
    firstName: 'Jane',
    lastName: 'Doe',
    phoneNumber: '0712345678',
    enabled: true,
    ...overrides,
  };
}

function buildPage(
  overrides: Partial<PageResponse<UserResponse>> = {},
): PageResponse<UserResponse> {
  return {
    content: [buildUser()],
    number: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    ...overrides,
  };
}

describe('UsersList', () => {
  let component: UsersList;
  let fixture: ComponentFixture<UsersList>;
  let usersService: {
    getUsers: ReturnType<typeof vi.fn>;
    enableUser: ReturnType<typeof vi.fn>;
    disableUser: ReturnType<typeof vi.fn>;
    deleteUser: ReturnType<typeof vi.fn>;
  };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };
  let dialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    usersService = {
      getUsers: vi.fn().mockReturnValue(of(buildPage())),
      enableUser: vi.fn(),
      disableUser: vi.fn(),
      deleteUser: vi.fn(),
    };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };
    dialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [UsersList],
      providers: [
        { provide: UsersService, useValue: usersService },
        { provide: SnackbarService, useValue: snackbarService },
        { provide: MatDialog, useValue: dialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UsersList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load users on init', () => {
    expect(usersService.getUsers).toHaveBeenCalledWith(0, 20);
    expect(component.users().length).toBe(1);
    expect(component.totalElements()).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should set the error state when loading fails', async () => {
    usersService.getUsers.mockReturnValue(throwError(() => new Error('boom')));

    component.retry();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should update page index and size on page change and reload', () => {
    usersService.getUsers.mockClear();
    const pageEvent = { pageIndex: 1, pageSize: 50 } as PageEvent;

    component.onPageChange(pageEvent);

    expect(component.pageIndex()).toBe(1);
    expect(component.pageSize()).toBe(50);
    expect(usersService.getUsers).toHaveBeenCalledWith(1, 50);
  });

  it('should disable an enabled user and show a success message', () => {
    usersService.disableUser.mockReturnValue(of(buildUser({ enabled: false })));
    const user = buildUser({ enabled: true });

    component.toggleStatus(user);

    expect(usersService.disableUser).toHaveBeenCalledWith('u1');
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('User disabled');
  });

  it('should enable a disabled user and show a success message', () => {
    usersService.enableUser.mockReturnValue(of(buildUser({ enabled: true })));
    const user = buildUser({ enabled: false });

    component.toggleStatus(user);

    expect(usersService.enableUser).toHaveBeenCalledWith('u1');
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('User enabled');
  });

  it('should show an error message when toggling status fails', () => {
    usersService.disableUser.mockReturnValue(throwError(() => new Error('cannot disable')));

    component.toggleStatus(buildUser({ enabled: true }));

    expect(snackbarService.showError).toHaveBeenCalledWith('cannot disable');
  });

  it('should delete the user when the confirm dialog is accepted', () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    usersService.deleteUser.mockReturnValue(of(undefined));

    component.confirmDelete(buildUser());

    expect(dialog.open).toHaveBeenCalled();
    expect(usersService.deleteUser).toHaveBeenCalledWith('u1');
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('User deleted');
  });

  it('should not delete the user when the confirm dialog is dismissed', () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });

    component.confirmDelete(buildUser());

    expect(usersService.deleteUser).not.toHaveBeenCalled();
  });

  it('should show an error message when deletion fails', () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    usersService.deleteUser.mockReturnValue(throwError(() => new Error('cannot delete')));

    component.confirmDelete(buildUser());

    expect(snackbarService.showError).toHaveBeenCalledWith('cannot delete');
  });
});
