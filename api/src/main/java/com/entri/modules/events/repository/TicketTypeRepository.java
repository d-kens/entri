package com.entri.modules.events.repository;

import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEvent(Event event);

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
