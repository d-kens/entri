package com.parrcel.api.modules.zones.mapper;

import com.parrcel.api.modules.zones.dto.ParcelPointResponse;
import com.parrcel.api.modules.zones.model.ParrcelPoint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParrcelPointMapper {
    ParcelPointResponse toResponse(ParrcelPoint parrcelPoint);
}
