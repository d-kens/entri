package com.entri.events.controller;

import com.entri.security.UserPrincipal;
import com.entri.ratelimit.RateLimited;
import com.entri.common.dto.PaginationResponse;
import com.entri.events.controller.api.EventApi;
import com.entri.events.dto.*;
import com.entri.events.service.EventService;
import com.entri.events.service.EventTicketReservationService;
import com.entri.events.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public EventResponse getEventByExternalId(final String eventExternalId) {
        return eventService.getEventByExternalId(eventExternalId);
    }

    @Override
    public EventResponse publishEvent(final String eventExternalId, final UserPrincipal requestingUser) {
        return eventService.publishEvent(eventExternalId, requestingUser);
    }

    @Override
    public EventResponse cancelEvent(final String eventExternalId, final UserPrincipal requestingUser) {
        return eventService.cancelEvent(eventExternalId, requestingUser);
    }

    @Override
    public List<TicketTypeResponse> getEventTicketTypes(String eventExternalId) {
        return ticketTypeService.getTicketTypesByEventExternalId(eventExternalId);
    }

    @Override
    public ResponseEntity<EventResponse> createEvent(
            UriComponentsBuilder uriComponentsBuilder,
            final EventRequest request,
            final UserPrincipal requestingUser
    ) {
        var response = eventService.createEvent(request, requestingUser.getExternalKey());
        var uri = uriComponentsBuilder.path("/events/{external_id}").buildAndExpand(response.externalId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public EventResponse updateEvent(
            final String externalId,
            final EventRequest request,
            final UserPrincipal requestingUser
    ) {
        return eventService.updateEvent(externalId, request, requestingUser);
    }

    @Override
    public PaginationResponse<EventResponse> listEvents(final EventFilter filter) {
        return eventService.listEvents(filter);
    }

    @Override
    public PaginationResponse<EventResponse> listOrganizerEvents(
            final EventFilter filter,
            final UserPrincipal requestingUser
    ) {
        String userKey = requestingUser.isAdmin() ? null : requestingUser.getExternalKey();
        return eventService.listOrganizerEvents(filter, userKey);
    }

    @RateLimited
    @Override
    public ResponseEntity<EventTicketReservationResponse> reserveEventTickets(
            UriComponentsBuilder uriComponentsBuilder,
            final String eventExternalId,
            final EventTicketReservationRequest eventTicketReservationRequest
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
            final String eventExternalId,
            String reservationId
    ) {
        return eventTicketReservationService.getEventTicketReservation(eventExternalId, reservationId);
    }

    @Override
    public ResponseEntity<TicketTypeResponse> createEventTicketType(
            UriComponentsBuilder uriComponentsBuilder,
            final String eventExternalId,
            final TicketTypeRequest ticketTypeRequest,
            final UserPrincipal requestingUser
    ) {
        var response = ticketTypeService.createEventTicketType(eventExternalId, ticketTypeRequest, requestingUser);
        var uri = uriComponentsBuilder.path("/ticket-types/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
