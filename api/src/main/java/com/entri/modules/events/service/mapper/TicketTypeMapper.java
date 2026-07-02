package com.entri.modules.events.service.mapper;

import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.TicketType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {
    TicketTypeResponse toTicketTypeResponse(TicketType ticketType);
}
