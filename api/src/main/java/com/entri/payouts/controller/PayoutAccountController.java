package com.entri.payouts.controller;

import com.entri.payouts.controller.api.PayoutAccountApi;
import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.payouts.service.PayoutAccountService;
import com.entri.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayoutAccountController implements PayoutAccountApi {

    private final PayoutAccountService payoutAccountService;

    @Override
    public List<PayoutAccountResponse> getAccounts(final String organizerKey, final UserPrincipal requestingUser) {
        return payoutAccountService.getAccounts(organizerKey);
    }

    @Override
    public PayoutAccountResponse addAccount(final String organizerKey, final PayoutAccountRequest request, final UserPrincipal requestingUser) {
        return payoutAccountService.addAccount(organizerKey, request, requestingUser);
    }

    @Override
    public PayoutAccountResponse setDefault(final String organizerKey, final String accountId, final UserPrincipal requestingUser) {
        return payoutAccountService.setDefault(organizerKey, accountId, requestingUser);
    }

    @Override
    public void deleteAccount(final String organizerKey, final String accountId, final UserPrincipal requestingUser) {
        payoutAccountService.deleteAccount(organizerKey, accountId, requestingUser);
    }
}
