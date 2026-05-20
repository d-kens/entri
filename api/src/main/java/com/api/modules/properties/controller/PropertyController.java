package com.api.modules.properties.controller;

import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.entity.Property;
import com.api.modules.properties.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Property createProperty(
            @Valid @ModelAttribute CreatePropertyRequest request
    ) {
        return propertyService.createProperty(request);
    }
}

/***
 {
 "id": 5,
 "externalKey": "92e297bc-e86c-4d37-8dab-02a47418b16c",
 "propertyName": "This is the property name",
 "description": "This is a description",
 "rentDueDate": 5,
 "propertyOwner": {
 "id": 1,
 "externalKey": "caf3a910-e39a-40e5-a0c4-bced3fb4ad30",
 "email": "dickens.onyango@strathmore.edu",
 "role": "PLATFORM_USER",
 "passwordHash": "$2a$10$QhmdADgnfYAa9UDWt/tq5O7H96ntCwJCUjVca8AzlHzV.C/oasGTW",
 "firstName": "Onyango",
 "lastName": "Dickens",
 "phoneNumber": "254707127309",
 "createdBy": "SYSTEM",
 "dateCreated": "2026-05-18T18:19:25Z",
 "dateModified": "2026-05-18T18:19:25Z",
 "modifiedBy": "SYSTEM"
 },
 "country": "kenya",
 "city": "nairobi",
 "area": "area",
 "latitude": -1.2921,
 "longitude": 36.8219,
 "coverImageUrl": "https://storage.googleapis.com/oro-web-app.firebasestorage.app/48133aac-7ec0-4bf2-bfb8-688f73f969d0_Screenshot_2026-05-18_at_6.43.58%E2%80%AFPM.png",
 "isListed": false,
 "createdBy": "caf3a910-e39a-40e5-a0c4-bced3fb4ad30",
 "dateCreated": "2026-05-18T19:10:58.230963Z",
 "dateModified": "2026-05-18T19:10:58.230963Z",
 "modifiedBy": "caf3a910-e39a-40e5-a0c4-bced3fb4ad30"
 }
 */
