package com.entri.wallet;

import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public WalletResponse getWallet(final String organizerExternalKey) {
        return walletService.getWallet(organizerExternalKey);
    }
}
