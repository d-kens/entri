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
