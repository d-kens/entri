import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { EventsService } from '@features/events/services/events-service';
import { CategoryResponse } from '@features/events/models/event.models';
import { SnackbarService } from '@shared/services/snackbar-service';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';
import { CategoryFormDialog } from '@features/categories/category-form-dialog/category-form-dialog';
import { DataTable } from '@shared/components/data-table/data-table';

@Component({
  selector: 'app-categories-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatMenuModule, DataTable],
  templateUrl: './categories-list.html',
  styleUrl: './categories-list.css',
})
export class CategoriesList implements OnInit {
  private eventsService = inject(EventsService);
  private snackbarService = inject(SnackbarService);
  private dialog = inject(MatDialog);

  loading = signal(true);
  error = signal(false);
  categories = signal<CategoryResponse[]>([]);

  readonly columns = ['name', 'description', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(CategoryFormDialog, {
      width: '480px',
      data: {},
    });
    ref.afterClosed().subscribe((result) => {
      if (!result) return;
      this.eventsService.createCategory(result).subscribe({
        next: () => {
          this.snackbarService.showSuccess('Category created');
          this.load();
        },
        error: (err: Error) => this.snackbarService.showError(err.message),
      });
    });
  }

  openEditDialog(category: CategoryResponse): void {
    const ref = this.dialog.open(CategoryFormDialog, {
      width: '480px',
      data: { category },
    });
    ref.afterClosed().subscribe((result) => {
      if (!result) return;
      this.eventsService.updateCategory(category.id, result).subscribe({
        next: () => {
          this.snackbarService.showSuccess('Category updated');
          this.load();
        },
        error: (err: Error) => this.snackbarService.showError(err.message),
      });
    });
  }

  confirmDelete(category: CategoryResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { message: `Delete "${category.name}"? This cannot be undone.` },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.eventsService.deleteCategory(category.id).subscribe({
        next: () => {
          this.snackbarService.showSuccess('Category deleted');
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
    this.eventsService.getCategories().subscribe({
      next: (data) => {
        this.categories.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
