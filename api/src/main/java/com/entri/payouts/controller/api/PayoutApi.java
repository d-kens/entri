package com.entri.payouts.controller.api;

import com.entri.intasend.dto.BankCodeResponse;
import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Payouts", description = "Payout account management and disbursement webhook")
public interface PayoutApi {

    @Operation(operationId = "getBankCodes", summary = "Get available bank codes")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{organizerKey}/payout-accounts/bank-codes")
    List<BankCodeResponse> getBankCodes(
            @Parameter(required = true) @PathVariable String organizerKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(operationId = "listPayoutAccounts", summary = "List payout accounts")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{organizerKey}/payout-accounts")
    List<PayoutAccountResponse> getAccounts(
            @Parameter(required = true) @PathVariable String organizerKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(operationId = "addPayoutAccount", summary = "Add a payout account")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/users/{organizerKey}/payout-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    PayoutAccountResponse addAccount(
            @Parameter(required = true) @PathVariable String organizerKey,
            @Valid @RequestBody PayoutAccountRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(operationId = "setDefaultPayoutAccount", summary = "Set default payout account")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/users/{organizerKey}/payout-accounts/{accountId}/default")
    PayoutAccountResponse setDefault(
            @Parameter(required = true) @PathVariable String organizerKey,
            @Parameter(required = true) @PathVariable String accountId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(operationId = "deletePayoutAccount", summary = "Delete a payout account")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/users/{organizerKey}/payout-accounts/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(
            @Parameter(required = true) @PathVariable String organizerKey,
            @Parameter(required = true) @PathVariable String accountId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(operationId = "payoutWebhook", summary = "IntaSend send-money webhook",
            description = "Receives disbursement status notifications from IntaSend.")
    @PostMapping("/payouts/webhook")
    @ResponseStatus(HttpStatus.OK)
    void handlePayoutWebhook(
            @org.springframework.web.bind.annotation.RequestHeader java.util.Map<String, String> headers,
            @RequestBody String payload
    );
}
