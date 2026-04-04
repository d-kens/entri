package com.oro.api.modules.routes.entity;

import com.oro.api.common.entity.AbstractAuditableEntity;
import com.oro.api.modules.deliveries.entity.Delivery;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "delivery_batches")
public class DeliveryBatch extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private DeliveryRoute route;

    @OneToMany(mappedBy = "batch", fetch = FetchType.LAZY)
    private List<Delivery> deliveries = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status = BatchStatus.OPEN;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_phone", length = 15)
    private String driverPhone;

    @PrePersist
    protected void onCreate() {
        if (externalId == null) {
            externalId = UUID.randomUUID().toString();
        }
    }
}
