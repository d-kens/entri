package com.entri.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.security.UserPrincipal;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawalRequest;
import com.entri.wallet.dto.WithdrawalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/wallet")
@Tag(name = "Wallet", description = "Operations for managing organizer wallets")
public interface WalletApi {

    @Operation(
            operationId = "GetWallet",
            summary = "Get organizer wallet",
            description = "Retrieves the wallet and current balance for a given organizer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WalletResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Wallet not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{organizerExternalKey}")
    WalletResponse getWallet(
            @Parameter(description = "The external key of the organizer", required = true)
            @PathVariable String organizerExternalKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "GetWalletTransactions",
            summary = "Get wallet transactions",
            description = "Returns a paginated list of transactions for the given organizer's wallet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Wallet not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{organizerExternalKey}/transactions")
    PaginationResponse<WalletTransactionResponse> getTransactions(
            @Parameter(description = "The external key of the organizer", required = true)
            @PathVariable String organizerExternalKey,
            @PageableDefault(size = 20, sort = "dateCreated", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "Withdraw",
            summary = "Request a withdrawal",
            description = "Initiates a withdrawal from the organizer's wallet to a Paybill, Till number, or bank account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal initiated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Insufficient balance or invalid request",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Wallet not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    @PostMapping("/{organizerExternalKey}/withdraw")
    WithdrawalResponse withdraw(
            @Parameter(description = "The external key of the organizer", required = true)
            @PathVariable String organizerExternalKey,
            @Valid @RequestBody WithdrawalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );
}
