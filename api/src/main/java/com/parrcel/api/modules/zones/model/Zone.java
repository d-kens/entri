package com.parrcel.api.modules.zones.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "zones",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_zone_name_city", columnNames = {"zone_name", "city"})
        }
)
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_cbd", nullable = false)
    private Boolean isCbd;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
