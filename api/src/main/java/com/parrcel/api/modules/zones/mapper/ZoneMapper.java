package com.parrcel.api.modules.zones.mapper;

import com.parrcel.api.modules.zones.dto.ZoneResponse;
import com.parrcel.api.modules.zones.entity.Zone;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZoneMapper {
    ZoneResponse toResponse(Zone zone);
}
