import { Injectable } from '@angular/core';
import {MatSnackBar, MatSnackBarConfig} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  /**
   * Show success message with green styling
   */
  showSuccess(message: string, duration: number = 3000) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }

  /**
   * Show error message with red styling
   */
  showError(message: string, duration: number = 5000) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }

  /**
   * Show info message with default styling
   */
  showInfo(message: string, duration: number = 3000) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    };

    this.snackBar.open(message, 'Close', config);
  }

  /**
   * Show warning message with custom styling (optional)
   */
  showWarning(message: string, duration: number = 4000) {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['warning-snackbar']
    };

    this.snackBar.open(message, 'Close', config);
  }
}
