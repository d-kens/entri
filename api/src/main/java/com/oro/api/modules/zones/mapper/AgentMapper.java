package com.oro.api.modules.zones.mapper;

import com.oro.api.modules.zones.dto.AgentResponse;
import com.oro.api.modules.zones.entity.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper {
    @Mapping(target = "zoneId", source = "zone.id")
    AgentResponse toResponse(Agent agent);
}
