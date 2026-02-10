package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.ParrcelPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelPointRepository extends JpaRepository<ParrcelPoint, Long> {
    List<ParrcelPoint> findByZoneId(Long zoneId);
}
