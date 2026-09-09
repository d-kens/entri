import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageEvent } from '@angular/material/paginator';
import { of, throwError } from 'rxjs';

import { Wallet } from './wallet';
import { WalletService } from './wallet-service';
import { AuthService } from '@features/auth/auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { WalletResponse, WalletTransaction } from './models/wallet.models';
import { PageResponse } from '@shared/models/common.model';

function buildWallet(overrides: Partial<WalletResponse> = {}): WalletResponse {
  return { externalId: 'w1', balance: 1000, ...overrides };
}

function buildTransactionsPage(
  overrides: Partial<PageResponse<WalletTransaction>> = {},
): PageResponse<WalletTransaction> {
  return {
    content: [],
    number: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    ...overrides,
  };
}

describe('Wallet', () => {
  let component: Wallet;
  let fixture: ComponentFixture<Wallet>;
  let walletService: {
    getWallet: ReturnType<typeof vi.fn>;
    getTransactions: ReturnType<typeof vi.fn>;
    withdraw: ReturnType<typeof vi.fn>;
  };
  let authService: { getExternalId: ReturnType<typeof vi.fn> };
  let snackbarService: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    walletService = {
      getWallet: vi.fn().mockReturnValue(of(buildWallet())),
      getTransactions: vi.fn().mockReturnValue(of(buildTransactionsPage())),
      withdraw: vi.fn(),
    };
    authService = { getExternalId: vi.fn().mockReturnValue('org-1') };
    snackbarService = { showSuccess: vi.fn(), showError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Wallet],
      providers: [
        { provide: WalletService, useValue: walletService },
        { provide: AuthService, useValue: authService },
        { provide: SnackbarService, useValue: snackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Wallet);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load the wallet and transactions for the current organizer on init', () => {
    expect(walletService.getWallet).toHaveBeenCalledWith('org-1');
    expect(walletService.getTransactions).toHaveBeenCalledWith('org-1', 0, 20);
    expect(component.wallet()).toEqual(buildWallet());
    expect(component.walletLoading()).toBe(false);
    expect(component.transactionsLoading()).toBe(false);
  });

  it('should stop the wallet loading spinner when the wallet request fails', async () => {
    walletService.getWallet.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect(component.walletLoading()).toBe(false);
  });

  it('should mark transactions as errored when the request fails', async () => {
    walletService.getTransactions.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect(component.transactionsError()).toBe(true);
    expect(component.transactionsLoading()).toBe(false);
  });

  it('should toggle the withdraw form visibility', () => {
    expect(component.showWithdrawForm()).toBe(false);

    component.toggleWithdrawForm();
    expect(component.showWithdrawForm()).toBe(true);

    component.toggleWithdrawForm();
    expect(component.showWithdrawForm()).toBe(false);
  });

  it('should reload transactions for the requested page', () => {
    walletService.getTransactions.mockClear();
    const pageEvent = { pageIndex: 2, pageSize: 5 } as PageEvent;

    component.onPage(pageEvent);

    expect(walletService.getTransactions).toHaveBeenCalledWith('org-1', 2, 5);
  });

  it('should submit a withdrawal with the mapped payload and reset the form on success', async () => {
    walletService.withdraw.mockReturnValue(of({ externalId: 'wd-1', status: 'PENDING' as const }));
    (component as unknown as { withdrawData: { set: (v: unknown) => void } }).withdrawData.set({
      amount: 500,
      accountType: 'PAYBILL',
      name: 'John Doe',
      account: '123456',
      narrative: 'Payout',
      accountReference: 'REF1',
      bankCode: '',
    });
    component.showWithdrawForm.set(true);

    await component.onSubmit(new Event('submit'));

    expect(walletService.withdraw).toHaveBeenCalledWith('w1', {
      amount: 500,
      accountType: 'PAYBILL',
      name: 'John Doe',
      account: '123456',
      narrative: 'Payout',
      accountReference: 'REF1',
      bankCode: undefined,
    });
    expect(snackbarService.showSuccess).toHaveBeenCalledWith('Withdrawal initiated successfully');
    expect(component.showWithdrawForm()).toBe(false);
    expect(component.withdrawLoading()).toBe(false);
  });

  it('should show an error message when the withdrawal fails', async () => {
    walletService.withdraw.mockReturnValue(
      throwError(() => ({ error: { detail: 'Insufficient balance' } })),
    );
    (component as unknown as { withdrawData: { set: (v: unknown) => void } }).withdrawData.set({
      amount: 500,
      accountType: 'PAYBILL',
      name: 'John Doe',
      account: '123456',
      narrative: 'Payout',
      accountReference: 'REF1',
      bankCode: '',
    });

    await component.onSubmit(new Event('submit'));

    expect(snackbarService.showError).toHaveBeenCalledWith('Insufficient balance');
    expect(component.withdrawLoading()).toBe(false);
  });
});
