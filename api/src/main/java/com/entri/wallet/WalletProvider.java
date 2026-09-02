package com.entri.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.wallet.dto.BankCodeResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawRequest;
import com.entri.wallet.dto.WithdrawResponse;
import com.entri.wallet.dto.WalletResponse;

import java.util.List;

public interface WalletProvider {
    String createWallet(String label);
    WalletResponse getWallet(String walletId);
    WithdrawResponse withdraw(WithdrawRequest request, String walletId);
    List<BankCodeResponse> getBankCodes();
    PaginationResponse<WalletTransactionResponse> getTransactions(String walletId, int page, int pageSize);
}
