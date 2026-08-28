package com.entri.wallet;

import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.WalletProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletProvider walletProvider;

    public String createWallet(final String label, final String currency) {
        var sanitizedLabel = label.replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        return walletProvider.createWallet(sanitizedLabel, currency);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(final UserPrincipal requestingUser) {
        var user = requestingUser.getUser();
        if (user.getWalletId() == null) {
            throw new ResourceNotFoundException("Wallet not found");
        }
        return walletProvider.getWallet(user.getWalletId());
    }
}
