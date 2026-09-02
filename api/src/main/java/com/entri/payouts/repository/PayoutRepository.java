package com.entri.payouts.repository;

import com.entri.payouts.entity.Payout;
import com.entri.payouts.entity.PayoutStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findByStatusAndAttemptsLessThan(PayoutStatus status, int maxAttempts, Pageable pageable);

    Optional<Payout> findByTrackingId(String trackingId);
}
