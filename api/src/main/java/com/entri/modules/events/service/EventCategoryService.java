package com.entri.modules.events.service;

import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.entity.EventCategory;
import com.entri.modules.events.repository.EventCategoryRepository;
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
