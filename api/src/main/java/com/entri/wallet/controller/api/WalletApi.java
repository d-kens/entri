package com.entri.wallet.controller.api;

import com.entri.common.dto.PaginationResponse;
import com.entri.wallet.dto.BankCodeResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawRequest;
import com.entri.wallet.dto.WithdrawResponse;
import com.entri.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.entri.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping("/users")
public interface WalletApi {

    @Operation(
            operationId = "getUserWallet",
            summary = "Get organizer wallet",
            description = "Retrieves the wallet details for a user identified by their external key. Organizer only."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not an organizer",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or wallet not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{externalKey}/wallet")
    WalletResponse getWallet(
            @Parameter(description = "The unique external key of the user", required = true)
            @PathVariable final String externalKey,
            @Parameter(hidden = true) @AuthenticationPrincipal final UserPrincipal requestingUser
    );

    @Operation(
            operationId = "createUserWallet",
            summary = "Create a wallet for a user",
            description = "Provisions a new wallet for the specified user. Admin only."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Wallet created successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not an admin",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified user was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The user already has a wallet",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{externalKey}/wallet")
    @ResponseStatus(HttpStatus.CREATED)
    WalletResponse createWallet(
            @Parameter(description = "The unique external key of the user", required = true)
            @PathVariable final String externalKey
    );

    @Operation(
            operationId = "withdrawFromWallet",
            summary = "Withdraw from wallet",
            description = "Initiates a withdrawal from the organizer's wallet to an M-Pesa Paybill or bank account."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal initiated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or insufficient balance",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not authorized",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or wallet not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping("/{externalKey}/wallet/withdraw")
    WithdrawResponse withdraw(
            @Parameter(description = "The unique external key of the user", required = true)
            @PathVariable final String externalKey,
            @Valid @RequestBody final WithdrawRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal final UserPrincipal requestingUser
    );

    @Operation(
            operationId = "getWalletBankCodes",
            summary = "List supported bank codes",
            description = "Returns the list of banks and their codes supported for wallet withdrawals."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank codes retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/wallet/bank-codes")
    List<BankCodeResponse> getBankCodes();

    @Operation(
            operationId = "getWalletTransactions",
            summary = "Get wallet transactions",
            description = "Returns a paginated list of transactions for the organizer's wallet."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Caller is not authorized",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or wallet not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{externalKey}/wallet/transactions")
    PaginationResponse<WalletTransactionResponse> getTransactions(
            @Parameter(description = "The unique external key of the user", required = true)
            @PathVariable final String externalKey,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of results per page") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(hidden = true) @AuthenticationPrincipal final UserPrincipal requestingUser
    );
}
