package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Page<Zone> findAll(Pageable pageable);
}
