package com.entri.wallet.controller;

import com.entri.shared.security.AuthenticatedUser;
import com.entri.wallet.WalletService;
import com.entri.wallet.controller.api.WalletApi;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public WalletResponse getWallet(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return walletService.getWallet(user);
    }
}
