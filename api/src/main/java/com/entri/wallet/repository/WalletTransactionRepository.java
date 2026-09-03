package com.entri.wallet.repository;

import com.entri.wallet.WalletTransactionStatus;
import com.entri.wallet.WalletTransactionType;
import com.entri.wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    boolean existsByReferenceIdAndType(String referenceId, WalletTransactionType type);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END), 0)
            FROM WalletTransaction t
            WHERE t.wallet.organizer.externalKey = :organizerKey
              AND t.status = :status
            """)
    BigDecimal calculateBalance(@Param("organizerKey") String organizerKey,
                                @Param("status") WalletTransactionStatus status);
}
