package com.entri.modules.events.controller;

import com.entri.common.dto.AuthenticatedUser;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    @RequestMapping(value = "/events/{eventExternalId}/ticket-types", method = RequestMethod.POST)
    public TicketTypeResponse createTicketType(
            @PathVariable final String eventExternalId,
            @Valid @RequestBody final CreateTicketTypeRequest ticketTypeRequest,
            Authentication authentication
    ) {
        final AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ticketTypeService.createTicketType(eventExternalId, ticketTypeRequest, authenticatedUser);
    }
}