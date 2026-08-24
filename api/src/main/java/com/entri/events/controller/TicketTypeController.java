package com.entri.events.controller;

import com.entri.security.UserPrincipal;
import com.entri.events.controller.api.TicketTypeApi;
import com.entri.events.dto.TicketTypeRequest;
import com.entri.events.dto.TicketTypeResponse;
import com.entri.events.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TicketTypeController implements TicketTypeApi {
    private final TicketTypeService ticketTypeService;

    @Override
    public TicketTypeResponse getTicketType(final Long ticketTypeId) {
        return ticketTypeService.getTicketType(ticketTypeId);
    }

    @Override
    public TicketTypeResponse updateTicketType(
            final Long ticketTypeId,
            final TicketTypeRequest ticketTypeRequest,
            final UserPrincipal user
    ) {
        return ticketTypeService.updateTicketType(ticketTypeId, ticketTypeRequest, user);
    }

    @Override
    public ResponseEntity<Void> deleteTicketType(
            final long ticketTypeId,
            final UserPrincipal user
    ) {
        ticketTypeService.deleteTicketType(ticketTypeId, user);
        return ResponseEntity.noContent().build();
    }
}
