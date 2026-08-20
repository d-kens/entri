package com.entri.events.service;

import com.entri.shared.exception.ResourceNotFoundException;
import com.entri.events.dto.CategoryResponse;
import com.entri.events.entity.EventCategory;
import com.entri.events.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCategoryService {
    private final EventCategoryRepository eventCategoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return eventCategoryRepository.findAll()
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    public EventCategory findById(Long id) {
        return eventCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
