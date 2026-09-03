package com.entri.wallet;

import com.entri.exception.ResourceNotFoundException;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.entity.Wallet;
import com.entri.wallet.entity.WalletTransaction;
import com.entri.wallet.repository.WalletRepository;
import com.entri.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createWallet(String organizerExternalKey) {
        var organizer = userRepository.findByExternalKeyAndDeletedFalse(organizerExternalKey)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + organizerExternalKey));

        var wallet = Wallet.builder()
                .organizer(organizer)
                .build();

        walletRepository.save(wallet);
    }

    @Transactional
    public void credit(String organizerExternalKey, BigDecimal amount, String currency, String referenceId) {
        if (walletTransactionRepository.existsByReferenceIdAndType(referenceId, WalletTransactionType.CREDIT)) {
            return;
        }

        var wallet = walletRepository.findByOrganizerExternalKey(organizerExternalKey)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for organizer: " + organizerExternalKey));

        var transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.CREDIT)
                .amount(amount)
                .currency(currency)
                .referenceId(referenceId)
                .status(WalletTransactionStatus.COMPLETED)
                .build();

        walletTransactionRepository.save(transaction);
    }
}
