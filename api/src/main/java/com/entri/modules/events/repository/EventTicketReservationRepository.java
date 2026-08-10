package com.entri.modules.events.repository;

import com.entri.modules.events.entity.EventTicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {
}
