import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';

import { SnackbarService } from './snackbar-service';
import { SnackbarComponent } from '@shared/components/snackbar/snackbar.component';

describe('SnackbarService', () => {
  let service: SnackbarService;
  let snackBarMock: { openFromComponent: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    snackBarMock = { openFromComponent: vi.fn() };
    TestBed.configureTestingModule({
      providers: [SnackbarService, { provide: MatSnackBar, useValue: snackBarMock }],
    });
    service = TestBed.inject(SnackbarService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('shows a success message with the success duration', () => {
    service.showSuccess('Saved!');

    expect(snackBarMock.openFromComponent).toHaveBeenCalledWith(
      SnackbarComponent,
      expect.objectContaining({
        duration: 3000,
        panelClass: ['success-snackbar'],
        horizontalPosition: 'end',
        verticalPosition: 'top',
        data: { message: 'Saved!', type: 'success' },
      }),
    );
  });

  it('shows an error message with the error duration and an optional action', () => {
    const action = { label: 'Retry', callback: vi.fn() };

    service.showError('Something broke', action);

    expect(snackBarMock.openFromComponent).toHaveBeenCalledWith(
      SnackbarComponent,
      expect.objectContaining({
        duration: 5000,
        panelClass: ['error-snackbar'],
        data: { message: 'Something broke', type: 'error', action },
      }),
    );
  });

  it('shows a warning message with the warning duration', () => {
    service.showWarning('Careful');

    expect(snackBarMock.openFromComponent).toHaveBeenCalledWith(
      SnackbarComponent,
      expect.objectContaining({
        duration: 4000,
        panelClass: ['warning-snackbar'],
        data: { message: 'Careful', type: 'warning' },
      }),
    );
  });

  it('shows an info message with the info duration', () => {
    service.showInfo('FYI');

    expect(snackBarMock.openFromComponent).toHaveBeenCalledWith(
      SnackbarComponent,
      expect.objectContaining({
        duration: 3000,
        panelClass: ['info-snackbar'],
        data: { message: 'FYI', type: 'info' },
      }),
    );
  });
});
