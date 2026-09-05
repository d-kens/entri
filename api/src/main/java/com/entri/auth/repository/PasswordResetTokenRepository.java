package com.entri.auth.repository;

import com.entri.auth.entity.PasswordResetToken;
import com.entri.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findFirstByUserAndUsedFalseOrderByExpiresAtDesc(User user);
}
