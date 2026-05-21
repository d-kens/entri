package com.api.modules.properties.service.mapper;

import com.api.modules.properties.dto.PropertyResponse;
import com.api.modules.properties.entity.Property;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PropertyMapper {
    PropertyResponse toPropertyresponse(Property property);
}
