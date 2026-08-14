package com.entri.modules.events.service;

import com.entri.common.exception.EventNotOnSaleException;
import com.entri.common.exception.InsufficientTicketsException;
import com.entri.common.exception.MaxTicketsPerOrderExceededException;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.BadRequestException;
import com.entri.modules.events.dto.*;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.EventStatus;
import com.entri.modules.events.entity.EventTicketReservation;
import com.entri.modules.events.entity.EventTicketReservationItem;
import com.entri.modules.events.entity.EventTicketReservationStatus;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.entity.TicketTypeStatus;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.EventTicketReservationRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventTicketReservationService {

    private final Clock clock;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventTicketReservationRepository eventTicketReservationRepository;

    @Value("${events.reservation.hold-duration:PT10M}")
    private Duration holdDuration;

    @Value("${events.reservation.expiration.batch-size:500}")
    private int batchSize;


    @Transactional(readOnly = true)
    public EventTicketReservationDetailDto getEventTicketReservation(final String eventExternalId, final String reservationExternalId) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));

        var reservation = eventTicketReservationRepository.findByExternalIdWithItems(reservationExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + reservationExternalId + " not found"));

        if (!reservation.getEvent().getId().equals(event.getId())) {
            throw new BadRequestException("Reservation " + reservationExternalId + " does not belong to event " + eventExternalId);
        }

        var items = reservation.getItems().stream()
                .map(item -> new EventTicketReservationItemDto(
                        item.getQuantity(),
                        item.getTicketType().getName(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();

        return new EventTicketReservationDetailDto(
                reservation.getExpiresAt(),
                reservation.getExternalId(),
                reservation.getTotalAmount(),
                eventExternalId,
                items
        );
    }

    @Transactional
    public int expireReservations() {
        List<EventTicketReservation> reservations = eventTicketReservationRepository
                .findExpiredPendingReservations(clock.instant(), batchSize);

        if (reservations.isEmpty()) {
            return 0;
        }

        List<Long> ticketTypeIds = reservations.stream()
                .flatMap(r -> r.getItems().stream())
                .map(item -> item.getTicketType().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, TicketType> ticketTypesById = ticketTypeRepository
                .findAllForUpdate(ticketTypeIds)
                .stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (EventTicketReservation reservation : reservations) {
            for (EventTicketReservationItem item : reservation.getItems()) {
                TicketType ticketType = ticketTypesById.get(item.getTicketType().getId());
                ticketType.setReservedQuantity(ticketType.getReservedQuantity() - item.getQuantity());
            }
            reservation.setStatus(EventTicketReservationStatus.EXPIRED);
        }

        return reservations.size();
    }

    @Transactional
    public EventTicketReservationResponse reserveEventTickets(
            final String eventExternalId,
            final EventTicketReservationRequest request
    ) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventNotOnSaleException("Tickets for this event are not currently on sale");
        }

        var ticketTypeIds = request.itemRequests()
                .stream()
                .map(EventTicketReservationItemRequest::ticketTypeId)
                .sorted()
                .toList();

        var ticketTypes = ticketTypeRepository.findAllForUpdate(ticketTypeIds);

        validateTicketTypes(ticketTypes, ticketTypeIds, event);

        var ticketTypesById = ticketTypes.stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        validateAvailability(ticketTypesById, request);

        var reservation = createReservation(event, ticketTypesById, request);
        eventTicketReservationRepository.save(reservation);

        return new EventTicketReservationResponse(
                reservation.getExpiresAt(),
                reservation.getExternalId(),
                reservation.getTotalAmount(),
                eventExternalId
        );
    }

    private void validateTicketTypes(
            final List<TicketType> ticketTypes,
            final List<Long> requestedTicketTypeIds,
            final Event event
    ) {
        var uniqueIds = new HashSet<>(requestedTicketTypeIds);
        if (uniqueIds.size() != requestedTicketTypeIds.size()) {
            throw new IllegalArgumentException("Request contains duplicate ticket type IDs");
        }

        var foundTicketTypeIds = ticketTypes.stream()
                .map(TicketType::getId)
                .collect(Collectors.toSet());

        if (!foundTicketTypeIds.containsAll(requestedTicketTypeIds)) {
            throw new ResourceNotFoundException("One or more ticket types were not found");
        }

        var belongsToEvent = ticketTypes.stream()
                .allMatch(ticketType -> ticketType.getEvent().getId().equals(event.getId()));

        if (!belongsToEvent) {
            throw new BadRequestException("One or more ticket types do not belong to this event");
        }

        for (TicketType ticketType : ticketTypes) {
            if (ticketType.getStatus() != TicketTypeStatus.ACTIVE) {
                throw new BadRequestException("Ticket type is not available for purchase");
            }
        }
    }

    private void validateAvailability(
            final Map<Long, TicketType> ticketTypesById,
            final EventTicketReservationRequest request
    ) {
        for (var item : request.itemRequests()) {
            var ticketType = ticketTypesById.get(item.ticketTypeId());

            if (ticketType.getMaxTicketsPerOrder() != null
                    && item.quantity() > ticketType.getMaxTicketsPerOrder()) {
                throw new MaxTicketsPerOrderExceededException(
                        "Quantity exceeds the maximum of "
                                + ticketType.getMaxTicketsPerOrder()
                                + " tickets per order for ticket type " + ticketType.getId()
                );
            }

            var availableQuantity =
                    ticketType.getQuantity()
                            - ticketType.getSoldQuantity()
                            - ticketType.getReservedQuantity();

            if (item.quantity() > availableQuantity) {
                throw new InsufficientTicketsException(
                        "Only %d ticket(s) available for '%s' — requested %d. Reduce quantity and retry."
                                .formatted(availableQuantity, ticketType.getName(), item.quantity())
                );
            }
        }
    }

    private EventTicketReservation createReservation(
            Event event,
            final Map<Long, TicketType> ticketTypesById,
            final EventTicketReservationRequest request
    ) {
        var reservation = EventTicketReservation.builder()
                .event(event)
                .status(EventTicketReservationStatus.PENDING)
                .expiresAt(clock.instant().plus(holdDuration))
                .totalAmount(BigDecimal.ZERO)
                .build();

        var totalAmount = BigDecimal.ZERO;

        for (var item : request.itemRequests()) {
            var ticketType = ticketTypesById.get(item.ticketTypeId());

            ticketType.setReservedQuantity(ticketType.getReservedQuantity() + item.quantity());

            var unitPrice = ticketType.getPrice();
            var itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));

            var reservationItem = EventTicketReservationItem.builder()
                    .ticketType(ticketType)
                    .quantity(item.quantity())
                    .unitPrice(unitPrice)
                    .build();

            reservation.addItem(reservationItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        reservation.setTotalAmount(totalAmount);
        return reservation;
    }
}
