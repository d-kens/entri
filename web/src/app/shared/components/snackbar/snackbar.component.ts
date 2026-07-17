import { Component, inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { MatButton } from '@angular/material/button';

export interface SnackbarData {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  action?: { label: string; callback: () => void };
}

const ICONS: Record<SnackbarData['type'], string> = {
  success: 'check_circle',
  error: 'error',
  warning: 'warning',
  info: 'info',
};

@Component({
  selector: 'app-snackbar',
  imports: [MatIcon, MatIconButton, MatButton],
  templateUrl: './snackbar.component.html',
  styleUrl: './snackbar.component.css',
})
export class SnackbarComponent {
  data = inject<SnackbarData>(MAT_SNACK_BAR_DATA);
  ref = inject(MatSnackBarRef);

  icon = ICONS[this.data.type];

  onAction() {
    this.data.action?.callback();
    this.ref.dismiss();
  }

  dismiss() {
    this.ref.dismiss();
  }
}
