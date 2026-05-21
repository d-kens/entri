package com.api.modules.properties.service;

import com.api.common.storage.FirebaseStorageService;
import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.dto.PropertyResponse;
import com.api.modules.properties.entity.Property;
import com.api.modules.properties.repository.PropertyRepository;
import com.api.modules.properties.service.mapper.PropertyMapper;
import com.api.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.common.exception.FileUploadException;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {
    private final UserService userService;
    private final PropertyMapper propertyMapper;
    private final PropertyRepository propertyRepository;
    private final FirebaseStorageService firebaseStorageService;

    public PropertyResponse createProperty(final CreatePropertyRequest request, final String currentUserExternalKey) {
        var user = userService.findEntityByExternalKey(currentUserExternalKey);
        String coverImageUrl;
        try {
            coverImageUrl = firebaseStorageService.upload(request.coverImage());
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload cover image", e);
        }
        Property property = Property.builder()
                .propertyName(request.propertyName())
                .description(request.description())
                .rentDueDate(request.rentDueDate())
                .propertyOwner(user)
                .country(request.country())
                .city(request.city())
                .area(request.area())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .coverImageUrl(coverImageUrl)
                .build();
        propertyRepository.save(property);
        return propertyMapper.toPropertyresponse(property);
    }
}

