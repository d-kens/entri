package com.entri.payouts.service;

import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.intasend.IntaSendClient;
import com.entri.intasend.dto.BankCodeResponse;
import com.entri.payment.PaymentGateway;
import com.entri.payment.PayoutMethod;
import com.entri.payment.PayoutRequest;
import com.entri.payment.dto.WebhookRequest;
import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.payouts.entity.OrganizerPayoutAccount;
import com.entri.payouts.entity.Payout;
import com.entri.payouts.entity.PayoutStatus;
import com.entri.payouts.repository.OrganizerPayoutAccountRepository;
import com.entri.payouts.repository.PayoutRepository;
import com.entri.security.UserPrincipal;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final OrganizerPayoutAccountRepository payoutAccountRepository;
    private final UserService userService;
    private final PayoutRepository payoutRepository;
    private final PaymentGateway paymentGateway;
    private final IntaSendClient intaSendClient;

    @Value("${platform.payout.max-attempts:3}")
    private int maxAttempts;

    @Value("${platform.payout.batch-size:50}")
    private int batchSize;

    @Transactional(readOnly = true)
    public List<BankCodeResponse> getBankCodes() {
        return intaSendClient.getBankCodes();
    }

    @Transactional(readOnly = true)
    public List<PayoutAccountResponse> getAccounts(String organizerKey, UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerKey);
        return payoutAccountRepository.findByOrganizerExternalKey(organizerKey)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PayoutAccountResponse addAccount(String organizerKey, PayoutAccountRequest request,
            UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerKey);

        if (request.method() == PayoutMethod.BANK && !StringUtils.hasText(request.bankCode())) {
            throw new BadRequestException("Bank code is required for BANK payout method");
        }
        if (request.method() == PayoutMethod.MPESA_PAYBILL && !StringUtils.hasText(request.accountReference())) {
            throw new BadRequestException("Account reference is required for MPESA_PAYBILL payout method");
        }

        var organizer = userService.findEntityByExternalKey(organizerKey);
        var existing = payoutAccountRepository.findByOrganizerExternalKey(organizerKey);
        boolean isFirst = existing.isEmpty();
        boolean makeDefault = isFirst || request.isDefault();

        if (makeDefault && !isFirst) {
            clearDefault(organizerKey);
        }

        var account = OrganizerPayoutAccount.builder()
                .organizer(organizer)
                .method(request.method())
                .recipientName(request.recipientName())
                .account(request.account())
                .accountReference(request.accountReference())
                .bankCode(request.bankCode())
                .isDefault(makeDefault)
                .build();

        return toResponse(payoutAccountRepository.save(account));
    }

    @Transactional
    public PayoutAccountResponse setDefault(String organizerKey, String accountId,
            UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerKey);

        var account = payoutAccountRepository
                .findByExternalIdAndOrganizerExternalKey(accountId, organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout account with ID " + accountId + " not found"));

        clearDefault(organizerKey);
        account.setDefault(true);
        return toResponse(payoutAccountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(String organizerKey, String accountId, UserPrincipal requestingUser) {
        requestingUser.assertCanManage(organizerKey);

        var account = payoutAccountRepository
                .findByExternalIdAndOrganizerExternalKey(accountId, organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout account with ID " + accountId + " not found"));

        var others = payoutAccountRepository.findByOrganizerExternalKey(organizerKey)
                .stream().filter(a -> !a.getExternalId().equals(accountId)).toList();

        if (others.isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete your only payout account — add another before deleting this one.");
        }
        if (account.isDefault()) {
            throw new BadRequestException(
                    "Cannot delete the default account. Set another account as default first.");
        }

        payoutAccountRepository.delete(account);
    }

    public int processNextBatch() {
        List<Payout> pending = payoutRepository.findByStatusAndAttemptsLessThan(
                PayoutStatus.PENDING, maxAttempts, PageRequest.of(0, batchSize));

        for (Payout payout : pending) {
            try {
                dispatch(payout);
            } catch (Exception e) {
                log.error("Unexpected error dispatching payout {}", payout.getIdempotencyKey(), e);
            }
        }

        return pending.size();
    }

    private void dispatch(Payout payout) {
        String idempotencyKey = payout.getIdempotencyKey();

        payout.setStatus(PayoutStatus.IN_FLIGHT);
        payout.setAttempts(payout.getAttempts() + 1);
        payout.setLastAttemptedAt(Instant.now());
        payoutRepository.save(payout);

        try {
            var request = new PayoutRequest(
                    payout.getPayoutRecipientName(),
                    payout.getPayoutAccount(),
                    payout.getPayoutAccountReference(),
                    payout.getPayoutBankCode(),
                    payout.getAmount(),
                    payout.getCurrency(),
                    payout.getPayoutMethod(),
                    idempotencyKey
            );

            String trackingId = paymentGateway.sendPayout(request);
            payout.setTrackingId(trackingId);
            payoutRepository.save(payout);
            log.info("Payout {} submitted — tracking_id {}", idempotencyKey, trackingId);

        } catch (Exception e) {
            log.error("Payout {} failed to dispatch on attempt {} — staying IN_FLIGHT",
                    idempotencyKey, payout.getAttempts(), e);
        }
    }

    @Transactional
    public void handlePayoutWebhook(WebhookRequest webhookRequest) {
        var result = paymentGateway.parsePayoutWebhook(webhookRequest).orElse(null);
        if (result == null) return;

        var payout = payoutRepository.findByTrackingId(result.trackingId()).orElse(null);
        if (payout == null) {
            log.warn("Received payout webhook for unknown tracking_id {}", result.trackingId());
            return;
        }

        if (payout.getStatus() != PayoutStatus.IN_FLIGHT) {
            log.warn("Received payout webhook for payout {} already in status {} — ignoring",
                    payout.getIdempotencyKey(), payout.getStatus());
            return;
        }

        if (result.completed()) {
            payout.setStatus(PayoutStatus.COMPLETED);
            log.info("Payout {} COMPLETED", payout.getIdempotencyKey());
        } else {
            int attempts = payout.getAttempts();
            if (attempts >= maxAttempts) {
                payout.setStatus(PayoutStatus.FAILED);
                log.error("Payout {} permanently FAILED after {} attempts", payout.getIdempotencyKey(), attempts);
            } else {
                payout.setStatus(PayoutStatus.PENDING);
                log.warn("Payout {} failed on attempt {} — reset to PENDING for retry",
                        payout.getIdempotencyKey(), attempts);
            }
        }

        payoutRepository.save(payout);
    }

    private void clearDefault(String organizerKey) {
        payoutAccountRepository.findByOrganizerExternalKeyAndIsDefaultTrue(organizerKey)
                .ifPresent(existing -> {
                    existing.setDefault(false);
                    payoutAccountRepository.save(existing);
                });
    }

    private PayoutAccountResponse toResponse(OrganizerPayoutAccount a) {
        return new PayoutAccountResponse(
                a.getExternalId(), a.getMethod(), a.getRecipientName(),
                a.getAccount(), a.getAccountReference(), a.getBankCode(), a.isDefault());
    }
}
