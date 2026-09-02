package com.entri.events.service;

import com.entri.checkout.dto.PaymentResult;
import com.entri.checkout.enums.PaymentStatus;
import com.entri.events.exception.EventNotOnSaleException;
import com.entri.events.exception.InvalidReservationStatusException;
import com.entri.events.exception.InsufficientTicketsException;
import com.entri.events.exception.MaxTicketsPerOrderExceededException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.BadRequestException;
import com.entri.events.dto.EventTicketReservationDetailDto;
import com.entri.events.dto.EventTicketReservationItemDto;
import com.entri.events.dto.EventTicketReservationItemRequest;
import com.entri.events.dto.EventTicketReservationRequest;
import com.entri.events.dto.EventTicketReservationResponse;
import com.entri.events.entity.Event;
import com.entri.events.entity.EventStatus;
import com.entri.events.entity.EventTicketReservation;
import com.entri.events.entity.EventTicketReservationItem;
import com.entri.events.entity.EventTicketReservationStatus;
import com.entri.events.entity.TicketType;
import com.entri.events.entity.TicketTypeSaleStatus;
import com.entri.events.repository.EventRepository;
import com.entri.events.repository.EventTicketReservationRepository;
import com.entri.events.repository.TicketTypeRepository;
import com.entri.tickets.ReservationEventPublisher;
import com.entri.tickets.dto.ReservationConfirmedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventTicketReservationService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventTicketReservationRepository eventTicketReservationRepository;
    private final ReservationEventPublisher reservationEventPublisher;

    @Value("${events.reservation.hold-duration:PT10M}")
    private Duration holdDuration;

    @Value("${events.reservation.expiration.batch-size:500}")
    private int batchSize;


    @Transactional
    public EventTicketReservation getPendingReservation(final String reservationId) {
        var reservation = eventTicketReservationRepository.findByExternalIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + reservationId + " not found"));

        if (reservation.getStatus() != EventTicketReservationStatus.PENDING) {
            throw new InvalidReservationStatusException(
                    "Reservation with ID " + reservationId + " is not available for payment"
            );
        }

        return reservation;
    }

    @Transactional
    public void applyPaymentResult(final PaymentResult result) {
        var reservation = eventTicketReservationRepository
                .findByExternalIdForUpdate(result.reservationExternalId())
                .orElse(null);

        if (reservation == null || reservation.getStatus() != EventTicketReservationStatus.PENDING) {
            return;
        }

        var ticketTypeIds = reservation.getItems().stream()
                .map(item -> item.getTicketType().getId())
                .sorted()
                .toList();

        var ticketTypesById = ticketTypeRepository.findAllForUpdate(ticketTypeIds)
                .stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (var item : reservation.getItems()) {
            var ticketType = ticketTypesById.get(item.getTicketType().getId());
            ticketType.setReservedQuantity(ticketType.getReservedQuantity() - item.getQuantity());
            if (result.status() == PaymentStatus.PAID) {
                ticketType.setSoldQuantity(ticketType.getSoldQuantity() + item.getQuantity());
            }
        }

        var newStatus = result.status() == PaymentStatus.PAID
                ? EventTicketReservationStatus.CONFIRMED
                : EventTicketReservationStatus.FAILED;
        reservation.setStatus(newStatus);

        if (newStatus == EventTicketReservationStatus.CONFIRMED) {
            reservationEventPublisher.publishReservationConfirmed(
                    new ReservationConfirmedMessage(reservation.getExternalId())
            );
        }
    }

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
                reservation.getStatus(),
                items
        );
    }

    @Transactional
    public int expireReservations() {
        List<EventTicketReservation> reservations = eventTicketReservationRepository
                .findExpiredPendingReservations(Instant.now(), batchSize);

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
            if (ticketType.getSaleStatus() != TicketTypeSaleStatus.ON_SALE) {
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
                .expiresAt(Instant.now().plus(holdDuration))
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
