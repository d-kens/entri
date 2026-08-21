package com.entri.wallet;

import com.entri.integrations.intasend.IntaSendClient;
import com.entri.integrations.intasend.IntaSendWalletRequest;
import com.entri.shared.exception.ResourceNotFoundException;
import com.entri.shared.security.AuthenticatedUser;
import com.entri.users.service.UserService;
import com.entri.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final IntaSendClient intaSendClient;
    private final UserService userService;

    public String createWallet(final String label, final String currency) {
        var sanitizedLabel = label.replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        var request = new IntaSendWalletRequest("SETTLEMENT", currency, sanitizedLabel, true);
        return intaSendClient.createWallet(request).walletId();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(final AuthenticatedUser requestingUser) {
        var user = userService.findEntityByExternalKey(requestingUser.userExternalKey());
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
