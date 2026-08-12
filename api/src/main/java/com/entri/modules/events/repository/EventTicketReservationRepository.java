package com.entri.modules.events.repository;

import com.entri.modules.events.entity.EventTicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {
    Optional<EventTicketReservation> findByExternalId(String externalId);
}
