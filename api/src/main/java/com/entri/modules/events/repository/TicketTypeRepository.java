package com.entri.modules.events.repository;

import com.entri.modules.events.entity.Event;
import com.entri.modules.events.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventOrderByDisplayOrderAsc(Event event);
}
