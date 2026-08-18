package com.entri.modules.events.service.mapper;

import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.TicketType;
import com.entri.modules.events.entity.TicketTypeAvailabilityStatus;
import com.entri.modules.events.entity.TicketTypeSaleStatus;
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
