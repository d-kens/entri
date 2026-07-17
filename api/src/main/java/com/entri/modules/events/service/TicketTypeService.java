package com.entri.modules.events.service;

import com.entri.common.dto.AuthenticatedUser;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.UnauthorizedException;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import com.entri.modules.events.service.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeResponse getTicketTypeById(final Long ticketTypeId) {
        TicketType ticketType = return ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

    public TicketTypeResponse updateTicketType(final Long ticketTypeId, final TicketTypeRequest ticketTypeRequest) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

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

    public TicketTypeResponse createTicketType(
            final String eventExternalId,
            final TicketTypeRequest ticketTypeRequest,
            AuthenticatedUser authenticatedUser
    ) {
        Event event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventExternalId + " not found"));

        boolean isEventOwner = event.getOrganizer().getExternalKey().equals(authenticatedUser.userExternalKey());

        if (!isEventOwner && !authenticatedUser.isPlatformAdmin()) {
            throw new UnauthorizedException("Only the event owner or a platform administrator can perform this action.");
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
}
