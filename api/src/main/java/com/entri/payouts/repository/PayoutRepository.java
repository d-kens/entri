package com.entri.payouts.repository;

import com.entri.payouts.entity.Payout;
import com.entri.payouts.entity.PayoutStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findByStatusAndAttemptsLessThan(PayoutStatus status, int maxAttempts, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM Payout p
        WHERE p.status = com.entri.payouts.entity.PayoutStatus.COMPLETED
    """)
    BigDecimal sumCompletedPayouts();

    @Query("""
        SELECT COUNT(p) FROM Payout p
        WHERE p.status = com.entri.payouts.entity.PayoutStatus.FAILED
          AND p.attempts >= :maxAttempts
    """)
    long countPermanentlyFailed(@Param("maxAttempts") int maxAttempts);
}
