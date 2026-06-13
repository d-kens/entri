package com.entri.modules.events.controller;

import com.entri.modules.events.dto.CategoryResponse;
import com.entri.modules.events.service.EventCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final EventCategoryService eventCategoryService;

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return eventCategoryService.getAllCategories();
    }
}
