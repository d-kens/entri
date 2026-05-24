package com.api.modules.events.controller;

import com.api.modules.events.dto.CreateEventRequest;
import com.api.modules.events.dto.EventResponse;
import com.api.modules.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            @AuthenticationPrincipal String currentUserKey,
            @Valid @ModelAttribute CreateEventRequest request
    ) {
        var response = eventService.createEvent(request, currentUserKey);
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
