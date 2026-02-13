package com.parrcel.api.modules.deliveries.model;

import com.parrcel.api.common.exception.InvalidDeliveryException;
import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;
import com.parrcel.api.modules.deliveries.enums.PaymentMethod;
import com.parrcel.api.modules.deliveries.enums.PaymentStatus;
import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.zones.model.Agent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 20)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

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

    @Column(name = "cash_collected_at")
    private LocalDateTime cashCollectedAt;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static Delivery create(
            Agent fromAgent,
            Agent toAgent,
            Boolean collectCash,
            BigDecimal cashAmount,
            String packageName,
            BigDecimal packagePrice,
            String packageDescription,
            String recipientName,
            String recipientPhone,
            BigDecimal deliveryFee
    ) {

        if (collectCash && (cashAmount == null || cashAmount.compareTo(BigDecimal.ZERO) <= 0))
            throw new InvalidDeliveryException("cashAmount id required for collect cash");

        return Delivery.builder()
                .fromAgent(fromAgent)
                .toAgent(toAgent)
                .collectCash(collectCash)
                .cashAmount(cashAmount)
                .packageName(packageName)
                .packagePrice(packagePrice)
                .packageDescription(packageDescription)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryFee(deliveryFee)
                .build();
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
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