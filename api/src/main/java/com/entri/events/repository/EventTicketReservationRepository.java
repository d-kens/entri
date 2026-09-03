package com.entri.events.repository;

import com.entri.events.entity.EventTicketReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {

    @Query("""
        SELECT r FROM EventTicketReservation r
        WHERE r.event.externalId = :eventExternalId
        ORDER BY r.dateCreated DESC
    """)
    Page<EventTicketReservation> findByEventExternalId(
            @Param("eventExternalId") String eventExternalId,
            Pageable pageable
    );

    @Query("""
        SELECT i.reservation.id, SUM(i.quantity)
        FROM EventTicketReservationItem i
        WHERE i.reservation.id IN :reservationIds
        GROUP BY i.reservation.id
    """)
    List<Object[]> sumQuantitiesByReservationIds(@Param("reservationIds") List<Long> reservationIds);

    @Query("""                                                                                                                                                                    
          SELECT r FROM EventTicketReservation r                                                                                                                                    
          JOIN FETCH r.items i                                                                                                                                                    
          JOIN FETCH i.ticketType                                                                                                                                                 
          WHERE r.externalId = :externalId
    """)
    Optional<EventTicketReservation> findByExternalIdWithItems(@Param("externalId") String externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EventTicketReservation r WHERE r.externalId = :externalId")
    Optional<EventTicketReservation> findByExternalIdForUpdate(@Param("externalId") String externalId);

    @Query(value = """
    SELECT *
    FROM event_ticket_reservations
    WHERE status = 'PENDING'
      AND expires_at < :now
    ORDER BY id ASC
    LIMIT :batchSize
    FOR UPDATE
    """, nativeQuery = true)
    List<EventTicketReservation> findExpiredPendingReservations(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );
}
