package com.entri.modules.events.service;

import com.entri.common.security.AuthenticatedUser;
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

    public TicketTypeResponse getTicketType(final Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket type with ID " + ticketTypeId + " not found")
        );

        return ticketTypeMapper.toTicketTypeResponse(ticketType);
    }

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
}
