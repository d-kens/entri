package com.parrcel.api.modules.wallet.repository;


import com.parrcel.api.modules.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByExternalId(String externalId);

    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);

    Optional<WalletTransaction> findByReferenceTypeAndReferenceId(
            String referenceType,
            String referenceId
    );
}
