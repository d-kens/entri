package com.entri.payouts.controller.api;

import com.entri.payouts.dto.PayoutAccountRequest;
import com.entri.payouts.dto.PayoutAccountResponse;
import com.entri.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping("/users/{organizerKey}/payout-accounts")
@SecurityRequirement(name = "bearerAuth")
public interface PayoutAccountApi {

    @Operation(
            operationId = "listPayoutAccounts",
            summary = "List payout accounts",
            description = "Returns all payout accounts configured for the organizer."
    )
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping
    List<PayoutAccountResponse> getAccounts(
            @Parameter(description = "The organizer's external key", required = true)
            @PathVariable String organizerKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "addPayoutAccount",
            summary = "Add a payout account",
            description = "Adds a new payout account for the organizer."
    )
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PayoutAccountResponse addAccount(
            @Parameter(description = "The organizer's external key", required = true)
            @PathVariable String organizerKey,
            @Valid @RequestBody PayoutAccountRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "setDefaultPayoutAccount",
            summary = "Set default payout account",
            description = "Marks the specified payout account as the organizer's default."
    )
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PutMapping("/{accountId}/default")
    PayoutAccountResponse setDefault(
            @Parameter(description = "The organizer's external key", required = true)
            @PathVariable String organizerKey,
            @Parameter(description = "The payout account's external ID", required = true)
            @PathVariable String accountId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "deletePayoutAccount",
            summary = "Delete a payout account",
            description = "Deletes the specified payout account."
    )
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(
            @Parameter(description = "The organizer's external key", required = true)
            @PathVariable String organizerKey,
            @Parameter(description = "The payout account's external ID", required = true)
            @PathVariable String accountId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );
}
