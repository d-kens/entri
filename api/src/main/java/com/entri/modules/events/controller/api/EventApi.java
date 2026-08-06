package com.entri.modules.events.controller.api;

import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.EventResponse;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/events")
@Tag(
        name = "Events",
        description = "Operations for creating, retrieving, and updating events"
)
public interface EventApi {

    @Operation(
            operationId = "getEventByExternalId",
            summary = "Get event details",
            description = "Retrieves the details of an event identified by its external identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified event was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{eventExternalId}")
    EventDetailResponse getEventByExternalId(
            @Parameter(
                    description = "The unique external identifier of the event",
                    required = true
            )
            @PathVariable
            final String eventExternalId
    );

    @Operation(
            operationId = "createEvent",
            summary = "Create event",
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
                    description = "Event created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventResponse.class)
                    )
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
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );

    @Operation(
            operationId = "updateEvent",
            summary = "Update event",
            description = "Updates an existing event identified by its external identifier using the provided details. On success, the API returns the updated event"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventResponse.class)
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
            )
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
            @org.springframework.web.bind.annotation.RequestBody
            final EventRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser user
    );
}