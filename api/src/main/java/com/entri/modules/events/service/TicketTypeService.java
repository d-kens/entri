package com.entri.modules.events.service;

import com.entri.common.exception.BadRequestException;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.UnauthorizedException;
import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import com.entri.modules.events.service.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketType(final Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByEventExternalId(
            final String eventExternalId) {

        Event event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event with ID " + eventExternalId + " not found"));

        return event.getTicketTypes().stream()
                .map(ticketType -> {

                    return ticketTypeMapper.toTicketTypeResponse(ticketType);
                })
                .toList();
    }

    @Transactional
    public TicketTypeResponse updateTicketType(final Long ticketTypeId, final TicketTypeRequest ticketTypeRequest, final AuthenticatedUser user) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        if (!user.isPlatformAdmin() && !user.userExternalKey().equals(ticketType.getEvent().getOrganizer().getExternalKey())) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }

        validateSaleDates(ticketTypeRequest, ticketType.getEvent());

        ticketType.setName(ticketTypeRequest.name());
        ticketType.setDescription(ticketTypeRequest.description());
        ticketType.setPrice(ticketTypeRequest.price());
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

        validateSaleDates(ticketTypeRequest, event);

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(ticketTypeRequest.name())
                .description(ticketTypeRequest.description())
                .price(ticketTypeRequest.price())
                .quantity(ticketTypeRequest.quantity())
                .maxTicketsPerOrder(ticketTypeRequest.maxTicketsPerOrder())
                .saleStartDate(ticketTypeRequest.saleStartDate())
                .saleEndDate(ticketTypeRequest.saleEndDate())
                .build();

        ticketTypeRepository.save(ticketType);

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    private void validateSaleDates(TicketTypeRequest request, Event event) {
        Instant start = request.saleStartDate();
        Instant end = request.saleEndDate();

        if ((start == null) != (end == null)) {
            throw new BadRequestException("Sale start date and end date must both be provided or both omitted");
        }

        if (start == null) {
            return;
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("Sale start date must not be after sale end date");
        }

        if (start.isBefore(event.getStartTime())) {
            throw new BadRequestException("Sale start date must not be before the event start date");
        }

        if (end.isAfter(event.getEndTime())) {
            throw new BadRequestException("Sale end date must not be after the event end date");
        }
    }
}
