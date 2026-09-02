package com.entri.events.repository;

import com.entri.events.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @Query("""
        SELECT COALESCE(SUM(tt.soldQuantity), 0) FROM TicketType tt
        WHERE tt.event.organizer.externalKey = :organizerKey
    """)
    long sumSoldQuantityByOrganizer(@Param("organizerKey") String organizerKey);

    @Query("SELECT COALESCE(SUM(tt.soldQuantity), 0) FROM TicketType tt")
    long sumSoldQuantityPlatform();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TicketType t
        WHERE t.id IN :ticketTypeIds
        ORDER BY t.id
        """)
    List<TicketType> findAllForUpdate(
            @Param("ticketTypeIds") Collection<Long> ticketTypeIds
    );
}
