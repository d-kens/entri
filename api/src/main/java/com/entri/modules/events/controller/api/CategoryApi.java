package com.entri.modules.events.controller.api;

import com.entri.modules.events.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/categories")
public interface CategoryApi {

    @Operation(
            operationId = "getCategories",
            summary = "Get All Categories",
            description = "Retrieves a list of all available event categories"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully"
            )
    })
    @GetMapping
    List<CategoryResponse> getCategories();
}
