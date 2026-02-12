package com.parrcel.api.modules.deliveries.repository;

import com.parrcel.api.modules.deliveries.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
