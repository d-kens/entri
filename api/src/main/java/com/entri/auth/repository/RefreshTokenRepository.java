package com.entri.auth.repository;

import com.entri.auth.entity.RefreshToken;
import com.entri.users.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t JOIN FETCH t.user WHERE t.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.tokenHash = :tokenHash AND t.revokedAt IS NULL")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.user = :user AND t.revokedAt IS NULL")
    int revokeAllForUser(@Param("user") User user, @Param("now") Instant now);
}
