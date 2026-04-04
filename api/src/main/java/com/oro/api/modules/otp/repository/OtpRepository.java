package com.oro.api.modules.otp.repository;

import com.oro.api.modules.otp.entity.Otp;
import com.oro.api.modules.otp.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query("SELECT o FROM Otp o WHERE o.user.id = ?1 AND o.otpHash = ?2 AND o.purpose = ?3 " +
            "AND o.usedAt IS NULL AND o.invalidatedAt IS NULL")
    Optional<Otp> findValidOtpByUserAndHash(Long userId, String otpHash, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE Otp o SET o.invalidatedAt = CURRENT_TIMESTAMP WHERE o.user.id = ?1 " +
            "AND o.purpose = ?2 AND o.usedAt IS NULL AND o.invalidatedAt IS NULL")
    int invalidateUnusedOtpsByUserAndPurpose(Long userId, OtpPurpose purpose);
}
