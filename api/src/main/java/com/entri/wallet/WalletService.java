package com.entri.wallet;

import com.entri.integrations.intasend.IntaSendClient;
import com.entri.integrations.intasend.IntaSendWalletRequest;
import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final IntaSendClient intaSendClient;

    public String createWallet(final String label, final String currency) {
        var sanitizedLabel = label.replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        var request = new IntaSendWalletRequest("SETTLEMENT", currency, sanitizedLabel, true);
        return intaSendClient.createWallet(request).walletId();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(final UserPrincipal requestingUser) {
        var user = requestingUser.getUser();
        if (user.getWalletId() == null) {
            throw new ResourceNotFoundException("Wallet not found");
        }
        var intaSendWallet = intaSendClient.getWallet(user.getWalletId());
        return new WalletResponse(
                intaSendWallet.walletId(),
                intaSendWallet.label(),
                intaSendWallet.currency(),
                intaSendWallet.currentBalance(),
                intaSendWallet.availableBalance()
        );
    }
}
