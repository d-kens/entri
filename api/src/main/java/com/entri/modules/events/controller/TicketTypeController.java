package com.entri.modules.events.controller;

import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    @RequestMapping(value = "/events/{eventExternalId}/ticket-types", method = RequestMethod.GET)
    public TicketTypeResponse createTicketType(
            @PathVariable final String eventExternalId,
            @RequestBody final CreateTicketTypeRequest ticketTypeRequest,
            Authentication authentication
    ) {
        final String currentUserKey = (String) authentication.getPrincipal();
        final boolean isPlatformAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
        return ticketTypeService.createTicketType(eventExternalId, ticketTypeRequest, currentUserKey, isPlatformAdmin);
    }
}