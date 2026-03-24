package com.parrcel.api.modules.token.entity;

import com.parrcel.api.common.entity.AbstractAuditableEntity;
import com.parrcel.api.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tokens")
public class Token extends AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TokenPurpose purpose;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;


    public boolean isValid() {
        Instant now = Instant.now();
        return now.isBefore(expiresAt) && usedAt == null && invalidatedAt == null;
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    public void invalidate() {
        this.invalidatedAt = Instant.now();
    }
}
