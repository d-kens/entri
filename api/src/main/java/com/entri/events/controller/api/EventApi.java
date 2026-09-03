package com.entri.events.controller.api;

import com.entri.common.dto.PaginationResponse;
import com.entri.events.dto.EventCheckInStatsResponse;
import com.entri.events.dto.EventReservationSummaryResponse;
import com.entri.security.UserPrincipal;
import com.entri.events.dto.EventFilter;
import com.entri.events.dto.EventRequest;
import com.entri.events.dto.EventResponse;
import com.entri.events.dto.EventTicketReservationDetailDto;
import com.entri.events.dto.EventTicketReservationRequest;
import com.entri.events.dto.EventTicketReservationResponse;
import com.entri.events.dto.TicketTypeRequest;
import com.entri.events.dto.TicketTypeResponse;
import com.entri.tickets.dto.CheckInCodeResponse;
import com.entri.tickets.dto.TicketFilter;
import com.entri.tickets.dto.TicketResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/manage")
    PaginationResponse<EventResponse> listOrganizerEvents(
            @ParameterObject @Valid final EventFilter filter,
            @Parameter(hidden = true) @AuthenticationPrincipal final UserPrincipal requestingUser
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PatchMapping("/{eventExternalId}/publish")
    EventResponse publishEvent(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            final UserPrincipal requestingUser
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PatchMapping("/{eventExternalId}/cancel")
    EventResponse cancelEvent(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable final String eventExternalId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            final UserPrincipal requestingUser
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping
    ResponseEntity<EventResponse> createEvent(
            @Parameter(hidden = true)
            UriComponentsBuilder uriComponentsBuilder,

            @RequestBody(
                    description = "The event details",
                    required = true
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            final UserPrincipal requestingUser
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
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
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            final UserPrincipal requestingUser
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
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
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
            @org.springframework.web.bind.annotation.RequestBody
            final TicketTypeRequest ticketTypeRequest,

            @Parameter(hidden = true)
            @AuthenticationPrincipal final UserPrincipal requestingUser
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
            @org.springframework.web.bind.annotation.RequestBody
            final EventTicketReservationRequest eventTicketReservationRequest
    );


    @Operation(
            operationId = "generateCheckInCode",
            summary = "Generate Check-in Code",
            description = "Generates a short-lived code for staff to authenticate on the mobile scanner. The code expires when the event ends."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in code generated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Event has already ended",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping("/{eventExternalId}/check-in-code")
    CheckInCodeResponse generateCheckInCode(
            @Parameter(description = "The unique external identifier of the event", required = true)
            @PathVariable String eventExternalId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal requestingUser
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

    @Operation(
            operationId = "listEventReservations",
            summary = "List Event Reservations",
            description = "Returns a paginated list of ticket reservations for the specified event. Only accessible by the event organizer or an admin."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user does not own this event",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{eventExternalId}/reservations")
    PaginationResponse<EventReservationSummaryResponse> listEventReservations(
            @Parameter(description = "The unique external identifier of the event", required = true)
            @PathVariable String eventExternalId,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of results per page") @RequestParam(defaultValue = "20") int size,

            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(summary = "Get tickets for an event", description = "Returns a paginated list of tickets for the given event. Organizers can only access their own events.")
    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{eventExternalId}/tickets")
    PaginationResponse<TicketResponse> getEventTickets(
            @Parameter(description = "The unique external identifier of the event", required = true)
            @PathVariable String eventExternalId,

            @Valid @ModelAttribute TicketFilter filter,

            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "getEventCheckInStats",
            summary = "Get Check-in Statistics",
            description = "Returns real-time check-in statistics for the specified event, including total tickets, checked-in count, check-in rate, and the 10 most recent check-ins. Only accessible by the event organizer or an admin."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in stats retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user does not own this event",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @GetMapping("/{eventExternalId}/check-in-stats")
    EventCheckInStatsResponse getCheckInStats(
            @Parameter(description = "The unique external identifier of the event", required = true)
            @PathVariable String eventExternalId,

            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );
}
