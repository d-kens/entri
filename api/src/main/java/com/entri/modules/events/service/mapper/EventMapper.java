package com.entri.modules.events.service.mapper;

import com.entri.modules.events.dto.EventDetailResponse;
import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.dto.TicketTypeResponse;
import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "category.name", target = "categoryName")
    EventResponse toEventResponse(Event event);

    @Mapping(source = "category.name", target = "categoryName")
    EventDetailResponse toEventDetailResponse(Event event);

    TicketTypeResponse toTicketTypeResponse(TicketType ticketType);
}
