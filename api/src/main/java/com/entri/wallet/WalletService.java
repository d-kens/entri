package com.entri.wallet;

import com.entri.common.PlatformProperties;
import com.entri.common.dto.PaginationResponse;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.payment.PaymentGateway;
import com.entri.payment.enums.PayoutStatus;
import com.entri.payment.dto.PayoutRequest;
import com.entri.security.UserPrincipal;
import com.entri.users.repository.UserRepository;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawalRequest;
import com.entri.wallet.dto.WithdrawalResponse;
import com.entri.wallet.entity.Wallet;
import com.entri.wallet.entity.WalletTransaction;
import com.entri.wallet.repository.WalletRepository;
import com.entri.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;
    private final PlatformTransactionManager transactionManager;
    private final PlatformProperties platformProperties;

    private static final String CURRENCY = "KES";

    @Transactional(readOnly = true)
    public WalletResponse getPlatformWallet() {
        var wallet = walletRepository.findByWalletType(WalletType.PLATFORM)
                .orElseThrow(() -> new ResourceNotFoundException("Platform wallet not found"));
        return new WalletResponse(wallet.getExternalId(), wallet.getBalance());
    }

    @Transactional
    public WalletResponse getWallet(String organizerExternalKey) {
        var wallet = walletRepository.findByOrganizerExternalKey(organizerExternalKey)
                .orElseGet(() -> createWallet(organizerExternalKey));

        return new WalletResponse(wallet.getExternalId(), wallet.getBalance());
    }

    private Wallet createWallet(String organizerExternalKey) {
        var organizer = userRepository.findByExternalKeyAndDeletedFalse(organizerExternalKey)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found: " + organizerExternalKey));
        return walletRepository.save(Wallet.builder().organizer(organizer).build());
    }

    @Transactional(readOnly = true)
    public PaginationResponse<WalletTransactionResponse> getTransactions(String organizerExternalKey, Pageable pageable) {
        var page = walletTransactionRepository.findByWalletOrganizerExternalKey(organizerExternalKey, pageable)
                .map(this::toResponse);

        return new PaginationResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public WithdrawalResponse withdraw(String walletId, WithdrawalRequest request, UserPrincipal requestingUser) {
        var template = new TransactionTemplate(transactionManager);

        // Phase 1: debit balance and record a PENDING debit — commits before HTTP call
        WalletTransaction transaction = template.execute(s -> {
            var wallet = walletRepository.findByExternalId(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

            if (wallet.getWalletType() == WalletType.PLATFORM) {
                if (!requestingUser.isAdmin()) {
                    throw new ForbiddenException("Only admins can withdraw from the platform wallet");
                }
            } else if (!requestingUser.getExternalKey().equals(wallet.getOrganizer().getExternalKey())) {
                throw new ForbiddenException("You are not authorized to withdraw from this wallet");
            }

            if (walletRepository.decrementBalance(wallet.getId(), request.amount()) == 0) {
                throw new BadRequestException("Insufficient wallet balance");
            }

            return walletTransactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet)
                    .type(WalletTransactionType.DEBIT)
                    .amount(request.amount())
                    .currency(CURRENCY)
                    .referenceId(UUID.randomUUID().toString())
                    .status(WalletTransactionStatus.PENDING)
                    .build());
        });

        // Phase 2: call IntaSend — outside any transaction, no DB connection held
        String trackingId;
        try {
            trackingId = paymentGateway.payout(new PayoutRequest(
                    request.name(),
                    request.account(),
                    request.accountType(),
                    request.accountReference(),
                    request.bankCode(),
                    request.amount(),
                    CURRENCY,
                    request.narrative(),
                    transaction.getExternalId()
            ));
        } catch (RuntimeException e) {
            // Phase 3a: payout call failed — mark FAILED and refund balance
            template.execute(s -> {
                transaction.setStatus(WalletTransactionStatus.FAILED);
                walletTransactionRepository.save(transaction);
                walletRepository.incrementBalance(transaction.getWallet().getId(), transaction.getAmount());
                return null;
            });
            throw e;
        }

        // Phase 3b: attach tracking id so the webhook can reconcile
        template.execute(s -> {
            transaction.setTrackingReference(trackingId);
            walletTransactionRepository.save(transaction);
            return null;
        });

        return new WithdrawalResponse(transaction.getExternalId(), transaction.getStatus());
    }

    @Transactional
    public void applyPayoutResult(String trackingId, PayoutStatus payoutStatus) {
        var transaction = walletTransactionRepository.findByTrackingReference(trackingId).orElse(null);
        if (transaction == null) {
            log.warn("Payout webhook received for unknown tracking id={}. Ignoring.", trackingId);
            return;
        }
        if (transaction.getStatus() != WalletTransactionStatus.PENDING) {
            return;
        }
        if (payoutStatus == PayoutStatus.COMPLETED) {
            transaction.setStatus(WalletTransactionStatus.COMPLETED);
        } else {
            transaction.setStatus(WalletTransactionStatus.FAILED);
            walletRepository.incrementBalance(transaction.getWallet().getId(), transaction.getAmount());
        }
        walletTransactionRepository.save(transaction);
    }

    public void creditReservation(String organizerExternalKey, BigDecimal totalAmount, String currency, String referenceId) {
        BigDecimal platformFee = totalAmount
                .multiply(platformProperties.serviceFeeRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal organizerAmount = totalAmount.subtract(platformFee);

        credit(organizerExternalKey, organizerAmount, currency, referenceId);
        creditPlatform(platformFee, currency, referenceId);
    }

    public void credit(String organizerExternalKey, BigDecimal amount, String currency, String referenceId) {
        try {
            new TransactionTemplate(transactionManager).execute(s -> {
                var wallet = walletRepository.findByOrganizerExternalKey(organizerExternalKey)
                        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for organizer: " + organizerExternalKey));
                creditWallet(wallet, amount, currency, referenceId);
                return null;
            });
        } catch (DataIntegrityViolationException ignored) {
            // duplicate delivery — already credited
        }
    }

    public void creditPlatform(BigDecimal amount, String currency, String referenceId) {
        try {
            new TransactionTemplate(transactionManager).execute(s -> {
                var wallet = walletRepository.findByWalletType(WalletType.PLATFORM)
                        .orElseThrow(() -> new IllegalStateException("Platform wallet not found"));
                creditWallet(wallet, amount, currency, referenceId);
                return null;
            });
        } catch (DataIntegrityViolationException ignored) {
            // duplicate delivery — already credited
        }
    }

    private void creditWallet(Wallet wallet, BigDecimal amount, String currency, String referenceId) {
        walletTransactionRepository.save(WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.CREDIT)
                .amount(amount)
                .currency(currency)
                .referenceId(referenceId)
                .status(WalletTransactionStatus.COMPLETED)
                .build());
        walletRepository.incrementBalance(wallet.getId(), amount);
    }

    private WalletTransactionResponse toResponse(WalletTransaction t) {
        return new WalletTransactionResponse(
                t.getExternalId(),
                t.getType(),
                t.getAmount(),
                t.getCurrency(),
                t.getReferenceId(),
                t.getStatus(),
                t.getDateCreated()
        );
    }
}
