package com.entri.modules.events.service.mapper;

import com.entri.modules.events.dto.EventResponse;
import com.entri.modules.events.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    EventResponse toEventResponse(Event event);
}
