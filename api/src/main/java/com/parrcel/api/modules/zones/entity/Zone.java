package com.parrcel.api.modules.zones.entity;

import com.parrcel.api.common.entity.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "zones")
public class Zone extends AbstractAuditableEntity {
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
}
