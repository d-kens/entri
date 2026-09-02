package com.entri.events.repository;

import com.entri.events.entity.EventTicketReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventTicketReservationRepository extends JpaRepository<EventTicketReservation, Long> {

    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0) FROM EventTicketReservation r
        WHERE r.event.organizer.externalKey = :organizerKey
          AND r.status = com.entri.events.entity.EventTicketReservationStatus.CONFIRMED
    """)
    BigDecimal sumRevenueByOrganizer(@Param("organizerKey") String organizerKey);

    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0) FROM EventTicketReservation r
        WHERE r.status = com.entri.events.entity.EventTicketReservationStatus.CONFIRMED
    """)
    BigDecimal sumRevenuePlatform();

    @Query(value = """
        SELECT DATE(r.date_created) AS sale_date,
               COALESCE(SUM(item_counts.total_qty), 0) AS tickets_sold,
               SUM(r.total_amount) AS revenue
        FROM event_ticket_reservations r
        JOIN events e ON r.event_id = e.id
        LEFT JOIN (
            SELECT reservation_id, SUM(quantity) AS total_qty
            FROM event_ticket_reservation_items
            GROUP BY reservation_id
        ) item_counts ON item_counts.reservation_id = r.id
        WHERE e.organizer_id = (SELECT u.id FROM users u WHERE u.external_key = :organizerKey)
          AND r.status = 'CONFIRMED'
          AND r.date_created >= :from
        GROUP BY DATE(r.date_created)
        ORDER BY DATE(r.date_created)
    """, nativeQuery = true)
    List<Object[]> findDailySalesTrend(@Param("organizerKey") String organizerKey, @Param("from") Instant from);

    @Query(value = """
        SELECT DATE_FORMAT(r.date_created, '%Y-%m') AS sale_date,
               COALESCE(SUM(item_counts.total_qty), 0) AS tickets_sold,
               SUM(r.total_amount) AS revenue
        FROM event_ticket_reservations r
        JOIN events e ON r.event_id = e.id
        LEFT JOIN (
            SELECT reservation_id, SUM(quantity) AS total_qty
            FROM event_ticket_reservation_items
            GROUP BY reservation_id
        ) item_counts ON item_counts.reservation_id = r.id
        WHERE e.organizer_id = (SELECT u.id FROM users u WHERE u.external_key = :organizerKey)
          AND r.status = 'CONFIRMED'
          AND r.date_created >= :from
        GROUP BY DATE_FORMAT(r.date_created, '%Y-%m')
        ORDER BY DATE_FORMAT(r.date_created, '%Y-%m')
    """, nativeQuery = true)
    List<Object[]> findMonthlySalesTrend(@Param("organizerKey") String organizerKey, @Param("from") Instant from);

    @Query(value = """
        SELECT DATE(r.date_created) AS sale_date,
               COALESCE(SUM(item_counts.total_qty), 0) AS tickets_sold,
               SUM(r.total_amount) AS revenue
        FROM event_ticket_reservations r
        LEFT JOIN (
            SELECT reservation_id, SUM(quantity) AS total_qty
            FROM event_ticket_reservation_items
            GROUP BY reservation_id
        ) item_counts ON item_counts.reservation_id = r.id
        WHERE r.status = 'CONFIRMED'
          AND r.date_created >= :from
        GROUP BY DATE(r.date_created)
        ORDER BY DATE(r.date_created)
    """, nativeQuery = true)
    List<Object[]> findDailySalesTrendPlatform(@Param("from") Instant from);

    @Query(value = """
        SELECT DATE_FORMAT(r.date_created, '%Y-%m') AS sale_date,
               COALESCE(SUM(item_counts.total_qty), 0) AS tickets_sold,
               SUM(r.total_amount) AS revenue
        FROM event_ticket_reservations r
        LEFT JOIN (
            SELECT reservation_id, SUM(quantity) AS total_qty
            FROM event_ticket_reservation_items
            GROUP BY reservation_id
        ) item_counts ON item_counts.reservation_id = r.id
        WHERE r.status = 'CONFIRMED'
          AND r.date_created >= :from
        GROUP BY DATE_FORMAT(r.date_created, '%Y-%m')
        ORDER BY DATE_FORMAT(r.date_created, '%Y-%m')
    """, nativeQuery = true)
    List<Object[]> findMonthlySalesTrendPlatform(@Param("from") Instant from);

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
