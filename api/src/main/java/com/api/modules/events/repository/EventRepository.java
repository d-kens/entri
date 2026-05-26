package com.api.modules.events.repository;

import com.api.modules.events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByExternalId(String externalId);
}
