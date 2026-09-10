import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { CheckInCodeDialog } from './check-in-code-dialog';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CheckInCodeResponse } from '@features/events/models/event.models';

describe('CheckInCodeDialog', () => {
  let component: CheckInCodeDialog;
  let fixture: ComponentFixture<CheckInCodeDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };
  let snackbarService: { showSuccess: ReturnType<typeof vi.fn> };
  const data: CheckInCodeResponse = {
    code: 'ABC123',
    eventExternalId: 'event-1',
    expiresAt: new Date().toISOString(),
  };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };
    snackbarService = { showSuccess: vi.fn() };

    Object.assign(navigator, {
      clipboard: { writeText: vi.fn(() => Promise.resolve()) },
    });

    await TestBed.configureTestingModule({
      imports: [CheckInCodeDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CheckInCodeDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should copy the code to the clipboard and show a success snackbar', async () => {
    await component.copyCode();

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith('ABC123');
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Code copied to clipboard');
  });

  it('should close the dialog', () => {
    component.close();

    expect(dialogRef.close).toHaveBeenCalledOnce();
  });
});
