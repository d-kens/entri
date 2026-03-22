package com.parrcel.api.modules.wallet.service;

import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.users.service.UserService;
import com.parrcel.api.modules.wallet.entity.Wallet;
import com.parrcel.api.modules.wallet.entity.WalletTransaction;
import com.parrcel.api.modules.wallet.repository.WalletRepository;
import com.parrcel.api.modules.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserService userService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    private Wallet createWallet(User user) {
        var wallet = new Wallet();
        wallet.setUser(user);

        wallet = walletRepository.save(wallet);
        log.info("Wallet created for user: {}", user.getId());

        return wallet;
    }

    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Wallet not found for user: {}. Creating new wallet.", userId);
                    User user = userService.getUserById(userId);
                    return createWallet(user);
                });
    }

    @Transactional(readOnly = true)
    public Page<WalletTransaction> getWalletTransactions(Long walletId, Pageable pageable) {
        return walletTransactionRepository.findByWalletId(walletId, pageable);
    }
}
