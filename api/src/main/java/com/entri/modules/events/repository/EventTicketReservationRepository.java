package com.entri.modules.events.repository;

import com.entri.modules.events.entity.EventTicketReservation;
import com.entri.modules.events.entity.TicketReservationStatus;
import com.entri.modules.events.repository.TicketTypeReservationSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {
    @Query("""
        SELECT new com.entri.modules.events.repository.TicketTypeReservationSum(
            i.ticketType.id,
            COALESCE(SUM(i.quantity), 0)
        )
        FROM EventTicketReservationItem i
        JOIN i.reservation r
        WHERE i.ticketType.id IN :ticketTypeIds
          AND r.status = :status
          AND r.expiresAt > CURRENT_TIMESTAMP
        GROUP BY i.ticketType.id
        """)
    List<TicketTypeReservationSum> sumActiveReservationsByTicketTypes(
            @Param("ticketTypeIds") Collection<Long> ticketTypeIds,
            @Param("status") TicketReservationStatus status
    );
}
