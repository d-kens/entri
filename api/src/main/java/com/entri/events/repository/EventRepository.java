package com.entri.events.repository;

import com.entri.events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Optional<Event> findByExternalId(String externalId);

    @Query("""
        SELECT COUNT(e) FROM Event e
        WHERE e.organizer.externalKey = :organizerKey
          AND e.status = com.entri.events.entity.EventStatus.PUBLISHED
          AND e.startTime > :now
    """)
    long countUpcomingEvents(@Param("organizerKey") String organizerKey, @Param("now") Instant now);

    @Query("""
        SELECT COUNT(e) FROM Event e
        WHERE e.organizer.externalKey = :organizerKey
          AND e.status = com.entri.events.entity.EventStatus.PUBLISHED
          AND e.startTime <= :now
          AND e.endTime >= :now
    """)
    long countLiveEvents(@Param("organizerKey") String organizerKey, @Param("now") Instant now);

}
