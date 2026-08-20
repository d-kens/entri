package com.entri.payment.controller.api;


import com.entri.payment.dto.CheckoutRequest;
import com.entri.payment.dto.CheckoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/payments")
@Tag(
        name = "Payments",
        description = "Operations for making and retrieving payments"
)
public interface PaymentApi {

    @Operation(
            operationId = "createPayment",
            summary = "Create a payment",
            description = "Initiates a payment for a ticket reservation using the provided payment details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reservation is not available for payment",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment checkout created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CheckoutResponse.class)
                    )
            ),
    })
    @PostMapping("/checkout")
    CheckoutResponse checkout(
            @RequestBody(
                    description = "The ticket reservation request",
                    required = true
            )
            @Valid
            // FQN required — collides with io.swagger.v3.oas.annotations.parameters.RequestBody
            @org.springframework.web.bind.annotation.RequestBody
            final CheckoutRequest checkoutRequest
    );
}