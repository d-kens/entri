package com.entri.events.service.mapper;

import com.entri.events.dto.EventResponse;
import com.entri.events.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    EventResponse toEventResponse(Event event);
}
