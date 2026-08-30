package com.entri.events.controller;

import com.entri.events.controller.api.CategoryApi;
import com.entri.events.dto.CategoryRequest;
import com.entri.events.dto.CategoryResponse;
import com.entri.events.service.EventCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {
    private final EventCategoryService eventCategoryService;

    @Override
    public List<CategoryResponse> getCategories() {
        return eventCategoryService.getAllCategories();
    }

    @Override
    public ResponseEntity<CategoryResponse> createCategory(final UriComponentsBuilder uriComponentsBuilder, final CategoryRequest request) {
        var category = eventCategoryService.createCategory(request);
        URI location = uriComponentsBuilder.path("/categories/{id}").buildAndExpand(category.id()).toUri();
        return ResponseEntity.created(location).body(category);
    }

    @Override
    public CategoryResponse updateCategory(final Long id, final CategoryRequest request) {
        return eventCategoryService.updateCategory(id, request);
    }

    @Override
    public void deleteCategory(final Long id) {
        eventCategoryService.deleteCategory(id);
    }
}
