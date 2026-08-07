package com.entri.modules.events.controller;
import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.events.controller.api.TicketTypeApi;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class TicketTypeController implements TicketTypeApi {
    private final TicketTypeService ticketTypeService;

    @Override
    public TicketTypeResponse getTicketType(@PathVariable final Long ticketTypeId) {
        return ticketTypeService.getTicketType(ticketTypeId);
    }

    @Override
    public TicketTypeResponse updateTicketType(
            @PathVariable final Long ticketTypeId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest,
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        return ticketTypeService.updateTicketType(ticketTypeId, ticketTypeRequest, user);
    }
}