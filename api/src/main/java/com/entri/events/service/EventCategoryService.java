package com.entri.events.service;

import com.entri.exception.ResourceNotFoundException;
import com.entri.events.dto.CategoryRequest;
import com.entri.events.dto.CategoryResponse;
import com.entri.events.entity.EventCategory;
import com.entri.events.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCategoryService {
    private final EventCategoryRepository eventCategoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return eventCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(final CategoryRequest request) {
        var category = EventCategory.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return toResponse(eventCategoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(final Long id, final CategoryRequest request) {
        var category = findById(id);
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(eventCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(final Long id) {
        var category = findById(id);
        eventCategoryRepository.delete(category);
    }

    public EventCategory findById(final Long id) {
        return eventCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private CategoryResponse toResponse(EventCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
