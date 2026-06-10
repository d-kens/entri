import { Injectable } from '@angular/core';
import {MatSnackBar, MatSnackBarConfig} from '@angular/material/snack-bar';

const SNACKBAR_DURATION = {
  success: 3000,
  error:   5000,
  info:    3000,
  warning: 4000,
} as const;

@Injectable({
  providedIn: 'root',
})
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  showSuccess(message: string, duration = SNACKBAR_DURATION.success) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }

  showError(message: string, duration = SNACKBAR_DURATION.error) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }

  showInfo(message: string, duration = SNACKBAR_DURATION.info) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    };

    this.snackBar.open(message, 'Close', config);
  }

  showWarning(message: string, duration = SNACKBAR_DURATION.warning) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['warning-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }
}
