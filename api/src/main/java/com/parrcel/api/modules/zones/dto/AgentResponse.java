package com.parrcel.api.modules.zones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AgentResponse {
    private Long id;
    private String name;
    private Long zoneId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressDescription;
    private Boolean isActive;
    private String phone;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
