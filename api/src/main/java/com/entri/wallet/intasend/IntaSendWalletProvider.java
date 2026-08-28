package com.entri.wallet.intasend;

import com.entri.intasend.IntaSendClient;
import com.entri.intasend.dto.IntaSendWalletRequest;
import com.entri.wallet.WalletProvider;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntaSendWalletProvider implements WalletProvider {

    private final IntaSendClient intaSendClient;

    @Override
    public String createWallet(final String label) {
        var request = new IntaSendWalletRequest("WORKING", "KES", label, true);
        var response = intaSendClient.createWallet(request);
        return response.walletId();
    }

    @Override
    public WalletResponse getWallet(final String walletId) {
        var response = intaSendClient.getWallet(walletId);
        return new WalletResponse(
                response.walletId(),
                response.label(),
                response.currency(),
                response.currentBalance(),
                response.availableBalance()
        );
    }
}
