package com.entri.checkout;

import com.entri.checkout.dto.CheckoutDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/checkout")
@Tag(
        name = "Checkout",
        description = "Operations for initiating and managing checkout"
)
public interface CheckoutApi {


    @Operation(
            operationId = "Checkout",
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
                    description = "Checkout created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CheckoutResponse.class)
                    )
            ),
    })
    @PostMapping("/{reservationId}")
    CheckoutResponse checkout(
            @Parameter(description = "The ID of the reservation to check out", required = true)
            @PathVariable
            final String reservationId,

            @RequestBody(
                    description = "The payment details for the reservation",
                    required = true
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            final CheckoutDetails checkoutDetails
    );

}
