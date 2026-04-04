package com.oro.api.modules.routes.repository;

import com.oro.api.modules.routes.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    Optional<DeliveryRoute> findByExternalId(String externalId);
}
