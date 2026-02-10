package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.ParcelPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelPointRepository extends JpaRepository<ParcelPoint, Long> {
    List<ParcelPoint> findByZoneId(Long zoneId);
}
