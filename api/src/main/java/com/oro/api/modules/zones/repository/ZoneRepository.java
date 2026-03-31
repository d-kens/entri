package com.oro.api.modules.zones.repository;

import com.oro.api.modules.zones.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {}
