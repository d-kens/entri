package com.entri.events.service.mapper;

import com.entri.events.dto.TicketTypeResponse;
import com.entri.events.entity.TicketType;
import com.entri.events.entity.TicketTypeAvailabilityStatus;
import com.entri.events.entity.TicketTypeSaleStatus;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TicketTypeMapper {

    TicketTypeResponse toTicketTypeResponse(
            TicketType ticketType,
            TicketTypeSaleStatus saleStatus,
            TicketTypeAvailabilityStatus availabilityStatus,
            Integer availableQuantity
    );

    default TicketTypeResponse toTicketTypeResponse(TicketType ticketType) {
        return toTicketTypeResponse(
                ticketType,
                ticketType.getSaleStatus(),
                ticketType.getAvailabilityStatus(),
                ticketType.getAvailableQuantity()
        );
    }
}
