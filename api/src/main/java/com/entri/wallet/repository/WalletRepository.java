package com.entri.wallet.repository;

import com.entri.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByOrganizerExternalKey(String organizerExternalKey);
}
