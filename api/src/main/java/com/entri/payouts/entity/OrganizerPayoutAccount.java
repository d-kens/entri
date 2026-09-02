package com.entri.payouts.entity;

import com.entri.common.entity.AbstractAuditableEntity;
import com.entri.users.entity.User;
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

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organizer_payout_accounts")
public class OrganizerPayoutAccount extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String externalId = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PayoutMethod method;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "account", nullable = false, length = 100)
    private String account;

    @Column(name = "account_reference", length = 100)
    private String accountReference;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
