package com.entri.payouts.entity;

import com.entri.common.entity.AbstractAuditableEntity;
import com.entri.events.entity.EventTicketReservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payouts")
public class Payout extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String idempotencyKey = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private EventTicketReservation reservation;

    @Column(name = "payout_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PayoutMethod payoutMethod;

    @Column(name = "payout_recipient_name", nullable = false)
    private String payoutRecipientName;

    @Column(name = "payout_account", nullable = false)
    private String payoutAccount;

    @Column(name = "payout_account_reference")
    private String payoutAccountReference;

    @Column(name = "payout_bank_code")
    private String payoutBankCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "last_attempted_at")
    private Instant lastAttemptedAt;

    @Column(name = "tracking_id", length = 36)
    private String trackingId;
}
