package com.entri.modules.events.controller.api;


import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/events")
@Tag(name = "Events", description = "Event management and discovery")
public interface EventApi {

    @Operation(summary = "Get event details")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{eventExternalId}")
    EventDetailResponse getEventByExternalId(
            @Parameter(description = "External ID of the event") @PathVariable String eventExternalId
    );


    @Operation(summary = "Create event")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Event created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    ResponseEntity<EventResponse> createEvent(
            @Parameter(hidden = true) UriComponentsBuilder uriComponentsBuilder,
            @RequestBody @Valid EventRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    );
}
