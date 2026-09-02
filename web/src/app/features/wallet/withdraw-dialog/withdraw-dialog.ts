import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DecimalPipe } from '@angular/common';
import { WalletService } from '@features/wallet/wallet-service';
import {
  BankCodeResponse,
  WalletResponse,
  WithdrawRequest,
  WithdrawType,
} from '@features/wallet/models/wallet.models';

export interface WithdrawDialogData {
  wallet: WalletResponse;
}

@Component({
  selector: 'app-withdraw-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    DecimalPipe,
  ],
  templateUrl: './withdraw-dialog.html',
  styleUrl: './withdraw-dialog.css',
})
export class WithdrawDialog implements OnInit {
  data = inject<WithdrawDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<WithdrawDialog>);
  private walletService = inject(WalletService);

  bankCodes = this.walletService.bankCodes;
  bankCodesLoading = signal(false);
  submitting = signal(false);
  submitError = signal('');

  form = new FormGroup({
    type: new FormControl<WithdrawType>('MPESA_PAYBILL', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    recipientName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    account: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    accountNumber: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    bankCode: new FormControl<string | null>(null),
  });

  get isMpesa(): boolean {
    return this.form.controls.type.value === 'MPESA_PAYBILL';
  }

  get isBank(): boolean {
    return this.form.controls.type.value === 'BANK';
  }

  ngOnInit(): void {
    if (this.bankCodes().length === 0) {
      this.bankCodesLoading.set(true);
      this.walletService.getBankCodes().subscribe({
        complete: () => this.bankCodesLoading.set(false),
        error: () => this.bankCodesLoading.set(false),
      });
    }

    this.form.controls.type.valueChanges.subscribe((type) => {
      this.submitError.set('');
      const { accountNumber, bankCode } = this.form.controls;
      if (type === 'MPESA_PAYBILL') {
        accountNumber.setValidators([Validators.required]);
        bankCode.clearValidators();
        bankCode.setValue(null);
      } else {
        bankCode.setValidators([Validators.required]);
        accountNumber.clearValidators();
        accountNumber.setValue('');
      }
      accountNumber.updateValueAndValidity();
      bankCode.updateValueAndValidity();
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { type, amount, account, accountNumber, bankCode, recipientName } =
      this.form.getRawValue();
    const request: WithdrawRequest = {
      type,
      amount: amount!,
      account,
      recipientName,
      ...(type === 'MPESA_PAYBILL' ? { accountNumber } : { bankCode: bankCode! }),
    };

    this.submitting.set(true);
    this.submitError.set('');

    this.walletService.withdraw(request).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.dialogRef.close(res);
      },
      error: (err: Error) => {
        this.submitting.set(false);
        this.submitError.set(err.message);
      },
    });
  }
}
