package com.entri.modules.events.controller.api;

import com.entri.common.dto.PaginationResponse;
import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequestMapping("/events")
@Tag(
        name = "Events",
        description = "Operations for creating, retrieving, and updating events"
)
public interface EventApi {

    @Operation(
            operationId = "listEvents",
            summary = "List Published Events",
            description = "Returns a paginated list of published events. Results can be filtered using the supported query parameters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request contains invalid query parameters",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Published events retrieved successfully"
            )
    })
    @GetMapping
    PaginationResponse<EventResponse> listEvents(
           @ParameterObject @Valid final EventFilter filter
    );


    @Operation(
            operationId = "listOrganizerEvents",
            summary = "List Organizer Events",
            description = "Retrieves a paginated list of events that the authenticated user is authorized to manage. Platform administrators can retrieve all events."
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
                    description = "The authenticated user is not authorized to view organizer events",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request contains invalid query parameters",
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
    @GetMapping("/manage")
    PaginationResponse<EventResponse> listOrganizerEvents(
            @ParameterObject @Valid final EventFilter filter,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user
    );

    @Operation(
            operationId = "getEventByExternalId",
            summary = "Get Event Details",
            description = "Retrieves the details of an event identified by its external identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Event retrieved successfully"
            ),
    })
    @GetMapping("/{eventExternalId}")
    EventResponse getEventByExternalId(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable
            final String eventExternalId
    );

    @Operation(
            operationId = "publishEvent",
            summary = "Publish Event",
            description = "Publishes the specified event, making it available to attendees."
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
                    description = "The authenticated user is not authorized to update events",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The event cannot be published because it has no ticket types, its status is not DRAFT or CANCELLED, or its start time is in the past",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Event published successfully"
            )
    })
    @PatchMapping("/{eventExternalId}/publish")
    EventResponse publishEvent(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );

    @Operation(
            operationId = "cancelEvent",
            summary = "Cancel Event",
            description = "Cancels the specified event."
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
                    description = "The authenticated user is not authorized to cancel this event",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The event cannot be cancelled because its status is not PUBLISHED",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Event cancelled successfully"
            )
    })
    @PatchMapping("/{eventExternalId}/cancel")
    EventResponse cancelEvent(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );

    @Operation(
            operationId = "getEventTicketTypes",
            summary = "Get Ticket Types for an Event",
            description = "Retrieves the ticket types of an event identified by its external identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Event ticket types retrieved successfully"
            ),
    })
    @GetMapping("/{eventExternalId}/ticket-types")
    List<TicketTypeResponse> getEventTicketTypes(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable
            final String eventExternalId
    );

    @Operation(
            operationId = "createEvent",
            summary = "Create Event",
            description = "Creates a new event using the provided details. On success, the API returns the newly created event"
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
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified category was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully"
            )
    })
    @PostMapping
    ResponseEntity<EventResponse> createEvent(
            @Parameter(hidden = true)
            UriComponentsBuilder uriComponentsBuilder,

            @RequestBody(
                    description = "The event details",
                    required = true
            )
            @Valid
            // FQN required — collides with io.swagger.v3.oas.annotations.parameters.RequestBody
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );

    @Operation(
            operationId = "updateEvent",
            summary = "Update Event",
            description = "Updates an existing event identified by its external identifier using the provided details. On success, the API returns the updated event"
    )
    @SecurityRequirement(name = "bearerAuth")
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
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user is not authorized to update events",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event or category was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully"
            ),
    })
    @PutMapping("/{externalId}")
    EventResponse updateEvent(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable
            final String externalId,

            @RequestBody(
                    description = "The event details",
                    required = true
            )
            @Valid
            // FQN required — collides with io.swagger.v3.oas.annotations.parameters.RequestBody
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );

    @Operation(
            operationId = "createEventTicketType",
            summary = "Create Event Ticket Type",
            description = "Creates a new ticket type for the specified event using the provided details. On success, the API returns the newly created ticket type"
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
                    description = "The authenticated user is not authorized to create event ticket types",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
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
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket type created successfully"
            ),
    })
    @PostMapping("/{eventExternalId}/ticket-types")
    ResponseEntity<TicketTypeResponse> createEventTicketType(
            @Parameter(hidden = true)
            UriComponentsBuilder uriComponentsBuilder,

            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @RequestBody(
                    description = "The ticket type details",
                    required = true
            )
            @Valid
            // FQN required — collides with io.swagger.v3.oas.annotations.parameters.RequestBody
            @org.springframework.web.bind.annotation.RequestBody
            final TicketTypeRequest ticketTypeRequest,

            @Parameter(hidden = true)
            @AuthenticationPrincipal final AuthenticatedUser user
    );


    @Operation(
            operationId = "reserveEventTickets",
            summary = "Reserve Event Tickets",
            description = "Temporarily reserves the requested tickets for the specified event and ticket types. The reservation holds the requested ticket quantities for a limited period, subject to ticket availability. On success, the API returns the created ticket reservation details."
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
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                        The booking request conflicts with the current state of the event or ticket.
                        This may occur when the event is not currently on sale, the ticket type is not
                        available, there are insufficient tickets available, or the maximum number of
                        tickets allowed per order has been exceeded.
                    """,
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket reservation created successfully"
            ),
    })
    @PostMapping("/{eventExternalId}/reservations")
    ResponseEntity<EventTicketReservationResponse> reserveEventTickets(
            @Parameter(hidden = true)
            UriComponentsBuilder uriComponentsBuilder,

            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @RequestBody(
                    description = "The ticket reservation request",
                    required = true
            )
            @Valid
            // FQN required — collides with io.swagger.v3.oas.annotations.parameters.RequestBody
            @org.springframework.web.bind.annotation.RequestBody
            final EventTicketReservationRequest eventTicketReservationRequest
    );


    @Operation(
            operationId = "getEventTicketsReservation",
            summary = "Retrieve Ticket Reservation for an Event",
            description = "Retrieves the ticket reservation for an event, including the reserved ticket quantities and reservation details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified reservation was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation for an event retrieved successfully"
            ),
    })
    @GetMapping("/{eventExternalId}/reservations/{reservationId}")
    EventTicketReservationDetailDto getEventTicketReservation(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @Parameter(
                    description = "The unique external identifier of the reservation",
                    required = true
            )
            @PathVariable final String reservationId
    );
}
