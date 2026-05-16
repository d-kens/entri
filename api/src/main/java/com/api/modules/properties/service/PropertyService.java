package com.api.modules.properties.service;

import com.api.modules.properties.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
}
