package com.entri.wallet.repository;

import com.entri.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletOrganizerExternalKey(String organizerExternalKey, Pageable pageable);

    Optional<WalletTransaction> findByTrackingReference(String trackingReference);
}
