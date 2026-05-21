package com.api.modules.properties.controller;

import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.dto.PropertyResponse;
import com.api.modules.properties.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> createProperty(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @ModelAttribute CreatePropertyRequest request,
            @AuthenticationPrincipal String currentUserExternalKey
    ) {
        var response = propertyService.createProperty(request, currentUserExternalKey);
        var uri = uriComponentsBuilder.path("/properties/{id}").buildAndExpand(response.externalKey()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
