package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.common.exception.InvalidDeliveryException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.model.Delivery;
import com.parrcel.api.modules.deliveries.repository.DeliveryRepository;
import com.parrcel.api.modules.users.service.UserService;
import com.parrcel.api.modules.zones.service.AgentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Delivery getDeliveryByExternalId(String externalId) {
        return deliveryRepository.getDeliveriesByExternalId(externalId).orElseThrow(
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
