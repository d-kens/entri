package com.entri.modules.events.service;

import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeResponse createTicketType(final String eventExternalId, CreateTicketTypeRequest ticketTypeRequest) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new NotFoundException("Event with external ID: " + eventExternalId + " not found"));

        return null;
    }
}
