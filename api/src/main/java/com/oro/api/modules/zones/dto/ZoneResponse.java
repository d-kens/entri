package com.oro.api.modules.zones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ZoneResponse {
    private Long id;
    private String zoneName;
    private String city;
    private Boolean isActive;
    private Boolean isCbd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
