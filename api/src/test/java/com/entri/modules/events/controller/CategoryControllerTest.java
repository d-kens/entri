package com.entri.modules.events.controller;

import com.entri.events.controller.CategoryController;
import com.entri.events.dto.CategoryRequest;
import com.entri.events.dto.CategoryResponse;
import com.entri.events.service.EventCategoryService;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    EventCategoryService eventCategoryService;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(eventCategoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getCategories_returnsAllCategories() throws Exception {
        when(eventCategoryService.getAllCategories())
                .thenReturn(List.of(new CategoryResponse(1L, "Music", "Music events")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Music"));
    }

    @Test
    void createCategory_validRequest_returns201WithLocation() throws Exception {
        var request = new CategoryRequest("Music", "Music events");
        var response = new CategoryResponse(1L, "Music", "Music events");
        when(eventCategoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/categories/1")))
                .andExpect(jsonPath("$.name").value("Music"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        var request = new CategoryRequest("", "Music events");

        mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void updateCategory_validRequest_returnsUpdatedCategory() throws Exception {
        var request = new CategoryRequest("Music", "Updated description");
        var response = new CategoryResponse(1L, "Music", "Updated description");
        when(eventCategoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/categories/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateCategory_notFound_returns404() throws Exception {
        var request = new CategoryRequest("Music", "Updated description");
        when(eventCategoryService.updateCategory(eq(99L), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put("/categories/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_existingCategory_returns204() throws Exception {
        mockMvc.perform(delete("/categories/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(eventCategoryService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Category not found"))
                .when(eventCategoryService).deleteCategory(99L);

        mockMvc.perform(delete("/categories/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
