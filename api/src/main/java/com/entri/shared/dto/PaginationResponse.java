package com.entri.shared.dto;

import java.util.List;

public record PaginationResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}