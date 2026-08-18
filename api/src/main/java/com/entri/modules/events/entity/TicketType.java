package com.entri.modules.events.entity;

import com.entri.common.entity.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_types")
@SQLRestriction("deleted_at IS NULL")
public class TicketType extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "max_tickets_per_order")
    private Integer maxTicketsPerOrder;

    @Column(name = "sale_start_date")
    private Instant saleStartDate;

    @Column(name = "sale_end_date")
    private Instant saleEndDate;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public TicketTypeSaleStatus getSaleStatus() {
        Instant now = Instant.now();
        if (saleStartDate != null && now.isBefore(saleStartDate)) {
            return TicketTypeSaleStatus.UPCOMING;
        }

        if (saleEndDate != null && now.isAfter(saleEndDate)) {
            return TicketTypeSaleStatus.ENDED;
        }

        return TicketTypeSaleStatus.ON_SALE;
    }

    public TicketTypeAvailabilityStatus getAvailabilityStatus() {
        return getAvailableQuantity() > 0
                ? TicketTypeAvailabilityStatus.AVAILABLE
                : TicketTypeAvailabilityStatus.SOLD_OUT;
    }


    public int getAvailableQuantity() {
        return quantity - soldQuantity - reservedQuantity;
    }
}
