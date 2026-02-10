package com.parrcel.api.modules.zones.repository;

import com.parrcel.api.modules.zones.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByZoneId(Long zoneId);
}
