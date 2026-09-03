package com.entri.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.payment.PaymentGateway;
import com.entri.payment.PayoutStatus;
import com.entri.payment.dto.PayoutRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void createWallet(String organizerExternalKey) {
        var organizer = userRepository.findByExternalKeyAndDeletedFalse(organizerExternalKey)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + organizerExternalKey));

        var wallet = Wallet.builder()
                .organizer(organizer)
                .build();

        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(String organizerExternalKey) {
        var wallet = walletRepository.findByOrganizerExternalKey(organizerExternalKey)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for organizer: " + organizerExternalKey));

        return new WalletResponse(wallet.getExternalId(), wallet.getBalance());
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

    @Transactional
    public WithdrawalResponse withdraw(String organizerExternalKey, WithdrawalRequest request) {
        var wallet = walletRepository.findByOrganizerExternalKey(organizerExternalKey)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for organizer: " + organizerExternalKey));

        int updated = walletRepository.decrementBalance(wallet.getId(), request.amount());
        if (updated == 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        var transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.DEBIT)
                .amount(request.amount())
                .currency("KES")
                .referenceId(UUID.randomUUID().toString())
                .status(WalletTransactionStatus.PENDING)
                .build();

        walletTransactionRepository.save(transaction);

        var payoutRequest = new PayoutRequest(
                request.name(),
                request.account(),
                request.accountType(),
                request.accountReference(),
                request.bankCode(),
                request.amount(),
                "KES",
                request.narrative(),
                transaction.getExternalId()
        );

        var trackingId = paymentGateway.payout(payoutRequest);
        transaction.setTrackingReference(trackingId);
        walletTransactionRepository.save(transaction);

        return new WithdrawalResponse(transaction.getExternalId(), transaction.getStatus());
    }

    @Transactional
    public void applyPayoutResult(String trackingId, PayoutStatus payoutStatus) {
        walletTransactionRepository.findByTrackingReference(trackingId).ifPresent(transaction -> {
            if (payoutStatus == PayoutStatus.COMPLETED) {
                transaction.setStatus(WalletTransactionStatus.COMPLETED);
            } else {
                transaction.setStatus(WalletTransactionStatus.FAILED);
                walletRepository.incrementBalance(transaction.getWallet().getId(), transaction.getAmount());
            }
            walletTransactionRepository.save(transaction);
        });
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
        walletRepository.incrementBalance(wallet.getId(), amount);
    }
}
