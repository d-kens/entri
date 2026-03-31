package com.oro.api.modules.deliveries.mapper;


import com.oro.api.modules.deliveries.dto.DeliveryResponseDto;
import com.oro.api.modules.deliveries.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    @Mapping(target = "toAgent", source = "toAgent.name")
    @Mapping(target = "fromAgent", source = "fromAgent.name")
    @Mapping(target = "toZone", source = "toAgent.zone.zoneName")
    @Mapping(target = "fromZone", source = "fromAgent.zone.zoneName")
    @Mapping(target = "customerName", source = "user.name")
    DeliveryResponseDto toResponseDto(Delivery delivery);
}
