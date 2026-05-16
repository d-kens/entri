package com.api.modules.properties.controller;

import com.api.modules.properties.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {
    private final PropertyService propertyService;
}
