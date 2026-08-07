package com.entri.modules.events.controller.api;

import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/ticket-types")
public interface TicketTypeApi {
    @Operation(
            operationId = "getTicketType",
            summary = "Get Ticket Type by ID",
            description = "Retrieves the details of a specific ticket type identified by its unique identifier. On success, the API returns the ticket type details"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
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
                    description = "The authenticated user is not authorized to view ticket type",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified ticket type was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Organizer events retrieved successfully"
            )
    })
    @GetMapping("/{ticketTypeId}")
    TicketTypeResponse getTicketType(
            @Parameter(
                    description = "The unique identifier for the ticket type",
                    required = true
            )
            @PathVariable final Long ticketTypeId
    );

    @Operation(
            operationId = "updateTicketType",
            summary = "Update Ticket Type",
            description = "Update an existing ticket type identified by its ID using the provided details. On success, the API returns the updated event"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
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
                    description = "The authenticated user is not authorized to view ticket type",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified ticket type was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Organizer events retrieved successfully"
            )
    })
    @PutMapping("/{ticketTypeId}")
    TicketTypeResponse updateTicketType(
            @Parameter(
                    description = "The unique identifier for the ticket type",
                    required = true
            )
            @PathVariable final Long ticketTypeId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest,
            @AuthenticationPrincipal final AuthenticatedUser user
    );

    @Operation(
            operationId = "deleteTicketType",
            summary = "Delete Ticket Type",
            description = "Deletes the ticket type identified by the given ID. On success, the API returns no content"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
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
                    description = "The authenticated user is not authorized to delete this ticket type",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified ticket type was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Ticket type deleted successfully"
            )
    })
    @DeleteMapping("/{ticketTypeId}")
    ResponseEntity<Void> deleteTicketType(
            @Parameter(
                    description = "The unique identifier for the ticket type",
                    required = true
            )
            @PathVariable final long ticketTypeId,
            @AuthenticationPrincipal final AuthenticatedUser user
    );
}

