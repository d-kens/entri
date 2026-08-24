package com.entri.wallet.controller;

import com.entri.security.UserPrincipal;
import com.entri.wallet.WalletService;
import com.entri.wallet.controller.api.WalletApi;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public WalletResponse getWallet(final UserPrincipal requestingUser) {
        return walletService.getWallet(requestingUser);
    }
}
