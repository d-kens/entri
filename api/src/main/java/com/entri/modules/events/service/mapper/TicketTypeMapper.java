package com.entri.modules.events.service.mapper;

import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {
    @Mapping(target = "availableQuantity", source = "availableQuantity")
    TicketTypeResponse toTicketTypeResponse(TicketType ticketType, Integer availableQuantity);
}
