package com.entri.modules.events.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.modules.events.dto.CreateEventRequest;
import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventFilter;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @GetMapping("/browse")
    public PaginationResponse<EventResponse> browseEvents(
            @Valid EventFilter filter
    ) {
        return  eventService.getEvents(filter);
    }

    @GetMapping("/{externalId}")
    public EventDetailResponse getEventByExternalId(
            @PathVariable String externalId
    ) {
        return eventService.getEventByExternalId(externalId);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            @AuthenticationPrincipal String currentUserKey,
            @Valid @RequestBody CreateEventRequest request
    ) {
        var response = eventService.createEvent(request, currentUserKey);
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
