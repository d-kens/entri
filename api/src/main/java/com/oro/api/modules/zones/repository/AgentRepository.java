package com.oro.api.modules.zones.repository;

import com.oro.api.modules.zones.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByZoneId(Long zoneId);
}
