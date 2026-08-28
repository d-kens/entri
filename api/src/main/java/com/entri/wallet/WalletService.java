package com.entri.wallet;

import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.service.UserService;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.exception.WalletAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletProvider walletProvider;
    private final UserService userService;

    @Transactional
    public WalletResponse createWalletForUser(final String externalKey, final UserPrincipal requestingUser) {
        var user = userService.findEntityByExternalKey(externalKey);
        if (!requestingUser.isAdmin() && !requestingUser.getExternalKey().equals(externalKey)) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
        if (user.getWalletId() != null) {
            throw new WalletAlreadyExistsException();
        }
        String sanitizedLabel = (user.getFirstName() + " " + user.getLastName())
                .replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        String walletId = walletProvider.createWallet(sanitizedLabel);
        userService.assignWalletId(user, walletId);
        return walletProvider.getWallet(walletId);
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
