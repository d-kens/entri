package com.parrcel.api.modules.wallet.service;

import com.parrcel.api.modules.deliveries.entity.Delivery;
import com.parrcel.api.modules.deliveries.service.DeliveryService;
import com.parrcel.api.modules.users.entity.User;
import com.parrcel.api.modules.users.service.UserService;
import com.parrcel.api.modules.wallet.dto.WithdrawRequest;
import com.parrcel.api.modules.wallet.dto.WithdrawResponse;
import com.parrcel.api.modules.wallet.entity.TransactionStatus;
import com.parrcel.api.modules.wallet.entity.TransactionType;
import com.parrcel.api.modules.wallet.entity.Wallet;
import com.parrcel.api.modules.wallet.entity.WalletTransaction;
import com.parrcel.api.modules.wallet.exception.InsufficientBalanceException;
import com.parrcel.api.modules.wallet.repository.WalletRepository;
import com.parrcel.api.modules.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserService userService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final DeliveryService deliveryService;

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

    @Transactional
    public WithdrawResponse withDraw(WithdrawRequest request, Long userId) {
        var wallet = getOrCreateWallet(userId);

        if (request.amount().compareTo(wallet.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        return new WithdrawResponse("payment-id");
    }

    /**
     * Credit wallet - add money
     */
    @Transactional
    public WalletTransaction creditWallet(
            Wallet wallet,
            BigDecimal amount,
            String referenceId,
            String description
    ) {
        log.info("Crediting wallet {} with amount {}", wallet.getExternalId(), amount);

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.credit(amount);
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .balanceAfter(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .description(description)
                .build();

        transaction.complete();

        transaction = walletTransactionRepository.save(transaction);
        log.info("Wallet credited successfully. New balance: {}", wallet.getBalance());
        return transaction;
    }

    @Transactional
    public void processCodCollection(String deliveryExternalId, BigDecimal amount, String paymentId) {
        Delivery delivery = deliveryService.getDeliveryByExternalId(deliveryExternalId);

        if (delivery.isCashCollected()) {
            log.info("Cash already collected for delivery: {} - Skipping duplicate",
                    delivery.getExternalId());
            return;
        }

        if (!delivery.isCollectCash()) {
            log.error("Payment {} references delivery {} without COD enabled",
                    paymentId, delivery.getExternalId());
            return;
        }

        Wallet merchantWallet = getOrCreateWallet(delivery.getUser().getId());

        creditWallet(
                merchantWallet,
                amount,
                deliveryExternalId,
                "COD collection for delivery " + delivery.getTrackingNumber()
        );

        deliveryService.markCashCollected(delivery.getExternalId());

        log.info("Merchant wallet credited for COD payment: {} - Amount: {}",
                paymentId, amount);
    }
}