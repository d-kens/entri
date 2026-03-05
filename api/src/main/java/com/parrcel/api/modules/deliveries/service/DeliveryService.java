package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.common.exception.InvalidDeliveryException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
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

        return "PAR-" + dateTimePrefix + "-" + randomSuffix;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
