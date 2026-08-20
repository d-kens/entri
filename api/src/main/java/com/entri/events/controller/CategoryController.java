package com.entri.events.controller;

import com.entri.events.controller.api.CategoryApi;
import com.entri.events.dto.CategoryResponse;
import com.entri.events.service.EventCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {
    private final EventCategoryService eventCategoryService;

    @Override
    public List<CategoryResponse> getCategories() {
        return eventCategoryService.getAllCategories();
    }
}
