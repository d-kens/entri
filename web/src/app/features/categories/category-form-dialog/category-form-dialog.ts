import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CategoryResponse } from '@features/events/models/event.models';

export interface CategoryFormDialogData {
  category?: CategoryResponse;
}

@Component({
  selector: 'app-category-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './category-form-dialog.html',
})
export class CategoryFormDialog {
  data = inject<CategoryFormDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<CategoryFormDialog>);

  isEdit = !!this.data?.category;

  form = new FormGroup({
    name: new FormControl(this.data?.category?.name ?? '', [
      Validators.required,
      Validators.maxLength(100),
    ]),
    description: new FormControl(this.data?.category?.description ?? '', [Validators.required]),
  });

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.value);
  }
}
