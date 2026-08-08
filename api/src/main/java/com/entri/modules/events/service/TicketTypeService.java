package com.entri.modules.events.service;

import com.entri.common.exception.InsufficientTicketsException;
import com.entri.common.security.AuthenticatedUser;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.UnauthorizedException;
import com.entri.modules.events.dto.*;
import com.entri.modules.events.entity.*;
import com.entri.modules.events.repository.TicketTypeReservationSum;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.EventTicketReservationRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import com.entri.modules.events.service.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventTicketReservationRepository eventTicketReservationRepository;

    public TicketTypeResponse getTicketType(final Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    @Transactional
    public TicketTypeResponse updateTicketType(final Long ticketTypeId, final TicketTypeRequest ticketTypeRequest, final AuthenticatedUser user) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        if (!user.isPlatformAdmin() && !user.userExternalKey().equals(ticketType.getEvent().getOrganizer().getExternalKey())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }

        ticketType.setName(ticketTypeRequest.name());
        ticketType.setDescription(ticketTypeRequest.description());
        ticketType.setPrice(ticketTypeRequest.price());
        ticketType.setCurrency(ticketTypeRequest.currency());
        ticketType.setQuantity(ticketTypeRequest.quantity());
        ticketType.setMaxTicketsPerOrder(ticketTypeRequest.maxTicketsPerOrder());
        ticketType.setSaleStartDate(ticketTypeRequest.saleStartDate());
        ticketType.setSaleEndDate(ticketTypeRequest.saleEndDate());

        ticketTypeRepository.save(ticketType);
        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    @Transactional
    public void deleteTicketType(final Long ticketTypeId, final AuthenticatedUser user) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        if (!user.isPlatformAdmin() && !user.userExternalKey().equals(ticketType.getEvent().getOrganizer().getExternalKey())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }

        if (ticketType.getDeletedAt() != null) {
            return;
        }

        ticketType.setDeletedAt(Instant.now());
    }

    @Transactional
    public TicketTypeResponse createEventTicketType(
            final String eventExternalId,
            final TicketTypeRequest ticketTypeRequest,
            final AuthenticatedUser user
    ) {
        Event event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));

        if (!user.isPlatformAdmin() && !user.userExternalKey().equals(event.getOrganizer().getExternalKey())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(ticketTypeRequest.name())
                .description(ticketTypeRequest.description())
                .price(ticketTypeRequest.price())
                .currency(ticketTypeRequest.currency())
                .quantity(ticketTypeRequest.quantity())
                .maxTicketsPerOrder(ticketTypeRequest.maxTicketsPerOrder())
                .saleStartDate(ticketTypeRequest.saleStartDate())
                .saleEndDate(ticketTypeRequest.saleEndDate())
                .build();

        ticketTypeRepository.save(ticketType);

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    @Transactional
    public EventTicketReservationResponse reserveEventTickets(
            final String eventExternalId,
            final EventTicketReservationRequest request
    ) {

        /**
         * TODO:
         1.  Event status not checked - Should be a published event
         2.  Ticket type status not checked - Should be an active ticket type
         3.  maxTicketsPerOrder not enforced
         4.  Sale date window not checked
         */




        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));

        var ticketTypeIds = request.itemRequests()
                .stream()
                .map(EventTicketReservationItemRequest::ticketTypeId)
                .sorted()
                .toList();

        var ticketTypes = ticketTypeRepository.findAllForUpdate(ticketTypeIds);

        validateTicketTypes(ticketTypes, ticketTypeIds, event);
        validateAvailability(ticketTypes, request);

        var reservation = createReservation(ticketTypes, request);
        eventTicketReservationRepository.save(reservation);

        return new EventTicketReservationResponse(
                reservation.getExpiresAt(),
                reservation.getExternalId(),
                reservation.getTotalAmount());
    }

    private void validateTicketTypes(
            final List<TicketType> ticketTypes,
            final List<Long> requestedTicketTypeIds,
            final Event event
    ) {
        var foundTicketTypeIds = ticketTypes.stream()
                .map(TicketType::getId)
                .collect(Collectors.toSet());

        if (!foundTicketTypeIds.containsAll(requestedTicketTypeIds)) {
            throw new ResourceNotFoundException(
                    "One or more ticket types were not found"
            );
        }

        var belongsToEvent = ticketTypes.stream()
                .allMatch(ticketType ->
                        ticketType.getEvent().getId().equals(event.getId())
                );

        if (!belongsToEvent) {
            throw new ResourceNotFoundException(
                    "One or more ticket types do not belong to event "
                            + event.getExternalId()
            );
        }
    }

    private void validateAvailability(
            final List<TicketType> ticketTypes,
            final EventTicketReservationRequest request
    ) {
        var ticketTypeIds = ticketTypes.stream().map(TicketType::getId).toList();

        var reservedByTicketType = eventTicketReservationRepository
                .sumActiveReservationsByTicketTypes(ticketTypeIds, TicketReservationStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(
                        TicketTypeReservationSum::ticketTypeId,
                        TicketTypeReservationSum::reservedQuantity
                ));

        var ticketTypesById = ticketTypes.stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (var item : request.itemRequests()) {
            var ticketType = ticketTypesById.get(item.ticketTypeId());
            var activeReservedQuantity = reservedByTicketType.getOrDefault(ticketType.getId(), 0L);
            var availableQuantity =
                    ticketType.getQuantity()
                            - ticketType.getSoldQuantity()
                            - activeReservedQuantity;

            if (item.quantity() > availableQuantity) {
                throw new InsufficientTicketsException(
                        "Insufficient tickets available for ticket type "
                                + ticketType.getId()
                );
            }
        }
    }

    private EventTicketReservation createReservation(
            final List<TicketType> ticketTypes,
            final EventTicketReservationRequest request
    ) {
        var ticketTypesById = ticketTypes.stream()
                .collect(Collectors.toMap(
                        TicketType::getId,
                        Function.identity()
                ));

        var reservation = EventTicketReservation.builder()
                .status(TicketReservationStatus.PENDING)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .totalAmount(BigDecimal.ZERO)
                .build();

        var totalAmount = BigDecimal.ZERO;

        for (var item : request.itemRequests()) {

            var ticketType = ticketTypesById.get(item.ticketTypeId());

            var unitPrice = ticketType.getPrice();

            var itemTotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.quantity())
            );

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
