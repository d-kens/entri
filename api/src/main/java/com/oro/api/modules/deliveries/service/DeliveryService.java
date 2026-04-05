package com.oro.api.modules.deliveries.service;

import com.oro.api.modules.deliveries.exception.InvalidDeliveryException;
import com.oro.api.common.exception.NotFoundException;
import com.oro.api.modules.deliveries.dto.CreateDeliveryDto;
import com.oro.api.modules.deliveries.dto.TrackDeliveryResponseDto;
import com.oro.api.modules.deliveries.entity.DeliveryStatus;
import com.oro.api.modules.deliveries.entity.Delivery;
import com.oro.api.modules.deliveries.repository.DeliveryRepository;
import com.oro.api.modules.deliveries.repository.DeliverySpecification;
import com.oro.api.modules.notification.enums.NotificationType;
import com.oro.api.modules.notification.events.SendNotificationEvent;
import com.oro.api.modules.routes.service.DeliveryBatchService;
import com.oro.api.modules.user.entity.User;
import com.oro.api.modules.user.service.UserService;
import com.oro.api.modules.zones.service.AgentService;
import org.springframework.security.access.AccessDeniedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class DeliveryService {
    private final ApplicationEventPublisher eventPublisher;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserService userService;
    private final AgentService agentService;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryBatchService deliveryBatchService;

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

    public Delivery getDeliveryByExternalId(String externalId, User currentUser) {
        Delivery delivery = deliveryRepository.findByExternalId(externalId).orElseThrow(
                () -> new NotFoundException("Delivery with ID " + externalId + " not found")
        );

        boolean isMerchant = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("MERCHANT"));
        boolean isAgent = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("AGENT"));

        if (isMerchant && !delivery.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (isAgent) {
            Long userId = currentUser.getId();
            boolean linked = delivery.getFromAgent().getUser().getId().equals(userId)
                    || delivery.getToAgent().getUser().getId().equals(userId);
            if (!linked) {
                throw new AccessDeniedException("Access denied");
            }
        }

        return delivery;
    }

    @Transactional
    public Delivery create(CreateDeliveryDto dto, Long userId) {

        log.info("Creating delivery order for user: {}, from point: {}, to point: {}",
                userId, dto.fromAgent(), dto.toAgent());

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
    public Delivery updateDeliveryStatus(String externalId, DeliveryStatus newStatus, String cancellationReason, boolean isAdmin) {
        Delivery delivery = deliveryRepository.findByExternalId(externalId).orElseThrow(
                () -> new NotFoundException("Delivery with ID " + externalId + " not found")
        );
        DeliveryStatus current = delivery.getDeliveryStatus();

        if (!isAdmin && !newStatus.isAgentAllowed()) {
            throw new InvalidDeliveryException(
                    "Agents are not permitted to set status: " + newStatus);
        }

        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidDeliveryException(
                    "Cannot transition delivery from " + current + " to " + newStatus);
        }

        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case AT_PICKUP_AGENT -> delivery.setDroppedAtPickupAgentAt(now);
            case AT_HUB -> {
                delivery.setArrivedAtHubAt(now);
                deliveryBatchService.assignToBatch(delivery);
            }
            case DELIVERED -> delivery.setDeliveredAt(now);
            case CANCELLED -> {
                delivery.setCancelledAt(now);
                delivery.setCancellationReason(cancellationReason);
            }
            default -> { }
        }

        delivery.setDeliveryStatus(newStatus);
        return deliveryRepository.save(delivery);
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

    public void sendDeliveryNotification(String deliveryExternalId) {
        Delivery delivery = deliveryRepository.findByExternalId(deliveryExternalId)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + deliveryExternalId));


        User user = delivery.getUser();

        eventPublisher.publishEvent(
                new SendNotificationEvent(
                        user,
                        Map.of(
                                "userName", user.getName(),
                                "message", "Your delivery fee has been paid. Please securely package your item, clearly label it with the tracking number, and include the recipient's name and phone number. Drop it off at your selected agent as soon as possible."
                        ),
                        NotificationType.DELIVERY_FEE_PAYMENT
                )
        );
    }


    private Specification<Delivery> buildSpecification(User currentUser, String status, String search) {
        Specification<Delivery> spec = Specification.allOf();

        boolean isMerchant = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("MERCHANT"));
        boolean isAgent = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("AGENT"));

        if (isMerchant) {
            spec = spec.and(DeliverySpecification.hasUser(currentUser.getId()));
        } else if (isAgent) {
            spec = spec.and(DeliverySpecification.isLinkedToAgent(currentUser.getId()));
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
                delivery.getDeliveryStatus(),
                delivery.getTrackingNumber(),
                delivery.getPackageName(),
                delivery.getPackagePrice(),
                delivery.getRecipientName(),
                delivery.getRecipientPhone(),
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
                delivery.getCreated() != null
                        ? formatter.format(delivery.getCreated().atZone(ZoneId.systemDefault()))
                        : "Pending",
                delivery.getFromAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.PENDING)
        ));

        // Event 2: Dropped at Pickup Point
        timeline.add(new TrackDeliveryResponseDto.TrackingTimeline(
                "Dropped at Pickup Point",
                delivery.getDroppedAtPickupAgentAt() != null ? delivery.getDroppedAtPickupAgentAt().format(formatter) : "Pending",
                delivery.getFromAgent().getName(),
                isStatusReached(currentStatus, DeliveryStatus.AT_PICKUP_AGENT)
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
                DeliveryStatus.AT_PICKUP_AGENT,
                DeliveryStatus.AT_HUB,
                DeliveryStatus.OUT_FOR_DELIVERY,
                DeliveryStatus.AT_DESTINATION_AGENT,
                DeliveryStatus.DELIVERED
        );

        int currentIndex = statusOrder.indexOf(currentStatus);
        int targetIndex = statusOrder.indexOf(targetStatus);

        return currentIndex >= targetIndex;
    }
}
