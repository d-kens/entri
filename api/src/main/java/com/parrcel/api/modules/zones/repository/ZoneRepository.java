package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {}
