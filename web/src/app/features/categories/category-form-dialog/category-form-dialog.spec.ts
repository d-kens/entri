import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { CategoryFormDialog, CategoryFormDialogData } from './category-form-dialog';
import { CategoryResponse } from '@features/events/models/event.models';

describe('CategoryFormDialog', () => {
  let component: CategoryFormDialog;
  let fixture: ComponentFixture<CategoryFormDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  async function setup(data: CategoryFormDialogData) {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CategoryFormDialog],
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryFormDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create in create mode when no category is provided', async () => {
    await setup({});
    expect(component).toBeTruthy();
    expect(component.isEdit).toBe(false);
    expect(component.form.value).toEqual({ name: '', description: '' });
  });

  it('should pre-populate the form in edit mode', async () => {
    const category: CategoryResponse = { id: 1, name: 'Music', description: 'Music events' };
    await setup({ category });

    expect(component.isEdit).toBe(true);
    expect(component.form.value).toEqual({ name: 'Music', description: 'Music events' });
  });

  it('should close the dialog with no result on cancel', async () => {
    await setup({});
    component.cancel();
    expect(dialogRef.close).toHaveBeenCalledWith();
  });

  it('should not close with a result when the form is invalid', async () => {
    await setup({});
    component.submit();
    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.form.controls.name.touched).toBe(true);
    expect(component.form.controls.description.touched).toBe(true);
  });

  it('should close the dialog with the form value when valid', async () => {
    await setup({});
    component.form.setValue({ name: 'Sports', description: 'Sports events' });

    component.submit();

    expect(dialogRef.close).toHaveBeenCalledWith({ name: 'Sports', description: 'Sports events' });
  });

  it('should reject a name longer than 100 characters', async () => {
    await setup({});
    component.form.setValue({ name: 'a'.repeat(101), description: 'desc' });

    component.submit();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.form.controls.name.hasError('maxlength')).toBe(true);
  });
});
