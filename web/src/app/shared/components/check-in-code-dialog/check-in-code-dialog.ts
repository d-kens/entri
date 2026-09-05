import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { EntriButton } from '@shared/components/button/entri-button.component';
import { SnackbarService } from '@shared/services/snackbar-service';
import { CheckInCodeResponse } from '@features/events/models/event.models';

@Component({
  selector: 'app-check-in-code-dialog',
  standalone: true,
  imports: [DatePipe, MatDialogModule, MatIconModule, EntriButton],
  templateUrl: './check-in-code-dialog.html',
  styleUrl: './check-in-code-dialog.css',
})
export class CheckInCodeDialog {
  data = inject<CheckInCodeResponse>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<CheckInCodeDialog>);
  private snackbarService = inject(SnackbarService);

  copyCode(): void {
    navigator.clipboard.writeText(this.data.code).then(() => {
      this.snackbarService.showSuccess('Code copied to clipboard');
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
