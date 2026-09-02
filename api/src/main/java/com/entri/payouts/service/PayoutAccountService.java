package com.entri.payouts.service;

import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.payouts.entity.OrganizerPayoutAccount;
import com.entri.payouts.entity.PayoutMethod;
import com.entri.payouts.repository.OrganizerPayoutAccountRepository;
import com.entri.security.UserPrincipal;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayoutAccountService {

    private final OrganizerPayoutAccountRepository payoutAccountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<PayoutAccountResponse> getAccounts(final String organizerKey) {
        return payoutAccountRepository.findByOrganizerExternalKey(organizerKey)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PayoutAccountResponse addAccount(
            final String organizerKey,
            final PayoutAccountRequest request,
            final UserPrincipal requestingUser
    ) {
        requestingUser.assertCanManage(organizerKey);

        var organizer = userService.findEntityByExternalKey(organizerKey);

        if (request.method() == PayoutMethod.BANK && !StringUtils.hasText(request.bankCode())) {
            throw new BadRequestException("Bank code is required for BANK payout method");
        }
        if (request.method() == PayoutMethod.MPESA_PAYBILL && !StringUtils.hasText(request.accountReference())) {
            throw new BadRequestException("Account reference is required for MPESA_PAYBILL payout method");
        }

        var existingAccounts = payoutAccountRepository.findByOrganizerExternalKey(organizerKey);
        boolean isFirstAccount = existingAccounts.isEmpty();
        boolean makeDefault = isFirstAccount || request.isDefault();

        if (makeDefault && !isFirstAccount) {
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

    public PayoutAccountResponse setDefault(
            final String organizerKey,
            final String accountExternalId,
            final UserPrincipal requestingUser
    ) {
        requestingUser.assertCanManage(organizerKey);

        var account = payoutAccountRepository
                .findByExternalIdAndOrganizerExternalKey(accountExternalId, organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout account with ID " + accountExternalId + " not found"));

        clearDefault(organizerKey);
        account.setDefault(true);

        return toResponse(payoutAccountRepository.save(account));
    }

    public void deleteAccount(
            final String organizerKey,
            final String accountExternalId,
            final UserPrincipal requestingUser
    ) {
        requestingUser.assertCanManage(organizerKey);

        var account = payoutAccountRepository
                .findByExternalIdAndOrganizerExternalKey(accountExternalId, organizerKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout account with ID " + accountExternalId + " not found"));

        if (account.isDefault()) {
            var otherAccounts = payoutAccountRepository.findByOrganizerExternalKey(organizerKey)
                    .stream()
                    .filter(a -> !a.getExternalId().equals(accountExternalId))
                    .toList();
            if (!otherAccounts.isEmpty()) {
                throw new BadRequestException(
                        "Cannot delete the default account. Set another account as default first.");
            }
        }

        payoutAccountRepository.delete(account);
    }

    private void clearDefault(final String organizerKey) {
        payoutAccountRepository.findByOrganizerExternalKeyAndIsDefaultTrue(organizerKey)
                .ifPresent(existing -> {
                    existing.setDefault(false);
                    payoutAccountRepository.save(existing);
                });
    }

    private PayoutAccountResponse toResponse(final OrganizerPayoutAccount a) {
        return new PayoutAccountResponse(
                a.getExternalId(),
                a.getMethod(),
                a.getRecipientName(),
                a.getAccount(),
                a.getAccountReference(),
                a.getBankCode(),
                a.isDefault()
        );
    }
}
