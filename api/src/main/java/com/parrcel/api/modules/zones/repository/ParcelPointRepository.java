package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.ParcelPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelPointRepository extends JpaRepository<ParcelPoint, Long> {
    Page<ParcelPoint> findAll(Pageable pageable);
    Page<ParcelPoint> findByZoneId(Long zoneId, Pageable pageable);
}
