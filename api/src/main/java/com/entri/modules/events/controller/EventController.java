package com.entri.modules.events.controller;

import com.entri.common.dto.AuthenticatedUser;
import com.entri.common.dto.PaginationResponse;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.dto.EventFilter;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.service.EventService;
import com.entri.modules.events.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;

    @GetMapping
    public PaginationResponse<EventResponse> browseEvents(
            @Valid final EventFilter filter
    ) {
        return eventService.getEvents(filter);
    }

    @GetMapping("/manage")
    public PaginationResponse<EventResponse> manageEvents(
            @Valid final EventFilter filter,
            @AuthenticationPrincipal String currentUserKey,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
        return eventService.getEventsForManagement(filter, isAdmin ? null : currentUserKey);
    }

    @GetMapping("/{externalId}")
    public EventDetailResponse getEventByExternalId(
            @PathVariable final String externalId
    ) {
        return eventService.getEventByExternalId(externalId);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            Authentication authentication,
            @Valid @RequestBody final EventRequest request
    ) {
        final AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        var response = eventService.createEvent(request, authenticatedUser.userExternalKey());
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{externalId}")
    public EventResponse updateEvent(
            @PathVariable final String externalId,
            @Valid @RequestBody final EventRequest request,
            Authentication authentication
    ) {
        final AuthenticatedUser authenticatedUser =  (AuthenticatedUser) authentication.getPrincipal();
        return eventService.updateEvent(externalId, request, authenticatedUser);
    }

    @PostMapping(value = "/{eventExternalId}/ticket-types")
    public TicketTypeResponse createTicketType(
            @PathVariable final String eventExternalId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest,
            Authentication authentication
    ) {
        final AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ticketTypeService.createTicketType(eventExternalId, ticketTypeRequest, authenticatedUser);
    }
}
