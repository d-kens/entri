package com.entri.wallet.entity;

import com.entri.common.entity.AbstractAuditableEntity;
import com.entri.users.entity.User;
import com.entri.wallet.WalletType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
public class Wallet extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String externalId = UUID.randomUUID().toString();

    @Column(name = "wallet_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WalletType walletType = WalletType.ORGANIZER;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", unique = true)
    private User organizer;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
