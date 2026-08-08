package com.entri.modules.events.repository;

import com.entri.modules.events.entity.EventTicketReservation;
import com.entri.modules.events.entity.TicketReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {
    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM EventTicketReservationItem i
        JOIN i.reservation r
        WHERE i.ticketType.id = :ticketTypeId
          AND r.status = :status
          AND r.expiresAt > CURRENT_TIMESTAMP
        """)
    Long sumActiveReservations(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("status") TicketReservationStatus status
    );
}
