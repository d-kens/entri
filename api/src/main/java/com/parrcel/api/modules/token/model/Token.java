package com.parrcel.api.modules.token.model;

import com.parrcel.api.modules.token.enums.TokenPurpose;
import com.parrcel.api.modules.users.model.User;
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

    // DB owned
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

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
