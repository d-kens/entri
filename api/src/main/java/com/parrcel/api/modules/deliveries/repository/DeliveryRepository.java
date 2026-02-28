package com.parrcel.api.modules.deliveries.repository;

import com.parrcel.api.modules.deliveries.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    Optional<Delivery> findByExternalId(String externalId);
}