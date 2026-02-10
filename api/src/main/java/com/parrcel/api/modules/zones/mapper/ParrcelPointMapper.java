package com.parrcel.api.modules.zones.mapper;

import com.parrcel.api.modules.zones.dto.AgentResponse;
import com.parrcel.api.modules.zones.model.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParrcelPointMapper {
    @Mapping(target = "zoneId", source = "zone.id")
    AgentResponse toResponse(Agent agent);
}
