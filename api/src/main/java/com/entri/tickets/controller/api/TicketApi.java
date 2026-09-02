package com.entri.tickets.controller.api;

import com.entri.tickets.dto.CheckInRequest;
import com.entri.tickets.dto.CheckInResponse;
import com.entri.tickets.dto.TicketResponse;
import com.entri.tickets.dto.VerifyCodeRequest;
import com.entri.tickets.dto.VerifyCodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Tickets", description = "Ticket retrieval and check-in operations")
public interface TicketApi {

    @Operation(
            operationId = "getTicketsByReservation",
            summary = "Get Tickets for a Reservation",
            description = "Returns all tickets issued for the given reservation"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/reservations/{reservationId}/tickets")
    List<TicketResponse> getTicketsByReservation(
            @Parameter(description = "Reservation external ID", required = true)
            @PathVariable String reservationId
    );

    @Operation(
            operationId = "getTicket",
            summary = "Get Individual Ticket",
            description = "Returns a single ticket by its unique code. Used by the frontend to render the QR code."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/tickets/{externalId}")
    TicketResponse getTicket(
            @Parameter(description = "Ticket external ID (UUID)", required = true)
            @PathVariable String externalId
    );

    @Operation(
            operationId = "verifyCheckInCode",
            summary = "Verify Check-in Code",
            description = "Validates a check-in code and returns the associated event. Used by the mobile app to confirm which event the code grants access to before opening the scanner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code is valid"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired check-in code",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/check-in/verify-code")
    VerifyCodeResponse verifyCheckInCode(
            @Valid @RequestBody VerifyCodeRequest request
    );

    @Operation(
            operationId = "checkInTicket",
            summary = "Check In a Ticket",
            description = "Marks a ticket as used. Requires a valid check-in code scoped to the ticket's event. Uses an atomic update to prevent double check-in under concurrent scanning."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in result returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired check-in code",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/tickets/{ticketCode}/check-in")
    CheckInResponse checkIn(
            @Parameter(description = "Unique ticket code from the QR", required = true)
            @PathVariable String ticketCode,

            @Valid @RequestBody CheckInRequest request
    );
}
