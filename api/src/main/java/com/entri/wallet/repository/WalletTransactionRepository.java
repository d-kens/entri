package com.entri.wallet.repository;

import com.entri.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletOrganizerExternalKey(String organizerExternalKey, Pageable pageable);

    Optional<WalletTransaction> findByTrackingReference(String trackingReference);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM WalletTransaction t
        WHERE t.wallet.organizer.externalKey = :organizerKey
          AND t.type = com.entri.wallet.WalletTransactionType.CREDIT
          AND t.status = com.entri.wallet.WalletTransactionStatus.COMPLETED
    """)
    BigDecimal sumRevenueByOrganizer(@Param("organizerKey") String organizerKey);
}
