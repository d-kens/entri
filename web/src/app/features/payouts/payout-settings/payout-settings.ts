import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { PayoutAccountsService } from '../payout-accounts-service';
import { PayoutAccountResponse, PayoutMethod } from '../models/payout.models';

@Component({
  selector: 'app-payout-settings',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatChipsModule,
  ],
  templateUrl: './payout-settings.html',
  styleUrl: './payout-settings.css',
})
export class PayoutSettings implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private payoutService = inject(PayoutAccountsService);
  private snackbar = inject(SnackbarService);

  accounts = signal<PayoutAccountResponse[]>([]);
  loading = signal(false);
  saving = signal(false);
  showForm = signal(false);
  error = signal<string | null>(null);

  readonly methodOptions: { value: PayoutMethod; label: string }[] = [
    { value: 'MPESA_PAYBILL', label: 'M-Pesa Paybill' },
    { value: 'MPESA_TILL', label: 'M-Pesa Till' },
    { value: 'BANK', label: 'Bank Transfer' },
  ];

  addForm = this.fb.group({
    method: ['MPESA_PAYBILL' as PayoutMethod, Validators.required],
    recipientName: ['', Validators.required],
    account: ['', Validators.required],
    accountReference: [''],
    bankCode: [''],
    isDefault: [false],
  });

  selectedMethod = computed(() => this.addForm.get('method')?.value as PayoutMethod);

  get accountLabel(): string {
    const method = this.addForm.get('method')?.value;
    if (method === 'MPESA_PAYBILL') return 'Paybill Number';
    if (method === 'MPESA_TILL') return 'Till Number';
    return 'Bank Account Number';
  }

  get isPaybill(): boolean {
    return this.addForm.get('method')?.value === 'MPESA_PAYBILL';
  }

  get isBank(): boolean {
    return this.addForm.get('method')?.value === 'BANK';
  }

  get isFirstAccount(): boolean {
    return this.accounts().length === 0;
  }

  ngOnInit(): void {
    this.loadAccounts();

    this.addForm.get('method')?.valueChanges.subscribe(() => {
      this.updateConditionalValidators();
    });
  }

  private updateConditionalValidators(): void {
    const accountReferenceCtrl = this.addForm.get('accountReference');
    const bankCodeCtrl = this.addForm.get('bankCode');

    if (this.isPaybill) {
      accountReferenceCtrl?.setValidators(Validators.required);
      bankCodeCtrl?.clearValidators();
    } else if (this.isBank) {
      bankCodeCtrl?.setValidators(Validators.required);
      accountReferenceCtrl?.clearValidators();
    } else {
      accountReferenceCtrl?.clearValidators();
      bankCodeCtrl?.clearValidators();
    }

    accountReferenceCtrl?.updateValueAndValidity();
    bankCodeCtrl?.updateValueAndValidity();
  }

  private loadAccounts(): void {
    const organizerKey = this.authService.getExternalId();
    if (!organizerKey) return;

    this.loading.set(true);
    this.error.set(null);

    this.payoutService.getAccounts(organizerKey).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.loading.set(false);
        this.syncDefaultCheckbox();
      },
      error: (err) => {
        const msg = err?.error?.detail || err?.error?.message || 'Failed to load payout accounts';
        this.error.set(msg);
        this.loading.set(false);
      },
    });
  }

  private syncDefaultCheckbox(): void {
    const isFirst = this.accounts().length === 0;
    if (isFirst) {
      this.addForm.get('isDefault')?.setValue(true);
      this.addForm.get('isDefault')?.disable();
    } else {
      this.addForm.get('isDefault')?.enable();
    }
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    if (!this.showForm()) {
      this.resetForm();
    } else {
      this.syncDefaultCheckbox();
      this.updateConditionalValidators();
    }
  }

  private resetForm(): void {
    this.addForm.reset({
      method: 'MPESA_PAYBILL',
      recipientName: '',
      account: '',
      accountReference: '',
      bankCode: '',
      isDefault: false,
    });
    this.addForm.get('isDefault')?.enable();
  }

  submit(): void {
    this.updateConditionalValidators();
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }

    const organizerKey = this.authService.getExternalId();
    if (!organizerKey) return;

    const { method, recipientName, account, accountReference, bankCode } = this.addForm.value;
    const isDefaultValue = this.addForm.get('isDefault')?.value ?? false;

    const request = {
      method: method as PayoutMethod,
      recipientName: recipientName!,
      account: account!,
      isDefault: isDefaultValue,
      ...(this.isPaybill && accountReference ? { accountReference } : {}),
      ...(this.isBank && bankCode ? { bankCode } : {}),
    };

    this.saving.set(true);

    this.payoutService.addAccount(organizerKey, request).subscribe({
      next: () => {
        this.snackbar.showSuccess('Payout account added successfully');
        this.saving.set(false);
        this.showForm.set(false);
        this.resetForm();
        this.loadAccounts();
      },
      error: (err) => {
        const msg = err?.error?.detail || err?.error?.message || 'Failed to add payout account';
        this.snackbar.showError(msg);
        this.saving.set(false);
      },
    });
  }

  setDefault(account: PayoutAccountResponse): void {
    const organizerKey = this.authService.getExternalId();
    if (!organizerKey) return;

    this.payoutService.setDefault(organizerKey, account.externalId).subscribe({
      next: () => {
        this.snackbar.showSuccess(`${account.recipientName} set as default`);
        this.loadAccounts();
      },
      error: (err) => {
        const msg = err?.error?.detail || err?.error?.message || 'Failed to update default account';
        this.snackbar.showError(msg);
      },
    });
  }

  deleteAccount(account: PayoutAccountResponse): void {
    const organizerKey = this.authService.getExternalId();
    if (!organizerKey) return;

    this.payoutService.deleteAccount(organizerKey, account.externalId).subscribe({
      next: () => {
        this.snackbar.showSuccess('Payout account removed');
        this.loadAccounts();
      },
      error: (err) => {
        const msg = err?.error?.detail || err?.error?.message || 'Failed to delete payout account';
        this.snackbar.showError(msg);
      },
    });
  }

  methodLabel(method: PayoutMethod): string {
    const found = this.methodOptions.find((o) => o.value === method);
    return found ? found.label : method;
  }
}
