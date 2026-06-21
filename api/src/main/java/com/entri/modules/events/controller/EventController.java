package com.entri.modules.events.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.modules.events.dto.EventRequest;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventFilter;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @GetMapping()
    public PaginationResponse<EventResponse> browseEvents(
            @Valid final EventFilter filter
    ) {
        return  eventService.getEvents(filter);
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
            @AuthenticationPrincipal String currentUserKey,
            @Valid @RequestBody final EventRequest request
    ) {
        var response = eventService.createEvent(request, currentUserKey);
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{externalId}")
    public EventResponse updateEvent(
            @PathVariable final String externalId,
            @AuthenticationPrincipal String currentUserKey,
            @Valid @RequestBody final EventRequest request
    ) {
        return eventService.updateEvent(externalId, request, currentUserKey);
    }
}
