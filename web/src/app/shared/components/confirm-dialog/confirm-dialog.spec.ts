import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ConfirmDialog, ConfirmDialogData } from './confirm-dialog';

describe('ConfirmDialog', () => {
  let component: ConfirmDialog;
  let fixture: ComponentFixture<ConfirmDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };
  const data: ConfirmDialogData = { message: 'Are you sure you want to delete this?' };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ConfirmDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the provided title, message and confirm label', async () => {
    const customData: ConfirmDialogData = {
      title: 'Delete event',
      message: 'This cannot be undone.',
      confirmLabel: 'Delete it',
    };

    await TestBed.resetTestingModule()
      .configureTestingModule({
        imports: [ConfirmDialog],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: customData },
          { provide: MatDialogRef, useValue: dialogRef },
        ],
      })
      .compileComponents();

    const customFixture = TestBed.createComponent(ConfirmDialog);
    customFixture.detectChanges();

    const text = customFixture.nativeElement.textContent;
    expect(text).toContain('Delete event');
    expect(text).toContain('This cannot be undone.');
    expect(text).toContain('Delete it');
  });

  it('should fall back to default title and confirm label when not provided', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Are you sure?');
    expect(text).toContain('Delete');
  });

  it('should close with false on cancel', () => {
    component.cancel();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('should close with true on confirm', () => {
    component.confirm();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });
});
