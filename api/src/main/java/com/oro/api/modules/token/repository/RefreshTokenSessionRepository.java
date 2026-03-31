package com.oro.api.modules.token.repository;

import com.oro.api.modules.token.entity.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    Optional<RefreshTokenSession> findByRefreshTokenHashAndIsRevokedFalse(String refreshTokenHash);

    @Query("SELECT rts FROM RefreshTokenSession rts WHERE rts.user.id = :userId AND rts.isRevoked = false")
    List<RefreshTokenSession> findActiveSessions(@Param("userId") Long userId);

    void deleteByRefreshTokenHash(String refreshTokenHash);
}
