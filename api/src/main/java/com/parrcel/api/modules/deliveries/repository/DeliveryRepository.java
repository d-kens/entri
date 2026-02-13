package com.parrcel.api.modules.deliveries.repository;

import com.parrcel.api.modules.deliveries.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> getDeliveriesByExternalId(String externalId);
}