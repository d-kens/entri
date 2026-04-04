package com.oro.api.modules.routes.service;

import com.oro.api.common.exception.NotFoundException;
import com.oro.api.modules.deliveries.entity.Delivery;
import com.oro.api.modules.deliveries.entity.DeliveryStatus;
import com.oro.api.modules.deliveries.repository.DeliveryRepository;
import com.oro.api.modules.routes.dto.AssignDriverDto;
import com.oro.api.modules.routes.dto.BatchResponseDto;
import com.oro.api.modules.routes.entity.BatchStatus;
import com.oro.api.modules.routes.entity.DeliveryBatch;
import com.oro.api.modules.routes.entity.DeliveryRoute;
import com.oro.api.modules.routes.repository.DeliveryBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryBatchService {

    private final DeliveryBatchRepository batchRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * Called automatically when a delivery status moves to AT_HUB.
     * Finds or creates an open batch for the delivery's toAgent route,
     * then adds the delivery to it.
     */
    @Transactional
    public void assignToBatch(Delivery delivery) {
        DeliveryRoute route = delivery.getToAgent().getRoute();

        if (route == null) {
            log.warn("Delivery {} toAgent has no route assigned — skipping batch assignment",
                    delivery.getExternalId());
            return;
        }

        DeliveryBatch batch = batchRepository
                .findByRouteAndStatus(route, BatchStatus.OPEN)
                .orElseGet(() -> {
                    DeliveryBatch newBatch = new DeliveryBatch();
                    newBatch.setRoute(route);
                    log.info("Creating new batch for route: {}", route.getName());
                    return batchRepository.save(newBatch);
                });

        delivery.setBatch(batch);
        deliveryRepository.save(delivery);
        log.info("Delivery {} assigned to batch {} (route: {})",
                delivery.getExternalId(), batch.getExternalId(), route.getName());
    }

    /**
     * Dispatches a batch — moves all its deliveries to OUT_FOR_DELIVERY in one go.
     */
    @Transactional
    public DeliveryBatch dispatchBatch(String batchExternalId) {
        DeliveryBatch batch = getBatchByExternalId(batchExternalId);

        if (batch.getStatus() != BatchStatus.OPEN) {
            throw new IllegalStateException("Only OPEN batches can be dispatched");
        }

        LocalDateTime now = LocalDateTime.now();
        batch.getDeliveries().forEach(d -> {
            d.setDeliveryStatus(DeliveryStatus.OUT_FOR_DELIVERY);
            d.setOutForDeliveryAt(now);
        });

        batch.setStatus(BatchStatus.DISPATCHED);
        return batchRepository.save(batch);
    }

    @Transactional
    public DeliveryBatch assignDriver(String batchExternalId, AssignDriverDto dto) {
        DeliveryBatch batch = getBatchByExternalId(batchExternalId);
        batch.setDriverName(dto.driverName());
        batch.setDriverPhone(dto.driverPhone());
        return batchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public List<DeliveryBatch> getBatchesByStatus(BatchStatus status) {
        if (status == null) return batchRepository.findAll();
        return batchRepository.findAllByStatus(status);
    }

    @Transactional(readOnly = true)
    public DeliveryBatch getBatchByExternalId(String externalId) {
        return batchRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Batch not found: " + externalId));
    }

    public BatchResponseDto toDto(DeliveryBatch batch) {
        List<BatchResponseDto.BatchDeliveryDto> deliveryDtos = batch.getDeliveries().stream()
                .map(d -> new BatchResponseDto.BatchDeliveryDto(
                        d.getExternalId(),
                        d.getTrackingNumber(),
                        d.getRecipientName(),
                        d.getRecipientPhone(),
                        d.getToAgent().getName(),
                        d.getToAgent().getAddressDescription()
                ))
                .toList();

        return new BatchResponseDto(
                batch.getExternalId(),
                batch.getRoute().getName(),
                batch.getStatus(),
                batch.getDriverName(),
                batch.getDriverPhone(),
                deliveryDtos
        );
    }
}
