package com.parrcel.api.modules.payment.model;


import com.parrcel.api.modules.payment.enums.PaymentDirection;
import com.parrcel.api.modules.payment.enums.PaymentType;
import com.parrcel.api.modules.payment.enums.PaymentMethod;
import com.parrcel.api.modules.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    private String externalId = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_direction", nullable = false, length = 20)
    private PaymentDirection paymentDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 50)
    private PaymentType paymentType;

    @Column(name = "reference_id", nullable = false, length = 36)
    private String referenceId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 50)
    private PaymentMethod method;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Column(name = "paid_by", length = 100)
    private String paidBy;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public void markPaid(String providerReference) {
        this.status = PaymentStatus.SUCCESS;
        this.providerReference = providerReference;
        this.paidAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }

    public boolean isTerminal() {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED;
    }
}
