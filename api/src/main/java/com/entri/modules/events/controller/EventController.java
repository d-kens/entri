package com.entri.modules.events.controller;

import com.entri.common.security.AuthenticatedUser;
import com.entri.infrastructure.ratelimit.RateLimited;
import com.entri.common.dto.PaginationResponse;
import com.entri.modules.events.controller.api.EventApi;
import com.entri.modules.events.dto.*;
import com.entri.modules.events.service.EventService;
import com.entri.modules.events.service.EventTicketReservationService;
import com.entri.modules.events.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController implements EventApi {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;
    private final EventTicketReservationService eventTicketReservationService;

    @Override
    public EventResponse getEventByExternalId(
            @PathVariable final String eventExternalId
    ) {
        return eventService.getEventByExternalId(eventExternalId);
    }

    @Override
    public EventResponse publishEvent(@PathVariable String eventExternalId,  @AuthenticationPrincipal final AuthenticatedUser user) {
        return eventService.publishEvent(eventExternalId, user);
    }

    @Override
    public List<TicketTypeResponse> getEventTicketTypes(@PathVariable String eventExternalId) {
        return ticketTypeService.getTicketTypesByEventExternalId(eventExternalId);
    }

    @Override
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody final EventRequest request,
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        var response = eventService.createEvent(request, user.userExternalKey());
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public EventResponse updateEvent(
            @PathVariable final String externalId,
            @Valid @RequestBody final EventRequest request,
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        return eventService.updateEvent(externalId, request, user);
    }

    @Override
    public PaginationResponse<EventResponse> listEvents(
            @Valid final EventFilter filter
    ) {
        return eventService.listEvents(filter);
    }

    @Override
    public PaginationResponse<EventResponse> listOrganizerEvents(
            @Valid final EventFilter filter,
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        String userKey = user.isPlatformAdmin() ? null : user.userExternalKey();
        return eventService.listOrganizerEvents(filter, userKey);
    }

    @RateLimited
    @Override
    public ResponseEntity<EventTicketReservationResponse> reserveEventTickets(
            UriComponentsBuilder uriComponentsBuilder,
            @PathVariable final String eventExternalId,
            @Valid @RequestBody final EventTicketReservationRequest eventTicketReservationRequest
    ) {
        var response = eventTicketReservationService.reserveEventTickets(eventExternalId, eventTicketReservationRequest);
        var uri = uriComponentsBuilder
                .path("/events/{eventExternalId}/reservations/{reservationId}")
                .buildAndExpand(
                        eventExternalId,
                        response.reservationId()
                )
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public EventTicketReservationDetailDto getEventTicketReservation(
            @PathVariable final String eventExternalId,
            @PathVariable  String reservationId
    ) {
        return eventTicketReservationService.getEventTicketReservation(eventExternalId, reservationId);
    }

    @Override
    public ResponseEntity<TicketTypeResponse> createEventTicketType(
            UriComponentsBuilder uriComponentsBuilder,
            @PathVariable final String eventExternalId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest,
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        var response = ticketTypeService.createEventTicketType(eventExternalId, ticketTypeRequest, user);
        var uri = uriComponentsBuilder.path("/ticket-types/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
