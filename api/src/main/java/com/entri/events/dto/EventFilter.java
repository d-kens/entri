package com.entri.events.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record EventFilter(
        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size,

        @Pattern(regexp = "ASC|DESC")
        String sortDirection,

        Long categoryId,

        String searchTerm,

        String startFrom,

        String startTo
) {
    public EventFilter {
        searchTerm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();
        startFrom  = (startFrom  == null || startFrom.isBlank())  ? null : startFrom.trim();
        startTo    = (startTo    == null || startTo.isBlank())    ? null : startTo.trim();

        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortDirection = (sortDirection == null) ? "ASC" : sortDirection.toUpperCase();
    }
}
