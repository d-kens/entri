package com.api.modules.properties.controller;

import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.entity.Property;
import com.api.modules.properties.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping
    public Property createProperty(
            @Valid @RequestBody CreatePropertyRequest request
    ) {
        return propertyService.createProperty(request);
    }
}
