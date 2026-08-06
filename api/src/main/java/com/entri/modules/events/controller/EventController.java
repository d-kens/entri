package com.entri.modules.events.controller;

import com.entri.common.security.AuthenticatedUser;
import com.entri.common.dto.PaginationResponse;
import com.entri.modules.events.controller.api.EventApi;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController implements EventApi {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;

    @Override
    public EventDetailResponse getEventByExternalId(
            @PathVariable final String eventExternalId
    ) {
        return eventService.getEventByExternalId(eventExternalId);
    }

    @Override
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody final EventRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var response = eventService.createEvent(request, user.userExternalKey());
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public EventResponse updateEvent(
            @PathVariable final String externalId,
            @Valid @RequestBody final EventRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return eventService.updateEvent(externalId, request, user);
    }

    @GetMapping
    public PaginationResponse<EventResponse> browseEvents(
            @Valid final EventFilter filter
    ) {
        return eventService.getPublishedEvents(filter);
    }

    @GetMapping("/manage")
    public PaginationResponse<EventResponse> manageEvents(
            @Valid final EventFilter filter,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        String userKey = user.isPlatformAdmin() ? null : user.userExternalKey();
        return eventService.getOrganizerEvents(filter, userKey);
    }

    @PostMapping(value = "/{eventExternalId}/ticket-types")
    public TicketTypeResponse createTicketType(
            @PathVariable final String eventExternalId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ticketTypeService.createTicketType(eventExternalId, ticketTypeRequest, user);
    }
}
