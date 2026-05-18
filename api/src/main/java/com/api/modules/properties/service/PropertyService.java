package com.api.modules.properties.service;

import com.api.common.utils.SecurityUtils;
import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.entity.Property;
import com.api.modules.properties.repository.PropertyRepository;
import com.api.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {
    private final UserService userService;
    private final PropertyRepository propertyRepository;

    public Property createProperty(CreatePropertyRequest request) {
        var userExternalKey = SecurityUtils.getCurrentUserExternalKey();
        var user = userService.findEntityByExternalKey(userExternalKey);
        Property property = Property.builder()
                .propertyName(request.propertyName())
                .description(request.description())
                .rentDueDate(request.rentDueDate())
                .propertyOwner(user)
                .country(request.country())
                .area(request.area())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .coverImageUrl("https://api.coolapp.net/property-image")
                .build();
        propertyRepository.save(property);
        return new Property();
    }
}

