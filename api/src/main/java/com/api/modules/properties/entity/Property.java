package com.api.modules.properties.entity;


import com.api.common.entity.AbstractAuditableEntity;
import com.api.modules.users.entity.User;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
public class Property extends AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", nullable = false, unique = true)
    @Builder.Default
    private String externalKey = UUID.randomUUID().toString();

    @Column(name = "property_name", nullable = false)
    private String propertyName;

    private String description;

    @Column(name = "rent_due_date")
    private Integer rentDueDate;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User propertyOwner;

    private String country;

    private String city;

    private String area;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "is_listed")
    @Builder.Default
    private Boolean isListed = false;
}
