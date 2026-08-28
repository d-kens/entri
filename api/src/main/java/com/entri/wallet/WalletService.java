package com.entri.wallet;

import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.User;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.exception.WalletAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletProvider walletProvider;
    private final UserRepository userRepository;

    @Transactional
    public WalletResponse createWalletForUser(final String externalKey) {
        var user = userRepository.findByExternalKey(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWalletId() != null) {
            throw new WalletAlreadyExistsException();
        }
        provisionWallet(user);
        return walletProvider.getWallet(user.getWalletId());
    }

    @Transactional
    public void provisionWallet(final User user) {
        String sanitizedLabel = (user.getFirstName() + " " + user.getLastName())
                .replaceAll("[^a-zA-Z0-9_\\- ]", "").strip();
        String walletId = walletProvider.createWallet(sanitizedLabel);
        user.setWalletId(walletId);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(final String externalKey, final UserPrincipal requestingUser) {
        if (!requestingUser.isAdmin() && !requestingUser.getExternalKey().equals(externalKey)) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
        var user = userRepository.findByExternalKey(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWalletId() == null) {
            throw new ResourceNotFoundException("Wallet not found");
        }
        return walletProvider.getWallet(user.getWalletId());
    }
}
