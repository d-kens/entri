package com.entri.wallet.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.security.UserPrincipal;
import com.entri.wallet.WalletService;
import com.entri.wallet.controller.api.WalletApi;
import com.entri.wallet.dto.BankCodeResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawRequest;
import com.entri.wallet.dto.WithdrawResponse;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public WalletResponse createWallet(final String externalKey) {
        return walletService.createWalletForUser(externalKey);
    }

    @Override
    public WalletResponse getWallet(final String externalKey, final UserPrincipal requestingUser) {
        return walletService.getWallet(externalKey, requestingUser);
    }

    @Override
    public WithdrawResponse withdraw(final String externalKey, final WithdrawRequest request, final UserPrincipal requestingUser) {
        return walletService.withdraw(externalKey, request, requestingUser);
    }

    @Override
    public List<BankCodeResponse> getBankCodes() {
        return walletService.getBankCodes();
    }

    @Override
    public PaginationResponse<WalletTransactionResponse> getTransactions(
            final String externalKey, final int page, final int pageSize, final UserPrincipal requestingUser) {
        return walletService.getTransactions(externalKey, page, pageSize, requestingUser);
    }
}
