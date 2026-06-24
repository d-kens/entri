package com.entri.modules.service;


import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.CreateTicketTypeRequest;
import com.entri.modules.events.repository.EventRepository;
import com.entri.modules.events.repository.TicketTypeRepository;
import com.entri.modules.events.service.TicketTypeService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TicketTypeServiceTest {

    @InjectMocks
    TicketTypeService ticketTypeService;

    @Mock
    TicketTypeRepository ticketTypeRepository;

    @Mock
    EventRepository eventRepository;

    @Test
    void createTicketType_eventNotFound_throwsNotFoundException() {
        String eventExternalId = "550e8400-e29b-41d4-a716-446655440000";
        CreateTicketTypeRequest ticketTypeRequest = getTicketTypeRequest();
        when(eventRepository.findByExternalId(eventExternalId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> ticketTypeService.createTicketType(eventExternalId, ticketTypeRequest));
        assertThat(exception.getMessage()).isEqualTo("Event with ID " + eventExternalId + " not found");
    }

    private static @NonNull CreateTicketTypeRequest getTicketTypeRequest() {
        String name = "Regular";
        String description = "Regular ticket";
        BigDecimal price = BigDecimal.valueOf(5000);
        String currency = "KES";
        Integer quantity = 100;
        Integer maxTicketsPerOrder = 10;
        Instant saleStartDate = Instant.parse("2025-11-01T00:00:00Z");
        Instant saleEndDate = Instant.parse("2025-12-14T23:59:59Z");
        Integer displayOrder = 1;
        Boolean isHidden = false;

        CreateTicketTypeRequest createTicketTypeRequest = new CreateTicketTypeRequest(
                name, description, price, currency, quantity, maxTicketsPerOrder,
                saleStartDate, saleEndDate, displayOrder, isHidden
        );
        return createTicketTypeRequest;
    }
}
