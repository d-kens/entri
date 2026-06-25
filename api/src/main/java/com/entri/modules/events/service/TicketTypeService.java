package com.entri.modules.events.service;

import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final EventRepository eventRepository;


    public TicketTypeResponse createTicketType(String eventExternalId, CreateTicketTypeRequest createTicketTypeRequest) {

        Event event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new NotFoundException("Event with ID " + eventExternalId + " not found"));
        return null;
    }
}
