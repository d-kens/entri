package com.parrcel.api.modules.deliveries.service;

import com.parrcel.api.modules.zones.service.AgentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeliveryService {
    private final AgentService agentService;
}
