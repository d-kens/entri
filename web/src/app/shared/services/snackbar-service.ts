import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { SnackbarComponent, SnackbarData } from '@shared/components/snackbar/snackbar.component';

const DURATION = {
  success: 3000,
  error: 5000,
  info: 3000,
  warning: 4000,
} as const;

const BASE_CONFIG: MatSnackBarConfig = {
  horizontalPosition: 'end',
  verticalPosition: 'top',
};

@Injectable({ providedIn: 'root' })
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  showSuccess(message: string) {
    this.open({ message, type: 'success' });
  }

  showError(message: string, action?: { label: string; callback: () => void }) {
    this.open({ message, type: 'error', action });
  }

  showWarning(message: string) {
    this.open({ message, type: 'warning' });
  }

  showInfo(message: string) {
    this.open({ message, type: 'info' });
  }

  private open(data: SnackbarData) {
    this.snackBar.openFromComponent(SnackbarComponent, {
      ...BASE_CONFIG,
      duration: DURATION[data.type],
      panelClass: [`${data.type}-snackbar`],
      data,
    });
  }
}
