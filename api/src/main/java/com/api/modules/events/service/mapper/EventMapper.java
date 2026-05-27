package com.api.modules.events.service.mapper;

import com.api.modules.events.dto.EventDetailResponse;
import com.api.modules.events.dto.EventResponse;
import com.api.modules.events.dto.TicketTypeResponse;
import com.api.modules.events.entity.Event;
import com.api.modules.events.entity.TicketType;
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
