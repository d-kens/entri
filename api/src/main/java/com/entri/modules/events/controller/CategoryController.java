package com.entri.modules.events.controller;

import com.entri.modules.events.controller.api.CategoryApi;
import com.entri.modules.events.dto.CategoryResponse;
import com.entri.modules.events.service.EventCategoryService;
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
