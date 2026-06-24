package com.entri.modules.events.service;

import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {


    public TicketTypeResponse createTicketType(String eventExternalId, CreateTicketTypeRequest createTicketTypeRequest) {
        return null;
    }
}
