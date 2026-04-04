package com.oro.api.modules.routes.repository;

import com.oro.api.modules.routes.entity.BatchStatus;
import com.oro.api.modules.routes.entity.DeliveryBatch;
import com.oro.api.modules.routes.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryBatchRepository extends JpaRepository<DeliveryBatch, Long> {
    Optional<DeliveryBatch> findByExternalId(String externalId);
    Optional<DeliveryBatch> findByRouteAndStatus(DeliveryRoute route, BatchStatus status);
    List<DeliveryBatch> findAllByStatus(BatchStatus status);
}
