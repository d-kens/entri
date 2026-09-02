package com.entri.tickets.repository;

import com.entri.tickets.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.externalId = :eventExternalId")
    long countByEventExternalId(@Param("eventExternalId") String eventExternalId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.externalId = :eventExternalId AND t.checkedInAt IS NOT NULL")
    long countCheckedInByEventExternalId(@Param("eventExternalId") String eventExternalId);

    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.ticketType
        WHERE t.event.externalId = :eventExternalId
          AND t.checkedInAt IS NOT NULL
        ORDER BY t.checkedInAt DESC
    """)
    List<Ticket> findRecentCheckIns(@Param("eventExternalId") String eventExternalId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.reservation JOIN FETCH t.event JOIN FETCH t.ticketType WHERE t.reservation.externalId = :reservationExternalId")
    List<Ticket> findByReservationExternalId(@Param("reservationExternalId") String reservationExternalId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.reservation JOIN FETCH t.event JOIN FETCH t.ticketType WHERE t.ticketExternalId = :externalId")
    Optional<Ticket> findByExternalIdWithDetails(@Param("externalId") String externalId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.reservation JOIN FETCH t.event JOIN FETCH t.ticketType WHERE t.ticketCode = :ticketCode")
    Optional<Ticket> findByTicketCodeWithDetails(@Param("ticketCode") String ticketCode);

    boolean existsByReservationId(Long reservationId);

    @Modifying
    @Query("UPDATE Ticket t SET t.status = com.entri.tickets.entity.TicketStatus.USED, t.checkedInAt = :now WHERE t.ticketCode = :ticketCode AND t.status = com.entri.tickets.entity.TicketStatus.VALID")
    int markAsUsed(@Param("ticketCode") String ticketCode, @Param("now") Instant now);
}
