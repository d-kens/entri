package com.api.modules.events.service;

import com.api.common.exception.NotFoundException;
import com.api.modules.events.entity.EventCategory;
import com.api.modules.events.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventCategoryService {
    private final EventCategoryRepository eventCategoryRepository;

    public EventCategory findById(Long id) {
        return eventCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
