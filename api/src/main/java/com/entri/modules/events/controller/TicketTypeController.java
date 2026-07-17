package com.entri.modules.events.controller;
import com.entri.modules.events.dto.TicketTypeRequest;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket-types")
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    @GetMapping("/{ticketTypeId}")
    public TicketTypeResponse getTicketTypeById(@PathVariable final Long ticketTypeId) {
        return ticketTypeService.getTicketTypeById(ticketTypeId);
    }

    @PutMapping("/{ticketTypeId}")
    public TicketTypeResponse updateTicketType(
            @PathVariable final Long ticketTypeId,
            @Valid @RequestBody final TicketTypeRequest ticketTypeRequest
    ) {
        return ticketTypeService.updateTicketType(ticketTypeId, ticketTypeRequest);
    }
}