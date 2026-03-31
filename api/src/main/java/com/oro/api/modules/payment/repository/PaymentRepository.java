package com.oro.api.modules.payment.repository;


import com.oro.api.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalId(String externalId);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
}
