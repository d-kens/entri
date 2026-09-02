package com.entri.payouts.controller;

import com.entri.intasend.dto.BankCodeResponse;
import com.entri.intasend.dto.IntaSendSendMoneyWebhookPayload;
import com.entri.payouts.controller.api.PayoutApi;
import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.payouts.service.PayoutService;
import com.entri.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayoutController implements PayoutApi {

    private final PayoutService payoutService;

    @Override
    public List<BankCodeResponse> getBankCodes(String organizerKey, UserPrincipal requestingUser) {
        return payoutService.getBankCodes();
    }

    @Override
    public List<PayoutAccountResponse> getAccounts(String organizerKey, UserPrincipal requestingUser) {
        return payoutService.getAccounts(organizerKey, requestingUser);
    }

    @Override
    public PayoutAccountResponse addAccount(String organizerKey, PayoutAccountRequest request,
            UserPrincipal requestingUser) {
        return payoutService.addAccount(organizerKey, request, requestingUser);
    }

    @Override
    public PayoutAccountResponse setDefault(String organizerKey, String accountId,
            UserPrincipal requestingUser) {
        return payoutService.setDefault(organizerKey, accountId, requestingUser);
    }

    @Override
    public void deleteAccount(String organizerKey, String accountId, UserPrincipal requestingUser) {
        payoutService.deleteAccount(organizerKey, accountId, requestingUser);
    }

    @Override
    public void handleSendMoneyWebhook(IntaSendSendMoneyWebhookPayload payload) {
        payoutService.handleSendMoneyWebhook(payload);
    }
}
