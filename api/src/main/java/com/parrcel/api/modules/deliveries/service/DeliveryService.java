package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.common.exception.InvalidDeliveryException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.dto.DeliveryResponseDto;
import com.parrcel.api.modules.deliveries.dto.TrackDeliveryResponseDto;
import com.parrcel.api.modules.deliveries.enums.DeliveryStatus;
import com.parrcel.api.modules.deliveries.model.Delivery;
import com.parrcel.api.modules.deliveries.repository.DeliveryRepository;
import com.parrcel.api.modules.deliveries.repository.DeliverySpecification;
import com.parrcel.api.modules.users.enums.Role;
import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.users.service.UserService;
import com.parrcel.api.modules.zones.service.AgentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DeliveryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserService userService;
    private final AgentService agentService;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public Page<Delivery> getDeliveries(
            Long userId,
            String status,
            String search,
            Pageable pageable
    ) {

        log.info("Fetching deliveries for userId: {}", userId);

        var user = userService.getUserById(userId);

        Specification<Delivery> spec = buildSpecification(user, status, search);

        return deliveryRepository.findAll(spec, pageable);
    }

    public Delivery getDeliveryByExternalId(String externalId) {
        return deliveryRepository.findByExternalId(externalId).orElseThrow(
                () -> new NotFoundException("Delivery with ID " + externalId + " not found")
        );
    }

    @Transactional
    public Delivery create(CreateDeliveryDto dto, Long userId) {

        log.info("Creating delivery order for user: {}, from point: {}, to point: {}",
                userId, dto.fromAgent(), dto.toAgent());

        if (dto.collectCash() && (dto.cashAmount() == null || dto.cashAmount().compareTo(BigDecimal.ZERO) <= 0))
            throw new InvalidDeliveryException("cashAmount id required for collect cash");

        var user = userService.getUserById(userId);
        var toAgent = agentService.getAgentPointById(dto.toAgent());
        var fromAgent = agentService.getAgentPointById(dto.fromAgent());

        if (!fromAgent.getIsActive()) {
            throw new InvalidDeliveryException("Pickup point is not currently active");
        }
        if (!toAgent.getIsActive()) {
            throw new InvalidDeliveryException("Delivery point is not currently active");
        }

        String trackingNumber = generateTrackingNumber();

        Delivery delivery =  Delivery.builder()
                .fromAgent(fromAgent)
                .toAgent(toAgent)
                .user(user)
                .collectCash(dto.collectCash())
                .cashAmount(dto.cashAmount())
                .packageName(dto.packageName())
                .packagePrice(dto.packagePrice())
                .packageDescription(dto.packageDescription())
                .recipientName(dto.recipientName())
                .recipientPhone(dto.recipientPhone())
                .deliveryFee(dto.deliveryFee())
                .trackingNumber(trackingNumber)
                .build();

        deliveryRepository.save(delivery);
        return delivery;
    }

    @Transactional
    public void markDeliveryAsPaid(String deliveryExternalId) {
        Delivery delivery = deliveryRepository.findByExternalId(deliveryExternalId)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + deliveryExternalId));

        delivery.markAsPaid();
        deliveryRepository.save(delivery);

        log.info("Delivery {} marked as PAID", deliveryExternalId);
    }

    @Transactional
    public void markDeliveryPaymentFailed(String deliveryExternalId) {
        Delivery delivery = deliveryRepository.findByExternalId(deliveryExternalId)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + deliveryExternalId));

        delivery.markPaymentFailed();
        deliveryRepository.save(delivery);

        log.warn("Delivery {} payment marked as FAILED", deliveryExternalId);
    }

    @Transactional
    public void markCashCollected(String deliveryExternalId) {
        log.info("Marking cash as collected for delivery: {}", deliveryExternalId);

        Delivery delivery = deliveryRepository.findByExternalId(deliveryExternalId)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + deliveryExternalId));

        if (!delivery.isCollectCash()) {
            throw new InvalidDeliveryException("Delivery does not have cash collection enabled");
        }

        if (delivery.isCashCollected()) {
            log.warn("Cash already marked as collected for delivery: {}", deliveryExternalId);
            return;
        }

        delivery.setCashCollected(true);
        deliveryRepository.save(delivery);

        log.info("Cash marked as collected for delivery: {}", deliveryExternalId);
    }


    private Specification<Delivery> buildSpecification(User currentUser, String status, String search) {
        Specification<Delivery> spec = Specification.allOf();

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            spec = spec.and(DeliverySpecification.hasUser(currentUser.getId()));
        }

        if (status != null && !status.isEmpty()) {
            try {
                DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());
                spec = spec.and(DeliverySpecification.hasStatus(deliveryStatus));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid delivery status: {}", status);
            }
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and(DeliverySpecification.search(search));
        }

        return spec;
    }


    private String generateTrackingNumber() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimePrefix = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String randomSuffix = generateRandomString(4);

        return "ORO-" + dateTimePrefix + "-" + randomSuffix;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }


    public TrackDeliveryResponseDto trackByTrackingNumber(String trackingNumber) {
        log.info("Tracking delivery by tracking number: {}", trackingNumber);

        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber).orElseThrow(
                () -> new NotFoundException("Delivery not found with tracking number: " + trackingNumber)
        );

        return buildTrackingResponse(delivery);
    }

    private TrackDeliveryResponseDto buildTrackingResponse(Delivery delivery) {

        List<TrackDeliveryResponseDto.TrackingTimeline> timeline = buildTimeline(delivery);

        TrackDeliveryResponseDto.LocationInfo from = new TrackDeliveryResponseDto.LocationInfo(
                delivery.getFromAgent().getZone().getZoneName(),
                delivery.getFromAgent().getName()
        );

        TrackDeliveryResponseDto.LocationInfo to = new TrackDeliveryResponseDto.LocationInfo(
                delivery.getToAgent().getZone().getZoneName(),
                delivery.getToAgent().getName()
        );

        return new TrackDeliveryResponseDto(
                delivery.getExternalId(),
                delivery.getTrackingNumber(),
                delivery.getPackageName(),
                delivery.getPackagePrice(),
                delivery.getDeliveryStatus(),
                delivery.getRecipientName(),
                delivery.getRecipientPhone(),
                delivery.isCollectCash(),
                delivery.getCashAmount(),
                from,
                to,
                timeline
        );

    }

    private List<TrackDeliveryResponseDto.TrackingTimeline> buildTimeline(Delivery delivery) {
        List<TrackDeliveryResponseDto.TrackingTimeline> timeline = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, h:mm a");

        DeliveryStatus currentStatus = delivery.getDeliveryStatus();

        // Event 1: Package Received
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "Package Received",
                delivery.getCreatedAt() != null ? delivery.getCreatedAt().format(formatter) : "Pending",
                delivery.getFromAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.PENDING)
        ));

        // Event 2: Dropped at Pickup Point
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "Dropped at Pickup Point",
                delivery.getDroppedAtPickupAgentAt() != null ? delivery.getDroppedAtPickupAgentAt().format(formatter) : "Pending",
                delivery.getFromAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.DROPPED_AT_PICKUP_AGENT)
        ));

        // Event 3: At Hub
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "At Hub",
                delivery.getArrivedAtHubAt() != null ? delivery.getArrivedAtHubAt().format(formatter) : "Pending",
                "Main Sorting Hub",
                isStatusReached(currentStatus, DeliveryStatus.AT_HUB)
        ));

        // Event 4: Out for Delivery
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "Out for Delivery",
                delivery.getOutForDeliveryAt() != null ? delivery.getOutForDeliveryAt().format(formatter) : "Pending",
                delivery.getToAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.OUT_FOR_DELIVERY)
        ));

        // Event 5: Delivered
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "Delivered",
                delivery.getDeliveredAt() != null ? delivery.getDeliveredAt().format(formatter) : "Pending",
                delivery.getToAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.DELIVERED)
        ));

        return timeline;

    }

    private boolean isStatusReached(DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        List<DeliveryStatus> statusOrder = List.of(
                DeliveryStatus.PENDING,
                DeliveryStatus.DROPPED_AT_PICKUP_AGENT,
                DeliveryStatus.AT_HUB,
                DeliveryStatus.OUT_FOR_DELIVERY,
                DeliveryStatus.DELIVERED
        );

        int currentIndex = statusOrder.indexOf(currentStatus);
        int targetIndex = statusOrder.indexOf(targetStatus);

        return currentIndex >= targetIndex;
    }
}
