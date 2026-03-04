package com.parrcel.api.modules.payment.repository;


import com.parrcel.api.modules.payment.enums.PaymentType;
import com.parrcel.api.modules.payment.enums.PaymentStatus;
import com.parrcel.api.modules.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalId(String externalId);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    boolean existsByPaymentTypeAndReferenceIdAndStatus(
            PaymentType paymentType, String referenceId, PaymentStatus status
    );
}
