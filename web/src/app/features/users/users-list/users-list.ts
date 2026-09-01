import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { UsersService } from '@features/users/users-service';
import { UserResponse } from '@features/users/models/user.models';
import { SnackbarService } from '@shared/services/snackbar-service';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';
import { DataTable } from '@shared/components/data-table/data-table';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatMenuModule,
    MatPaginatorModule,
    DataTable,
  ],
  templateUrl: './users-list.html',
  styleUrl: './users-list.css',
})
export class UsersList implements OnInit {
  private usersService = inject(UsersService);
  private snackbarService = inject(SnackbarService);
  private dialog = inject(MatDialog);

  loading = signal(true);
  error = signal(false);
  users = signal<UserResponse[]>([]);
  totalElements = signal(0);
  pageSize = signal(20);
  pageIndex = signal(0);

  readonly columns = ['name', 'email', 'role', 'status', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  toggleStatus(user: UserResponse): void {
    const action$ = user.enabled
      ? this.usersService.disableUser(user.externalKey)
      : this.usersService.enableUser(user.externalKey);
    const label = user.enabled ? 'disabled' : 'enabled';

    action$.subscribe({
      next: () => {
        this.snackbarService.showSuccess(`User ${label}`);
        this.load();
      },
      error: (err: Error) => this.snackbarService.showError(err.message),
    });
  }

  confirmDelete(user: UserResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { message: `Delete "${user.firstName} ${user.lastName}"? This cannot be undone.` },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.usersService.deleteUser(user.externalKey).subscribe({
        next: () => {
          this.snackbarService.showSuccess('User deleted');
          this.load();
        },
        error: (err: Error) => this.snackbarService.showError(err.message),
      });
    });
  }

  retry(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.usersService.getUsers(this.pageIndex(), this.pageSize()).subscribe({
      next: (data) => {
        this.users.set(data.content);
        this.totalElements.set(data.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
