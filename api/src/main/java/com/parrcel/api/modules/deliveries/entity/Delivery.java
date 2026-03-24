package com.parrcel.api.modules.deliveries.entity;

import com.parrcel.api.common.entity.AbstractAuditableEntity;
import com.parrcel.api.modules.payment.entity.PaymentStatus;
import com.parrcel.api.modules.users.entity.User;
import com.parrcel.api.modules.zones.entity.Agent;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "deliveries")
public class Delivery extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    private String externalId;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 30)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 15)
    private String recipientPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_agent_id", nullable = false)
    private Agent fromAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_agent_id", nullable = false)
    private Agent toAgent;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "package_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagePrice;

    @Column(name = "package_description", length = 500)
    private String packageDescription;

    @Builder.Default
    @Column(name = "collect_cash", nullable = false)
    private boolean collectCash = false;

    @Column(name = "cash_amount", precision = 10, scale = 2)
    private BigDecimal cashAmount;

    @Builder.Default
    @Column(name = "cash_collected", nullable = false)
    private boolean cashCollected = false;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 50)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "dropped_at_pickup_agent_at")
    private LocalDateTime droppedAtPickupAgentAt;

    @Column(name = "arrived_at_hub_at")
    private LocalDateTime arrivedAtHubAt;

    @Column(name = "out_for_delivery_at")
    private LocalDateTime outForDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @PrePersist
    protected void onCreate() {
        if (externalId == null) {
            externalId = UUID.randomUUID().toString();
        }
    }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }


    public boolean isPaid() {
        return paymentStatus == PaymentStatus.SUCCESS;
    }

    public boolean isDelivered() {
        return deliveryStatus == DeliveryStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return deliveryStatus == DeliveryStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return deliveryStatus == DeliveryStatus.PENDING ||
                deliveryStatus == DeliveryStatus.DROPPED_AT_PICKUP_AGENT;
    }
}

