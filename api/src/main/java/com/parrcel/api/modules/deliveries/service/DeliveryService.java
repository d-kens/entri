package com.parrcel.api.modules.deliveries.service;


import com.parrcel.api.modules.deliveries.dto.CalcDeliveryFeeDTO;
import com.parrcel.api.modules.deliveries.dto.DeliveryFeeDTO;
import com.parrcel.api.modules.zones.service.ParrcelPointService;
import com.parrcel.api.modules.zones.service.ZoneService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeliveryService {
    private final ZoneService zoneService;
    private final ParrcelPointService parrcelPointService;

    public DeliveryFeeDTO calculateDeliveryFee(CalcDeliveryFeeDTO request) {
        return new DeliveryFeeDTO(5000.0);
    }
}
