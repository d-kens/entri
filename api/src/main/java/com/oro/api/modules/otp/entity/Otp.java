package com.oro.api.modules.otp.entity;

import com.oro.api.common.entity.AbstractAuditableEntity;
import com.oro.api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "otp")
public class Otp extends AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_hash", nullable = false, length = 128)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OtpPurpose purpose;

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
