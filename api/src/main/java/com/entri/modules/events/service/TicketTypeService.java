package com.entri.modules.events.service;

import com.entri.common.dto.AuthenticatedUser;
import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.exception.UnauthorizedException;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
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

    public TicketTypeResponse createTicketType(
            final String eventExternalId,
            final CreateTicketTypeRequest createTicketTypeRequest,
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
                .name(createTicketTypeRequest.name())
                .description(createTicketTypeRequest.description())
                .price(createTicketTypeRequest.price())
                .currency(createTicketTypeRequest.currency())
                .quantity(createTicketTypeRequest.quantity())
                .maxTicketsPerOrder(createTicketTypeRequest.maxTicketsPerOrder())
                .saleStartDate(createTicketTypeRequest.saleStartDate())
                .saleEndDate(createTicketTypeRequest.saleEndDate())
                .build();

        ticketTypeRepository.save(ticketType);

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }
}
