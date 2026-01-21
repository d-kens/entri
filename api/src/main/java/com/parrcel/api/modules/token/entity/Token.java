package com.parrcel.api.modules.token.entity;

import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.user.entity.User;
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
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token_hash")
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose")
    private TokenPurpose purpose;

    @Column(name = "request_ip")
    private String requestIp;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    public boolean isValid() {
        Instant now = Instant.now();
        return now.isBefore(expiresAt) && usedAt == null && invalidatedAt == null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isInvalidated() {
        return invalidatedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    public void invalidate() {
        this.invalidatedAt = Instant.now();
    }
}