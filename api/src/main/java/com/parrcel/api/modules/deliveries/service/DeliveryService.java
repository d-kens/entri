package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.modules.deliveries.dto.CreateDeliveryDto;
import com.parrcel.api.modules.deliveries.model.Delivery;
import com.parrcel.api.modules.deliveries.repository.DeliveryRepository;
import com.parrcel.api.modules.zones.service.AgentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeliveryService {

    private final AgentService agentService;
    private final DeliveryRepository deliveryRepository;

    // TODO: Fetching the currently logged in user and saving them with the delivery
    // TODO Catching legal argument exceptio

    public Delivery create(CreateDeliveryDto dto){

        var toAgent = agentService.getAgentPointById(dto.toAgent());
        var fromAgent = agentService.getAgentPointById(dto.fromAgent());

        var delivery = Delivery.create(
                fromAgent,
                toAgent,
                dto.collectCash(),
                dto.cashAmount(),
                dto.packageName(),
                dto.packagePrice(),
                dto.packageDescription(),
                dto.recipientName(),
                dto.recipientPhone(),
                dto.deliveryFee()
        );

        return delivery;
        // deliveryRepository.save(delivery);

    }
}
