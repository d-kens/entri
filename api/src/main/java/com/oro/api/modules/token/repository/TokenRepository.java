package com.oro.api.modules.token.repository;

import com.oro.api.modules.token.entity.Token;
import com.oro.api.modules.token.entity.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE Token t SET t.invalidatedAt = CURRENT_TIMESTAMP WHERE t.user.id = ?1 " +
            "AND t.purpose = ?2 AND t.usedAt IS NULL AND t.invalidatedAt IS NULL")
    int invalidateUnusedTokensByUserAndPurpose(Long userId, TokenPurpose purpose);

}