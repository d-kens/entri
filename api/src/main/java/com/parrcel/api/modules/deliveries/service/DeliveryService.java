package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.modules.deliveries.dto.CalcDeliveryFeeDTO;
import com.parrcel.api.modules.deliveries.dto.DeliveryFeeDTO;
import com.parrcel.api.modules.zones.service.AgentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeliveryService {
    private final AgentService agentService;

    public DeliveryFeeDTO calculateDeliveryFee(CalcDeliveryFeeDTO request) {
        var fromPoint = agentService.getAgentPointById(request.fromAgent());
        var toPoint = agentService.getAgentPointById(request.toAgent());

        if (fromPoint.getZone().getIsCbd() || toPoint.getZone().getIsCbd())
            return new DeliveryFeeDTO(150.0);
        else
            return new DeliveryFeeDTO(200.0);
    }
}
