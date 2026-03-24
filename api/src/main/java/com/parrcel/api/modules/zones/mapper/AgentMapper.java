package com.parrcel.api.modules.zones.mapper;

import com.parrcel.api.modules.zones.dto.AgentResponse;
import com.parrcel.api.modules.zones.entity.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper {
    @Mapping(target = "zoneId", source = "zone.id")
    AgentResponse toResponse(Agent agent);
}
