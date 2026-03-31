package com.oro.api.modules.zones.mapper;

import com.oro.api.modules.zones.dto.ZoneResponse;
import com.oro.api.modules.zones.entity.Zone;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZoneMapper {
    ZoneResponse toResponse(Zone zone);
}
